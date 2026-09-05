package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.R
import must.kdroiders.hustlehub.sharedComposables.HustleSearchBar

/**
 * Non-editable search bar displayed on the Home/Discovery screen using shared [HustleSearchBar].
 */
@Composable
fun HomeSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onAiSearchClick: () -> Unit = {},
) {
    HustleSearchBar(
        query = query,
        onQueryChanged = onQueryChanged,
        placeholder = stringResource(R.string.search_services_hint),
        readOnly = true,
        onSearchClick = onSearchClick,
        modifier = modifier,
        trailingIcon = {
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                            ),
                        ),
                    ).clickable(
                        onClick = onAiSearchClick,
                        role = Role.Button,
                    ).padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("home_ai_search_button"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ai_sparkle),
                    contentDescription = stringResource(R.string.cd_ai_search),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.search_ai_chip_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                )
            }
        },
    )
}
