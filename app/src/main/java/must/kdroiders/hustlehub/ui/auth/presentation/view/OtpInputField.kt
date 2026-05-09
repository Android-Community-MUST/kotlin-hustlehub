package must.kdroiders.hustlehub.ui.auth.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalTextStyle

@Composable
fun OtpInputField(
    otpValues: List<String>,
    focusRequesters: List<FocusRequester>,
    onOtpValueChange: (index: Int, value: String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        otpValues.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { newVal ->
                    if (newVal.length <= 1 && newVal.all { it.isDigit() }) {
                        onOtpValueChange(index, newVal)
                        if (newVal.isNotEmpty() && index < 5) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .focusRequester(focusRequesters[index]),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
            )
        }
    }
}
