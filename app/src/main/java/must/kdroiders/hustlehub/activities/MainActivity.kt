package must.kdroiders.hustlehub.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import must.kdroiders.hustlehub.navigation.HustleHubNav
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HustleHubTheme {
                // Navigation 3 — no NavController needed at the activity level.
                HustleHubNav()
            }
        }
    }
}
