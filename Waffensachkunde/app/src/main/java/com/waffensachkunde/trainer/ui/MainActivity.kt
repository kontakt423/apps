package com.waffensachkunde.trainer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.waffensachkunde.trainer.ui.common.AppNavHost
import com.waffensachkunde.trainer.ui.theme.WaffensachkundeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaffensachkundeTheme {
                AppNavHost()
            }
        }
    }
}
