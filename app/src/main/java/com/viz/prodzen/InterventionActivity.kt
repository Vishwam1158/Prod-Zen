package com.viz.prodzen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.viz.prodzen.service.ProdZenAccessibilityService
import com.viz.prodzen.ui.screens.intervention.InterventionScreen
import com.viz.prodzen.ui.theme.ProdZenTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterventionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPackageName = intent.getStringExtra("TARGET_PACKAGE_NAME")
        val interventionType = intent.getStringExtra("INTERVENTION_TYPE") ?: "PAUSE_EXERCISE"

        setContent {
            ProdZenTheme {
                InterventionScreen(
                    interventionType = interventionType,
                    onClose = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onContinue = {
                        ProdZenAccessibilityService.setAllowedPackage(targetPackageName)

                        targetPackageName?.let {
                            val launchIntent = packageManager.getLaunchIntentForPackage(it)
                            if (launchIntent != null) {
                                startActivity(launchIntent)
                            }
                        }
                        finish()
                    }
                )
            }
        }
        overridePendingTransition(R.anim.fade_in, R.anim.stay)
    }
}

