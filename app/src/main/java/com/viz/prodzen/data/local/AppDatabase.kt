package com.viz.prodzen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.viz.prodzen.data.model.AppInfo

// FIXED: Removed AutoMigration to simplify the build process for now.
// The DI module already includes fallbackToDestructiveMigration.
@Database(
    entities = [AppInfo::class, DailyUsage::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun usageDao(): UsageDao
}
