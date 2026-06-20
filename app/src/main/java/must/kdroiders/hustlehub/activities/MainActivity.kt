package must.kdroiders.hustlehub.activities

import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.navigation.HustleHubNav
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.LoginViewModel
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    // For older Android versions (below API 34)
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Always initialize legacy Google Sign-In as fallback
        initializeLegacyGoogleSignIn()

        val launchCredentialFlow: () -> Unit = {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    launchModernCredentialFlowWithFallback()
                }
                else -> {
                    launchLegacyGoogleSignIn()
                }
            }
        }

        setContent {
            HustleHubTheme(
                darkTheme = isSystemInDarkTheme(),
            ) {
                // Navigation 3 — pass the google sign in flow callback down
                HustleHubNav(
                    onGoogleSignInClick = launchCredentialFlow,
                )
            }
        }
    }

    private fun initializeLegacyGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            signInLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                handleLegacySignInResult(result.data)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize legacy Google Sign-In")
        }
    }

    private fun handleLegacySignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            Timber.d("Legacy GoogleSignIn returned idToken present=${!idToken.isNullOrEmpty()}, email=${account?.email}")

            if (!idToken.isNullOrEmpty()) {
                loginViewModel.signInWithGoogle(idToken, onSuccess = {
                    Timber.d("Google sign in success via Legacy SDK")
                })
            } else {
                Timber.e("Legacy GoogleSignIn returned no idToken")
            }
        } catch (e: ApiException) {
            Timber.e(e, "Legacy GoogleSignIn failed: ${e.statusCode}")
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error handling legacy GoogleSignIn result")
        }
    }

    private fun launchModernCredentialFlowWithFallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            lifecycleScope.launch {
                try {
                    launchModernCredentialFlow()
                } catch (e: Exception) {
                    Timber.w(e, "Modern credential flow failed, falling back to legacy")
                    launchLegacyGoogleSignIn()
                }
            }
        } else {
            launchLegacyGoogleSignIn()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun launchModernCredentialFlow() {
        lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(this@MainActivity)

                val googleIdOption = GetGoogleIdOption
                    .Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.google_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest
                    .Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Timber.d("Attempting modern credential flow...")

                val result = credentialManager.getCredential(
                    request = request,
                    context = this@MainActivity,
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                Timber.d("Modern CredentialManager provided idToken; sending to ViewModel")
                if (!idToken.isNullOrEmpty()) {
                    loginViewModel.signInWithGoogle(idToken, onSuccess = {
                        Timber.d("Google sign in success via CredentialManager")
                    })
                }
            } catch (e: GetCredentialException) {
                Timber.e(e, "CredentialManager GetCredentialException")
            } catch (e: GoogleIdTokenParsingException) {
                Timber.e(e, "CredentialManager GoogleIdTokenParsingException")
            } catch (e: Exception) {
                Timber.e(e, "Error during modern Google Sign-In")
            }
        }
    }

    private fun launchLegacyGoogleSignIn() {
        try {
            if (!::googleSignInClient.isInitialized || !::signInLauncher.isInitialized) {
                Timber.e("GoogleSignInClient or launcher not initialized")
                return
            }
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
            Timber.d("Launched legacy Google Sign-In")
        } catch (e: Exception) {
            Timber.e(e, "Error launching legacy Google Sign-In")
        }
    }
}
