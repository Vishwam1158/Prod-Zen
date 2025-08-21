package com.viz.prodzen.data.local

import androidx.room.*
import com.viz.prodzen.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(appInfo: AppInfo)

    @Query("SELECT * FROM tracked_apps WHERE isTracked = 1")
    fun getTrackedApps(): Flow<List<AppInfo>>

    @Query("SELECT * FROM tracked_apps")
    suspend fun getAllApps(): List<AppInfo>

    @Query("SELECT * FROM tracked_apps WHERE packageName = :packageName")
    suspend fun getAppByPackageName(packageName: String): AppInfo?
}