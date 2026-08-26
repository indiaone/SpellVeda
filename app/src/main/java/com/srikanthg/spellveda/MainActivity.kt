package com.srikanthg.spellveda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import com.srikanthg.spellveda.data.SpellingBeeDatabase
import com.srikanthg.spellveda.ui.navigation.SpellVedaNavHost
import com.srikanthg.spellveda.ui.theme.SpellVedaTheme

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
