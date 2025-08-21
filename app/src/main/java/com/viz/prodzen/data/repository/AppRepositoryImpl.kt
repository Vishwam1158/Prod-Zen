package com.viz.prodzen.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import com.viz.prodzen.data.local.AppDao
import com.viz.prodzen.data.local.UsageDao
import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val appDao: AppDao,
    private val usageDao: UsageDao,
    private val context: Context
) : AppRepository {

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        return@withContext getAllAppsWithUsage(TimeRange.TODAY)
    }

    override suspend fun getAllAppsWithUsage(timeRange: TimeRange): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val savedApps = appDao.getAllApps().associateBy { it.packageName }

        val usageStatsMap = when (timeRange) {
            TimeRange.TODAY -> getUsageStatsForPeriod(TimeRange.TODAY)
            TimeRange.WEEK -> getUsageStatsForPeriod(TimeRange.WEEK)
            TimeRange.MONTH -> getUsageStatsForPeriod(TimeRange.MONTH)
        }

        return@withContext pm.queryIntentActivities(mainIntent, 0).mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appName = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)

            val savedAppInfo = savedApps[packageName]
            val isTracked = savedAppInfo?.isTracked ?: false
            val timeLimit = savedAppInfo?.timeLimitMinutes ?: 0
            val hasIntention = savedAppInfo?.hasIntention ?: false // NEW
            val usage = usageStatsMap[packageName] ?: 0L

            AppInfo(packageName, appName, isTracked, timeLimit, hasIntention, icon, usage)
        }.sortedByDescending { it.usageTodayMillis }
    }

    private suspend fun getUsageStatsForPeriod(timeRange: TimeRange): Map<String, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        when (timeRange) {
            TimeRange.TODAY -> calendar.set(Calendar.HOUR_OF_DAY, 0)
            TimeRange.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            TimeRange.MONTH -> calendar.add(Calendar.MONTH, -1)
        }
        val startTime = calendar.timeInMillis

        return if (timeRange == TimeRange.TODAY) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                .associate { it.packageName to it.totalTimeInForeground }
        } else {
            usageDao.getUsageSince(startTime)
                .groupBy { it.packageName }
                .mapValues { entry -> entry.value.sumOf { it.usageInMillis } }
        }
    }

    override suspend fun getAppUsageToday(packageName: String): Long = withContext(Dispatchers.IO) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        return@withContext usageStats.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }

    override suspend fun updateTrackedApp(appInfo: AppInfo) {
        val appToSave = appInfo.copy(usageTodayMillis = 0)
        appDao.insertOrUpdateApp(appToSave)
    }

    override fun getTrackedApps(): Flow<List<AppInfo>> {
        return appDao.getTrackedApps()
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? {
        return appDao.getAppByPackageName(packageName)
    }
}
