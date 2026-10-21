package com.dinhlekieulinh.diemdanh.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.dinhlekieulinh.diemdanh.ui.navigation.DinhLeKieuLinhNavGraph
import com.dinhlekieulinh.diemdanh.ui.theme.DinhLeKieuLinhTheme

class DinhLeKieuLinhMainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DinhLeKieuLinhTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DinhLeKieuLinhNavGraph(activity = this)
                }
            }
        }
    }
}
