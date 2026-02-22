package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

private val TextFieldShape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HustleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    isSuccess: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    placeholder: String? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation = if (isPassword && !passwordVisible)
        PasswordVisualTransformation() else VisualTransformation.None

    val successColor = MaterialTheme.colorScheme.tertiary
    val motionScheme = MaterialTheme.motionScheme

    val focusedBorderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isSuccess -> successColor
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "focusedBorder"
    )

    val unfocusedBorderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            isSuccess -> successColor.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "unfocusedBorder"
    )

    val actualTrailingIcon: @Composable (() -> Unit)? = when {
        isPassword -> ({
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        })

        isSuccess -> ({
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Valid",
                modifier = Modifier.size(20.dp),
                tint = successColor
            )
        })

        trailingIcon != null -> trailingIcon
        else -> null
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            placeholder = placeholder?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            isError = isError,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            isError -> MaterialTheme.colorScheme.error
                            isSuccess -> successColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            },
            trailingIcon = actualTrailingIcon,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            shape = TextFieldShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = focusedBorderColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )

        // Animated helper / error message
        AnimatedVisibility(
            visible = isError && errorText != null,
            enter = expandVertically(motionScheme.fastSpatialSpec()) + fadeIn(motionScheme.fastEffectsSpec()),
            exit = shrinkVertically(motionScheme.fastSpatialSpec()) + fadeOut(motionScheme.fastEffectsSpec())
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HustleTextFieldPreview() {
    HustleHubTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            HustleTextField(
                value = "",
                onValueChange = {},
                label = "Username",
                leadingIcon = Icons.Default.Person,
                placeholder = "Enter your username"
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            HustleTextField(
                value = "bad@email",
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Default.Email,
                isError = true,
                errorText = "Please enter a valid email address"
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            HustleTextField(
                value = "valid@email.com",
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Default.Email,
                isSuccess = true
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            HustleTextField(
                value = "password123",
                onValueChange = {},
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )
        }
    }
}
