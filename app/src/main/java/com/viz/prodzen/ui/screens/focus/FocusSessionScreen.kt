package com.viz.prodzen.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSessionScreen(
    navController: NavController,
    viewModel: FocusSessionViewModel = hiltViewModel()
) {
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnimation")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Focus Session") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
                CircularProgress(progress = animatedProgress)
                Text(
                    text = timeRemaining,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (isSessionActive) {
                Button(
                    onClick = { viewModel.stopSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                ) {
                    Text("Stop Session")
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text("Set Duration", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = viewModel.durationMinutes.collectAsState().value,
                        onValueChange = { viewModel.setDuration(it) },
                        valueRange = 1f..120f,
                        steps = 118
                    )
                    Text("${viewModel.durationMinutes.collectAsState().value.toInt()} minutes", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.startSession() }, modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)) {
                        Text("Start Session")
                    }
                }
            }
        }
    }
}

@Composable
fun CircularProgress(progress: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 12.dp.toPx()
        // Background circle
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
        // Foreground progress arc
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = 360 * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

