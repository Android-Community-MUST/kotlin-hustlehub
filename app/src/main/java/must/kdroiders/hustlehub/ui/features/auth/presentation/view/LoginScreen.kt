package must.kdroiders.hustlehub.ui.features.auth.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    prefilledEmail: String = "",
    onLoginSuccess: (hasProfile: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToEmailVerification: (email: String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
        }
    }

    // Prefill email if passed from navigation (e.g. after registration or email verification)
    LaunchedEffect(prefilledEmail) {
        if (prefilledEmail.isNotEmpty()) {
            loginViewModel.onEmailChange(prefilledEmail)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Decorative background
        LoginBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(72.dp))

            // Logo
            val isDarkTheme = isSystemInDarkTheme()
            val logoResId = if (isDarkTheme) R.drawable.dark_logo else R.drawable.light_logo
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "HustleHub Logo",
                modifier = Modifier.size(92.dp),
            )

            Spacer(Modifier.height(16.dp))

            // Two-tone wordmark: "Hustle" (onBackground) + "Hub" (primary)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)) {
                        append("Hustle")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("Hub")
                    }
                },
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Login to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(Modifier.height(36.dp))

            // Email field — placeholder-only, no floating label
            HustleTextField(
                value = uiState.email,
                onValueChange = { loginViewModel.onEmailChange(it) },
                placeholder = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // Password field
            HustleTextField(
                value = uiState.password,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                placeholder = "Password",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            // Forgot password — right-aligned
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text(
                        text = "Forgot password?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Error message
            uiState.errorMessage?.let { err ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Login primary button
            HustleButton(
                text = "Login",
                onClick = {
                    loginViewModel.login(
                        onSuccess = { hasProfile -> onLoginSuccess(hasProfile) },
                        onEmailNotVerified = onNavigateToEmailVerification,
                    )
                },
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(28.dp))

            // OR divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            Spacer(Modifier.height(28.dp))

            // Google sign-in — Outlined HustleButton with Google painter
            HustleButton(
                text = "Continue with Google",
                onClick = { onGoogleSignInClick() },
                variant = HustleButtonVariant.Outlined,
                painter = painterResource(id = R.drawable.google),
                iconSize = 22.dp,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(36.dp))

            // Sign up link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "New here?  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToSignUp() },
                )
            }

            Spacer(Modifier.height(48.dp))
        }

        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Reset Password") },
                text = {
                    Text(
                        "Enter your student email address to receive a password reset link.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showForgotPasswordDialog = false
                            loginViewModel.sendPasswordResetEmail(
                                email = uiState.email,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Password reset link sent to your email")
                                    }
                                },
                                onError = { err ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(err)
                                    }
                                },
                            )
                        },
                    ) {
                        Text("Send Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

// Background: themed blobs, bottom wave, and dot grid
@Composable
private fun LoginBackground() {
    val blobColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    val dotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)
    val bgColor = MaterialTheme.colorScheme.background
    val density = LocalDensity.current

    Canvas(modifier = Modifier.fillMaxSize().clearAndSetSemantics {}) {
        val w = size.width
        val h = size.height

        drawRect(color = bgColor)

        // Top-left blob
        drawCircle(color = blobColor, radius = w * 0.32f, center = Offset(-w * 0.12f, h * 0.07f))

        // Bottom wave — gentle S-curve, C1-continuous, matches design reference
        val wavePath = Path().apply {
            moveTo(0f, h * 0.88f)
            // Curve 1: horizontal exit → gentle trough
            cubicTo(
                w * 0.12f,
                h * 0.88f,
                w * 0.32f,
                h * 0.94f,
                w * 0.45f,
                h * 0.92f,
            )
            // Curve 2: C1-continuous (reflected control) → rises to right end
            cubicTo(
                w * 0.58f,
                h * 0.90f,
                w * 0.85f,
                h * 0.84f,
                w,
                h * 0.85f,
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(wavePath, blobColor)

        // Top-right dot grid (5 rows x 6 cols)
        val dotR = 3.0f
        val dotGap = 18f
        val gx = w - 6 * dotGap - 20f
        val gy = with(density) { 48.dp.toPx() }
        repeat(5) { row ->
            repeat(6) { col ->
                drawCircle(
                    color = dotColor,
                    radius = dotR,
                    center = Offset(gx + col * dotGap, gy + row * dotGap),
                )
            }
        }
    }
}
