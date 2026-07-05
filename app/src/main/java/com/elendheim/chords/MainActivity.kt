package com.elendheim.chords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.elendheim.chords.ui.AppRoot
import com.elendheim.chords.ui.theme.ElendheimChordsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElendheimChordsTheme {
                AppRoot()
            }
        }
    }
}
