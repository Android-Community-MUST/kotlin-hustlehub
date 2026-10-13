package must.kdroiders.hustlehub.sharedComposables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.MiscellaneousServices
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory
import must.kdroiders.hustlehub.ui.theme.CategoryNeonAmber
import must.kdroiders.hustlehub.ui.theme.CategoryNeonBlue
import must.kdroiders.hustlehub.ui.theme.CategoryNeonBrightCyan
import must.kdroiders.hustlehub.ui.theme.CategoryNeonCoral
import must.kdroiders.hustlehub.ui.theme.CategoryNeonCyan
import must.kdroiders.hustlehub.ui.theme.CategoryNeonDefault
import must.kdroiders.hustlehub.ui.theme.CategoryNeonGreen
import must.kdroiders.hustlehub.ui.theme.CategoryNeonIndigo
import must.kdroiders.hustlehub.ui.theme.CategoryNeonLime
import must.kdroiders.hustlehub.ui.theme.CategoryNeonMint
import must.kdroiders.hustlehub.ui.theme.CategoryNeonOrange
import must.kdroiders.hustlehub.ui.theme.CategoryNeonPink
import must.kdroiders.hustlehub.ui.theme.CategoryNeonPurple
import must.kdroiders.hustlehub.ui.theme.CategoryNeonRose
import must.kdroiders.hustlehub.ui.theme.CategoryNeonTeal
import must.kdroiders.hustlehub.ui.theme.CategoryNeonViolet
import must.kdroiders.hustlehub.ui.theme.CategoryNeonWarmAmber
import must.kdroiders.hustlehub.ui.theme.CategoryNeonYellow

data class CategoryVisuals(
    val icon: ImageVector,
    val neonColor: Color,
    val containerColor: Color,
    val iconColor: Color,
)

fun getCategoryIconAndColor(category: ServiceCategory): Pair<ImageVector, Color> {
    return when (category) {
        ServiceCategory.TECH -> Icons.Outlined.Computer to CategoryNeonBrightCyan
        ServiceCategory.PRINTING -> Icons.Outlined.Print to CategoryNeonIndigo
        ServiceCategory.DESIGN -> Icons.Outlined.Palette to CategoryNeonViolet
        ServiceCategory.TUTORING -> Icons.Outlined.School to CategoryNeonGreen
        ServiceCategory.PHOTOGRAPHY -> Icons.Outlined.PhotoCamera to CategoryNeonPink
        ServiceCategory.RENTALS -> Icons.Outlined.DirectionsCar to CategoryNeonTeal
        ServiceCategory.GAS_CYLINDER -> Icons.Outlined.LocalGasStation to CategoryNeonOrange
        ServiceCategory.FOOD -> Icons.Outlined.Restaurant to CategoryNeonAmber
        ServiceCategory.TUCK_SHOP -> Icons.Outlined.Storefront to CategoryNeonWarmAmber
        ServiceCategory.DELIVERY -> Icons.Outlined.TwoWheeler to CategoryNeonYellow
        ServiceCategory.SALON -> Icons.Outlined.ContentCut to CategoryNeonPurple
        ServiceCategory.BEAUTY -> Icons.Outlined.Spa to CategoryNeonRose
        ServiceCategory.FASHION -> Icons.Outlined.Checkroom to CategoryNeonBlue
        ServiceCategory.DECOR -> Icons.Outlined.Weekend to CategoryNeonGreen
        ServiceCategory.CLEANING -> Icons.Outlined.CleaningServices to CategoryNeonMint
        ServiceCategory.LAUNDRY -> Icons.Outlined.LocalLaundryService to CategoryNeonCyan
        ServiceCategory.MARKETPLACE -> Icons.Outlined.ShoppingBag to CategoryNeonCoral
        ServiceCategory.EVENTS -> Icons.Outlined.Celebration to CategoryNeonLime
        ServiceCategory.ALL -> Icons.Outlined.Category to CategoryNeonDefault
        ServiceCategory.OTHER -> Icons.Outlined.MiscellaneousServices to CategoryNeonDefault
    }
}

@Composable
@ReadOnlyComposable
fun getCategoryVisuals(category: ServiceCategory): CategoryVisuals {
    val (icon, neonColor) = getCategoryIconAndColor(category)
    return CategoryVisuals(
        icon = icon,
        neonColor = neonColor,
        containerColor = neonColor.copy(alpha = 0.16f),
        iconColor = neonColor,
    )
}
