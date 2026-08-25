package com.example.spellveda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import com.example.spellveda.data.SpellingBeeDatabase
import com.example.spellveda.ui.navigation.SpellVedaNavHost
import com.example.spellveda.ui.theme.SpellVedaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpellVedaTheme {
                // Initialize database
                SpellingBeeDatabase.getDatabase(this)
                
                SpellVedaNavHost()
            }
        }
    }
}
