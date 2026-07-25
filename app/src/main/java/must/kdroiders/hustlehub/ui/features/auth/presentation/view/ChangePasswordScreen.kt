package must.kdroiders.hustlehub.ui.features.auth.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleScaffold
import must.kdroiders.hustlehub.sharedComposables.HustleTextField
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.ChangePasswordViewModel
import must.kdroiders.hustlehub.ui.features.auth.presentation.viewmodel.PasswordStrength
import must.kdroiders.hustlehub.ui.theme.HustleActiveGreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    HustleScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Secure your account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your current password to verify your identity, then choose a new strong password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            HustleTextField(
                value = uiState.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                placeholder = "Current Password",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            HustleTextField(
                value = uiState.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                placeholder = "New Password",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth(),
            )

            // Password strength indicator
            if (uiState.newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(strength = viewModel.getPasswordStrength())
            }

            Spacer(modifier = Modifier.height(16.dp))

            HustleTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = "Confirm New Password",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HustleButton(
                text = "Save Password",
                onClick = {
                    viewModel.changePassword(
                        onSuccess = {
                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                    )
                },
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (color, text, weight) = when (strength) {
        PasswordStrength.NONE -> Triple(Color.Transparent, "", 0.01f)
        PasswordStrength.WEAK -> Triple(MaterialTheme.colorScheme.error, "Weak", 0.25f)
        PasswordStrength.MEDIUM -> Triple(Color(0xFFFFB300), "Medium", 0.5f) // Amber
        PasswordStrength.STRONG -> Triple(HustleActiveGreen, "Strong", 0.75f)
        PasswordStrength.VERY_STRONG -> Triple(HustleActiveGreen, "Very Strong", 1f)
    }

    if (strength == PasswordStrength.NONE) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(4.dp)
                    .background(color, MaterialTheme.shapes.small),
            )
            if (weight < 1f) {
                Box(
                    modifier = Modifier
                        .weight(1f - weight)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
