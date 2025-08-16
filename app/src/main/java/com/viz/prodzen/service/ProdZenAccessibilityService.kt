package com.viz.prodzen.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.viz.prodzen.InterventionActivity
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.ui.screens.focus.FocusSessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ProdZenAccessibilityService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var repository: AppRepository

    companion object {
        private var allowedPackageName: String? = null

        fun setAllowedPackage(packageName: String?) {
            allowedPackageName = packageName
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (packageName != null && packageName != applicationContext.packageName) {

                if (packageName == allowedPackageName) {
                    Log.d("ProdZenAccessibility", "ALLOWING: $packageName")
                    scope.launch {
                        kotlinx.coroutines.delay(2000) // 2 seconds delay
                        allowedPackageName = null
                    }
                    return
                }

                if (FocusSessionViewModel.isSessionActive.value) {
                    Log.d("ProdZenAccessibility", "BLOCKING (Focus Session): $packageName")
                    launchIntervention(packageName, "FOCUS_SESSION")
                    return
                }

                scope.launch {
                    val trackedApp = repository.getAppByPackageName(packageName)
                    if (trackedApp != null) {
                        // NEW: Check for App Limits first.
                        if (trackedApp.timeLimitMinutes > 0) {
                            val usageToday = repository.getAppUsageToday(packageName)
                            val limitMillis = TimeUnit.MINUTES.toMillis(trackedApp.timeLimitMinutes.toLong())
                            if (usageToday >= limitMillis) {
                                Log.d("ProdZenAccessibility", "BLOCKING (Limit Exceeded): $packageName")
                                launchIntervention(packageName, "LIMIT_EXCEEDED")
                                return@launch // Stop further checks.
                            }
                        }

                        // Then check for Pause Exercise.
                        if (trackedApp.isTracked) {
                            Log.d("ProdZenAccessibility", "BLOCKING (Pause Exercise): $packageName")
                            launchIntervention(packageName, "PAUSE_EXERCISE")
                        }
                    }
                }
            }
        }
    }
    private fun launchIntervention(packageName: String, type: String) {
        val intent = Intent(this, InterventionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            putExtra("TARGET_PACKAGE_NAME", packageName)
            putExtra("INTERVENTION_TYPE", type)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.d("ProdZenAccessibility", "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
