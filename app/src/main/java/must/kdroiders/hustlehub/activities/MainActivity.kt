package must.kdroiders.hustlehub.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import must.kdroiders.hustlehub.navigation.HustleHubNavGraph
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HustleHubTheme {
                val navController = rememberNavController()
                //SplashScreen()
                HustleHubNavGraph(navController = navController)
            }
        }
    }
}
