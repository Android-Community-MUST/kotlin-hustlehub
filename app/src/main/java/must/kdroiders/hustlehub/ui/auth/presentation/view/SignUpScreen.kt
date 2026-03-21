package must.kdroiders.hustlehub.ui.auth.presentation.view

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.auth.presentation.viewmodel.SignUpViewModel
import must.kdroiders.hustlehub.ui.auth.presentation.view.components.PasswordStrengthIndicator

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Join HustleHub with your student email",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        HustleTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = "Full Name",
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            errorText = uiState.nameError,
            leadingIcon = Icons.Default.Person
        )
        Spacer(modifier = Modifier.height(16.dp))

        HustleTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = "Must Student Email",
            placeholder = "example@must.ac.ke",
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.emailError != null,
            errorText = uiState.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = Icons.Default.Email
        )
        Spacer(modifier = Modifier.height(16.dp))

        HustleTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.passwordError != null,
            errorText = uiState.passwordError,
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = Icons.Default.Lock
        )
        PasswordStrengthIndicator(strength = uiState.passwordStrength)
        Spacer(modifier = Modifier.height(16.dp))

        HustleTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = "Confirm Password",
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.confirmPasswordError != null,
            errorText = uiState.confirmPasswordError,
            isPassword = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = Icons.Default.Lock
        )
        Spacer(modifier = Modifier.height(32.dp))

        HustleButton(
            text = "Sign Up",
            onClick = viewModel::signUp,
            modifier = Modifier.fillMaxWidth(),
            loading = uiState.isLoading
        )
        
        uiState.signUpError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val annotatedString = buildAnnotatedString {
            append("Already have an account? ")
            pushStringAnnotation(tag = "login", annotation = "login")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                append("Login")
            }
            pop()
        }

        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "login", start = offset, end = offset).firstOrNull()?.let {
                    onNavigateToLogin()
                }
            }
        )
    }
}
