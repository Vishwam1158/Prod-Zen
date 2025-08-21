package com.viz.prodzen.ui.screens.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.ui.navigation.hasUsageStatsPermission
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }

    // This will refresh the permission status when the user returns to the app
    LaunchedEffect(Unit) {
        hasPermission = hasUsageStatsPermission(context)
        if (hasPermission) {
            viewModel.loadUsageStatsForCurrentRange()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Usage Statistics") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (hasPermission) {
                StatisticsDashboard(uiState = uiState, viewModel = viewModel)
            } else {
                PermissionPrompt()
            }
        }
    }
}

@Composable
fun StatisticsDashboard(uiState: HomeUiState, viewModel: HomeViewModel) {
    TabRow(selectedTabIndex = uiState.selectedTabIndex) {
        uiState.timeRanges.forEachIndexed { index, timeRange ->
            Tab(
                selected = uiState.selectedTabIndex == index,
                onClick = { viewModel.onTimeRangeSelected(index) },
                text = { Text(timeRange.name) }
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    Text("Total Screen Time", style = MaterialTheme.typography.titleMedium)
    Text(formatMillisToHoursMinutes(uiState.totalUsage), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.height(24.dp))

    AnalyticsBarChart(apps = uiState.topApps)
}

@Composable
fun PermissionPrompt() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Permission Required", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "To show your usage statistics, ProdZen needs access to your phone's usage data.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun AnalyticsBarChart(apps: List<AppInfo>) {
    val maxUsage = apps.maxOfOrNull { it.usageTodayMillis } ?: 1L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Most Used Apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (apps.isEmpty()) {
            Text("No usage data available for this period.", style = MaterialTheme.typography.bodyMedium)
        } else {
            apps.take(5).forEach { app ->
                AnimatedUsageBar(app = app, maxUsage = maxUsage)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun AnimatedUsageBar(app: AppInfo, maxUsage: Long) {
    val usageFraction = if (maxUsage > 0) {
        (app.usageTodayMillis.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedFraction by animateFloatAsState(targetValue = usageFraction, label = "barAnimation")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(app.appName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(formatMillisToHoursMinutes(app.usageTodayMillis), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun formatMillisToHoursMinutes(millis: Long): String {
    if (millis < 60000) return "0m"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
