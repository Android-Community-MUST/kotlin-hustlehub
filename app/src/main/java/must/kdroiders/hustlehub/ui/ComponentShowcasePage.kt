package must.kdroiders.hustlehub.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.sharedComposables.*
import must.kdroiders.hustlehub.ui.theme.HustleHubTheme

@Composable
fun ComponentShowcasePage(
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Shared UI Components Showcase",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // HustleButton Section
        item {
            SectionTitle("HustleButton")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HustleButton(text = "Primary Button", onClick = {})
                HustleButton(text = "Secondary Button", onClick = {}, variant = HustleButtonVariant.Secondary)
                HustleButton(text = "Outlined Button", onClick = {}, variant = HustleButtonVariant.Outlined)
                HustleButton(text = "With Icon", onClick = {}, icon = Icons.Default.Add)
                HustleButton(text = "Loading State", onClick = {}, loading = true)
                HustleButton(text = "Disabled State", onClick = {}, enabled = false)
            }
        }

        // HustleTextField Section
        item {
            SectionTitle("HustleTextField")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HustleTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = "Default TextField",
                    leadingIcon = Icons.Default.Email
                )
                HustleTextField(
                    value = "Invalid Data",
                    onValueChange = {},
                    label = "Error State",
                    isError = true,
                    errorText = "This field is required"
                )
                HustleTextField(
                    value = "Valid Data",
                    onValueChange = {},
                    label = "Success State",
                    isSuccess = true
                )
                HustleTextField(
                    value = "secret123",
                    onValueChange = {},
                    label = "Password Field",
                    isPassword = true
                )
            }
        }

        // HustleCard Section
        item {
            SectionTitle("HustleCard")
            HustleCard(onClick = {}) {
                Column {
                    Text(text = "This is a HustleCard", style = MaterialTheme.typography.titleMedium)
                    Text(text = "You can click it!", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // LoadingIndicator Section
        item {
            SectionTitle("LoadingIndicator")
            Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                LoadingIndicator()
            }
        }

        // ErrorView Section
        item {
            SectionTitle("ErrorView")
            Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                ErrorView(
                    message = "Failed to load data from the server.",
                    onRetry = {}
                )
            }
        }

        // EmptyStateView Section
        item {
            SectionTitle("EmptyStateView")
            Box(modifier = Modifier.height(400.dp).fillMaxWidth()) {
                EmptyStateView(
                    title = "Your bag is empty",
                    description = "Browse our catalog to find items you love.",
                    action = {
                        HustleButton(text = "Start Shopping", onClick = {})
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentShowcasePagePreview() {
    HustleHubTheme {
        ComponentShowcasePage()
    }
}
