package must.kdroiders.hustlehub.ui.features.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import must.kdroiders.hustlehub.ui.features.home.domain.model.SearchFilters
import must.kdroiders.hustlehub.ui.features.home.domain.model.SortOrder
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceAvailability
import must.kdroiders.hustlehub.ui.features.service.domain.model.ServiceCategory

/**
 * Full-screen bottom sheet containing all filter and sort controls.
 *
 * State is held as a [draft] by the caller ([SearchViewModel.draftFilters]) so the user
 * can cancel without committing changes. [onDraftChanged] is called on every interaction.
 * [onApply] commits the draft; [onReset] clears all filters to defaults.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    draft: SearchFilters,
    onDraftChanged: (SearchFilters) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("filter_bottom_sheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            // Header row with title and reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("filter_reset_button"),
                ) {
                    Text(
                        "Reset",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // Category multi-select chips
            FilterSectionLabel("Category")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServiceCategory.entries
                    .filter { it != ServiceCategory.ALL }
                    .forEach { category ->
                        val selected = draft.categories.contains(category.name)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val updated = if (selected) {
                                    draft.categories - category.name
                                } else {
                                    draft.categories + category.name
                                }
                                onDraftChanged(draft.copy(categories = updated))
                            },
                            label = { Text(category.label) },
                            modifier = Modifier.testTag("filter_category_${category.name}"),
                        )
                    }
            }
            Spacer(Modifier.height(20.dp))

            // Minimum rating slider
            FilterSectionLabel("Min Rating: ${if (draft.minRating == 0f) "Any" else "%.1f+ stars".format(draft.minRating)}")
            Slider(
                value = draft.minRating,
                onValueChange = { onDraftChanged(draft.copy(minRating = it)) },
                valueRange = 0f..5f,
                steps = 9, // 0.5 increments
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("filter_rating_slider"),
            )
            Spacer(Modifier.height(20.dp))

            // Max price slider section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterSectionLabel("PRICE RANGE")
                Text(
                    text = if (draft.maxPrice >= 5000) "Any" else "KES 0 - ${draft.maxPrice}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Slider(
                value = draft.maxPrice.toFloat(),
                onValueChange = { onDraftChanged(draft.copy(maxPrice = it.toInt())) },
                valueRange = 0f..5000f,
                steps = 49, // KES 100 increments
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("filter_price_slider"),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("5k+", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(32.dp))

            // Availability segmented button
            FilterSectionLabel("AVAILABILITY")
            val availabilityOptions = listOf(null, ServiceAvailability.AVAILABLE, ServiceAvailability.BUSY)
            val availabilityLabels = listOf("All", "Available", "Busy")
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("filter_availability"),
            ) {
                availabilityOptions.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = draft.availability == option,
                        onClick = { onDraftChanged(draft.copy(availability = option)) },
                        shape = SegmentedButtonDefaults.itemShape(index, availabilityOptions.size),
                        label = { Text(availabilityLabels[index], fontWeight = FontWeight.SemiBold) },
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            // Sort order segmented button
            FilterSectionLabel("Sort By")
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("filter_sort"),
            ) {
                SortOrder.entries.forEachIndexed { index, sort ->
                    SegmentedButton(
                        selected = draft.sortOrder == sort,
                        onClick = { onDraftChanged(draft.copy(sortOrder = sort)) },
                        shape = SegmentedButtonDefaults.itemShape(index, SortOrder.entries.size),
                        label = { Text(sort.label) },
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Action button (Show Results)
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("filter_apply_button"),
            ) {
                Text("Show Results", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
