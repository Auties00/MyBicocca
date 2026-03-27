package it.attendance100.mybicocca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import it.attendance100.mybicocca.ui.navigation.MyBicoccaNavHost
import it.attendance100.mybicocca.ui.theme.MyBicoccaTheme
import it.attendance100.mybicocca.util.ProvideHapticManager

@AndroidEntryPoint
class MyBicoccaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyBicoccaTheme {
                ProvideHapticManager {
                    MyBicoccaNavHost()
                }
            }
        }
    }
}
