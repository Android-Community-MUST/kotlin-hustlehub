package must.kdroiders.hustlehub.ui.features.profile.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleButton
import must.kdroiders.hustlehub.sharedComposables.HustleButtonVariant
import must.kdroiders.hustlehub.sharedComposables.HustleCard
import must.kdroiders.hustlehub.sharedComposables.HustleCardVariant

@Composable
fun ProviderOnboardingCard(
    onCreateServiceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HustleCard(
        variant = HustleCardVariant.Glass,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.profile_provider_cta_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading () }
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.profile_provider_cta_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(Modifier.height(18.dp))

            HustleButton(
                text = stringResource(R.string.profile_provider_cta_action),
                onClick = onCreateServiceClick,
                variant = HustleButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
