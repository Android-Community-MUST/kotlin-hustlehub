package must.kdroiders.hustlehub.ui.features.auth.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.auth.presentation.view.components.PasswordStrengthIndicator
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (email: String) -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    signUpViewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by signUpViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.signUpError) {
        uiState.signUpError?.let { err ->
            snackbarHostState.showSnackbar(err)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = "Join HustleHub with your student email",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp),
            )

            HustleTextField(
                value = uiState.name,
                onValueChange = signUpViewModel::onNameChanged,
                label = "Full Name",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                errorText = uiState.nameError,
                leadingIcon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))

            HustleTextField(
                value = uiState.email,
                onValueChange = signUpViewModel::onEmailChanged,
                label = "Must Student Email",
                placeholder = "example@students.must.ac.ke",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.emailError != null,
                errorText = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                leadingIcon = Icons.Default.Email,
            )
            Spacer(modifier = Modifier.height(16.dp))

            HustleTextField(
                value = uiState.password,
                onValueChange = signUpViewModel::onPasswordChanged,
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.passwordError != null,
                errorText = uiState.passwordError,
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                leadingIcon = Icons.Default.Lock,
            )
            if (uiState.password.isNotEmpty()) {
                PasswordStrengthIndicator(strength = uiState.passwordStrength)
            }
            Spacer(modifier = Modifier.height(16.dp))

            HustleTextField(
                value = uiState.confirmPassword,
                onValueChange = signUpViewModel::onConfirmPasswordChanged,
                label = "Confirm Password",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.confirmPasswordError != null,
                errorText = uiState.confirmPasswordError,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = Icons.Default.Lock,
            )
            Spacer(modifier = Modifier.height(32.dp))

            HustleButton(
                text = "Sign Up",
                onClick = { signUpViewModel.signUp(onSignUpSuccess) },
                modifier = Modifier.fillMaxWidth(),
                loading = uiState.isLoading,
            )

            uiState.signUpError?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(28.dp))

            // Google sign-in button
            HustleButton(
                text = "Continue with Google",
                onClick = { onGoogleSignInClick() },
                variant = HustleButtonVariant.Outlined,
                painter = painterResource(id = R.drawable.google),
                iconSize = 22.dp,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(36.dp))

            val annotatedString = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    ),
                ) { append("Already have an account?  ") }
                pushStringAnnotation(tag = "login", annotation = "login")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    ),
                ) { append("Login") }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "login", start = offset, end = offset).firstOrNull()?.let {
                        onNavigateToLogin()
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
