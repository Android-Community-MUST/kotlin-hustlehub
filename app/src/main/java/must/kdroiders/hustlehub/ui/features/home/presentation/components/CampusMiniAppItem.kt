package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Data item representing a discoverable campus mini-app.
 */
data class CampusMiniAppItem(
    val appId: String,
    val title: String,
    val description: String,
    val url: String,
    val icon: ImageVector? = null,
    val iconUrl: String? = null,
    val coverPhotoUrl: String? = null,
)

val defaultCampusMiniApps = listOf(
    CampusMiniAppItem(
        appId = "thrift-drip",
        title = "MUST Drip & Fits",
        description = "Student Thrift & Wear",
        url = "https://example.com",
        coverPhotoUrl = "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400", // Sample clothes cover
        iconUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=100", // Sample logo
    ),
    CampusMiniAppItem(
        appId = "must-canteen",
        title = "MUST Canteen",
        description = "Meals & Drinks",
        url = "file:///android_asset/miniapps/canteen/index.html",
        icon = Icons.Default.Restaurant,
    ),
    CampusMiniAppItem(
        appId = "mabby-food",
        title = "Mabby",
        description = "Tasty Food",
        url = "https://mabby.co.ke",
        icon = Icons.Default.Cake,
    ),
    CampusMiniAppItem(
        appId = "must-events",
        title = "Campus Passes",
        description = "Tickets & Entry",
        url = "https://events.must.ac.ke",
        icon = Icons.Default.ConfirmationNumber,
    ),
)

/**
 * Horizontal row displaying quick-access campus mini-apps on the Home discovery tab.
 */
@Composable
fun CampusMiniAppsRow(
    onMiniAppClick: (appId: String, title: String, url: String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<CampusMiniAppItem> = defaultCampusMiniApps,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Campus Mini-Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Instant services & campus tools",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.appId }) { item ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onMiniAppClick(item.appId, item.title, item.url) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column {
                        if (!item.coverPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = item.coverPhotoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(75.dp),
                            )
                        }

                        Column(
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (!item.iconUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = item.iconUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Icon(
                                        imageVector = item.icon ?: Icons.Default.Storefront,
                                        contentDescription = item.title,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
