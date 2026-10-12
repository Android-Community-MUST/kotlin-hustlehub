package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

@Composable
fun ServiceImagePlaceholder(
    category: ServiceCategory,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
) {
    val visuals = getCategoryVisuals(category)

    Box(
        modifier = modifier.background(visuals.containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = visuals.icon,
            contentDescription = null,
            tint = visuals.iconColor,
            modifier = Modifier.size(iconSize),
        )
    }
}
