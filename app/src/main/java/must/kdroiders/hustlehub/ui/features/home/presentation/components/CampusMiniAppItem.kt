package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

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
        description = "Thrift & Wear",
        url = "https://example.com",
        coverPhotoUrl = "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400",
        iconUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=100",
    ),
    CampusMiniAppItem(
        appId = "must-canteen",
        title = "MUST Canteen",
        description = "Food & Drinks",
        url = "file:///android_asset/miniapps/canteen/index.html",
        coverPhotoUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=400",
        icon = Icons.Default.Restaurant,
    ),
    CampusMiniAppItem(
        appId = "mabby-food",
        title = "Mabby",
        description = "Meals & Treats",
        url = "https://mabby.co.ke",
        coverPhotoUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400",
        icon = Icons.Default.Cake,
    ),
    CampusMiniAppItem(
        appId = "must-events",
        title = "Campus Passes",
        description = "Tickets & Entry",
        url = "https://events.must.ac.ke",
        coverPhotoUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400",
        icon = Icons.Default.ConfirmationNumber,
    ),
)

@Composable
fun CampusMiniAppsRow(
    onMiniAppClick: (appId: String, title: String, url: String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<CampusMiniAppItem> = defaultCampusMiniApps,
    onApplyClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Campus Mini-Apps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.appId }) { item ->
                Card(
                    modifier = Modifier
                        .width(136.dp)
                        .clickable {
                            onMiniAppClick(item.appId, item.title, item.url)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            if (!item.coverPhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.coverPhotoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                ),
                                            ),
                                        ),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .offset(y = (-18).dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
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
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                                .offset(y = (-10).dp),
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            item(key = "apply_miniapp") {
                Card(
                    modifier = Modifier
                        .width(136.dp)
                        .clickable { onApplyClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        ),
                                    ),
                                ),
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .offset(y = (-18).dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddBusiness,
                                        contentDescription = "Host Your App",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                                .offset(y = (-10).dp),
                        ) {
                            Text(
                                text = "Host Your App",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Launch on campus",
                                style = MaterialTheme.typography.labelSmall,
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
