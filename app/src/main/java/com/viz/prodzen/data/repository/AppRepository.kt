package com.viz.prodzen.data.repository

import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun updateTrackedApp(appInfo: AppInfo)
    fun getTrackedApps(): Flow<List<AppInfo>>
    suspend fun getAppByPackageName(packageName: String): AppInfo?
    suspend fun getAppUsageToday(packageName: String): Long
    suspend fun getAllAppsWithUsage(timeRange: TimeRange): List<AppInfo> // Updated
}

enum class TimeRange {
    TODAY, WEEK, MONTH
}

