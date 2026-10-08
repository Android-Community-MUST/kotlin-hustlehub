package must.kdroiders.hustlehub.activities

import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.core.notification.NotificationHelper
import must.kdroiders.hustlehub.core.review.AppReviewManager
import must.kdroiders.hustlehub.datastore.AppTheme
import must.kdroiders.hustlehub.navigation.DeepLinkAction
import must.kdroiders.hustlehub.navigation.HustleHubNav
import must.kdroiders.hustlehub.navigation.MainNavigationViewModel
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.LoginViewModel
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import must.kdroiders.hustlehub.ui.theme.HustleDarkBackground
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme
import must.kdroiders.hustlehub.ui.theme.HustleLightBackground
import must.kdroiders.hustlehub.ui.theme.ThemeViewModel
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appReviewManager: AppReviewManager

    private var locationJob: kotlinx.coroutines.Job? = null

    private val loginViewModel: LoginViewModel by viewModels()
    private val mainNavigationViewModel: MainNavigationViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    // For older Android versions (below API 34)
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        Timber.d("POST_NOTIFICATIONS permission granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register notification channel early — safe to call multiple times (OS is idempotent)
        NotificationHelper.createChannel(this)

        // Request notifications permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }

        // Always initialize legacy Google Sign-In as fallback
        initializeLegacyGoogleSignIn()

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                appReviewManager.recordAppOpen()
            }
        }

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
            val appTheme by themeViewModel.theme.collectAsState()
            val isDark = when (appTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = HustleLightBackground.toArgb(),
                        darkScrim = HustleDarkBackground.toArgb(),
                    ) { isDark },
                )
                onDispose {}
            }

            HustleHubTheme(
                darkTheme = isDark,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Navigation 3 — pass the google sign in flow callback down
                    HustleHubNav(
                        onGoogleSignInClick = launchCredentialFlow,
                    )
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        val scheme = uri.scheme ?: return
        val host = uri.host ?: return

        val action: DeepLinkAction? = when {
            scheme == "https" && (host == "hustlehub-8367.web.app" || host == "hustlehub-8367.firebaseapp.com") -> {
                val segments = uri.pathSegments
                val type = segments.getOrNull(0)
                val id = segments.getOrNull(1)
                when {
                    type == "profile" && !id.isNullOrBlank() -> DeepLinkAction.OpenProviderProfile(id)
                    type == "service" && !id.isNullOrBlank() -> DeepLinkAction.OpenServiceDetail(id)
                    else -> null
                }
            }
            scheme == "hustlehub" -> {
                val lastSegment = uri.lastPathSegment
                when (host) {
                    "chat" -> {
                        val conversationId = lastSegment ?: uri.getQueryParameter("conversationId")
                        if (!conversationId.isNullOrBlank()) DeepLinkAction.OpenChat(conversationId) else null
                    }
                    "service" -> {
                        if (!lastSegment.isNullOrBlank()) DeepLinkAction.OpenServiceDetail(lastSegment) else null
                    }
                    "profile" -> {
                        if (!lastSegment.isNullOrBlank()) DeepLinkAction.OpenProviderProfile(lastSegment) else null
                    }
                    "review" -> {
                        val serviceId = lastSegment
                        val providerId = uri.getQueryParameter("providerId") ?: ""
                        if (!serviceId.isNullOrBlank()) DeepLinkAction.OpenWriteReview(serviceId, providerId) else null
                    }
                    "notifications" -> DeepLinkAction.OpenNotifications
                    "app" -> {
                        when {
                            uri.path?.contains("chat") == true -> {
                                val conversationId = uri.getQueryParameter("conversationId")
                                if (!conversationId.isNullOrBlank()) DeepLinkAction.OpenChat(conversationId) else null
                            }
                            uri.path?.contains("profile") == true -> DeepLinkAction.OpenProfile
                            uri.path?.contains("inquiries") == true -> DeepLinkAction.OpenChatList
                            else -> null
                        }
                    }
                    else -> null
                }
            }
            else -> null
        }

        if (action != null) {
            mainNavigationViewModel.triggerDeepLink(action)
        } else {
            Timber.w("Invalid or unhandled deep link URI: $uri")
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = lifecycleScope.launch {
            while (isActive) {
                updateLocationIfPermitted()
                kotlinx.coroutines.delay(5 * 60 * 1000L) // 5 minutes
            }
        }
    }

    private fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun updateLocationIfPermitted() {
        if (com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser == null
        ) {
            return
        }
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lifecycleScope.launch {
                        userRepository
                            .updateUserLocation(location.latitude, location.longitude)
                            .onSuccess {
                                Timber.d("Successfully updated location to backend: lat=${location.latitude}, lng=${location.longitude}")
                            }.onFailure { e ->
                                Timber.e(e, "Failed to update location to backend")
                            }
                    }
                }
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

            Timber.d("Legacy GoogleSignIn returned idToken present=${!idToken.isNullOrEmpty()}")

            if (!idToken.isNullOrEmpty()) {
                loginViewModel.signInWithGoogle(idToken, onSuccess = {
                    Timber.d("Google sign in success via Legacy SDK")
                })
            } else {
                Timber.e("Legacy GoogleSignIn returned no idToken")
                loginViewModel.setErrorMessage("Google Sign-In failed: No ID token returned.")
            }
        } catch (e: ApiException) {
            val errorMsg = when (e.statusCode) {
                CommonStatusCodes.DEVELOPER_ERROR -> "Google Sign-In Developer Error (10): Keystore SHA-1 fingerprint is not registered in Firebase Console."
                CommonStatusCodes.SIGN_IN_REQUIRED -> "Google Sign-In required. Please select an account."
                CommonStatusCodes.NETWORK_ERROR -> "Google Sign-In failed: Network error. Check your connection."
                12500 -> "Google Sign-In failed (12500): Configuration mismatch in Google Play Services."
                else -> "Google Sign-In failed (${e.statusCode}): ${e.message}"
            }
            Timber.e(e, "Legacy GoogleSignIn failed: ${e.statusCode} - $errorMsg")
            loginViewModel.setErrorMessage(errorMsg)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error handling legacy GoogleSignIn result")
            loginViewModel.setErrorMessage("Google Sign-In error: ${e.localizedMessage}")
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
                loginViewModel.setErrorMessage("Google Sign-In failed: ${e.message}")
            } catch (e: GoogleIdTokenParsingException) {
                Timber.e(e, "CredentialManager GoogleIdTokenParsingException")
                loginViewModel.setErrorMessage("Google Sign-In failed to parse token.")
            } catch (e: Exception) {
                Timber.e(e, "Error during modern Google Sign-In")
                loginViewModel.setErrorMessage("Google Sign-In error: ${e.localizedMessage}")
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
