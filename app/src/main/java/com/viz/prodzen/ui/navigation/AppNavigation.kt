package com.viz.prodzen.ui.navigation

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.viz.prodzen.ui.screens.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // The app now always starts at the main screen, which handles its own internal navigation.
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen()
        }
    }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

