package com.viz.prodzen.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Statistics : BottomNavItem("statistics", Icons.Default.BarChart, "Stats")
    object Focus : BottomNavItem("focus", Icons.Default.Timer, "Focus")
    object Limits : BottomNavItem("limits", Icons.Default.Shield, "Limits")
    object Intentions : BottomNavItem("intentions", Icons.Default.Edit, "Intentions") // NEW
}
