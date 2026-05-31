package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import must.kdroiders.hustlehub.ui.features.home.domain.model.LiveService
import must.kdroiders.hustlehub.data.model.ServiceCategory
import must.kdroiders.hustlehub.ui.features.home.domain.model.TopHustler
import javax.inject.Inject

data class HomeUiState(
    val selectedCategory: ServiceCategory = ServiceCategory.ALL,
    val searchQuery: String = "",
    val topHustlers: List<TopHustler> = emptyList(),
    val availableNow: List<LiveService> = emptyList(),
    val providerInitials: String = "JK",   // logged-in user initials for avatar
    val notificationCount: Int = 3,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    fun onCategorySelected(category: ServiceCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // TODO: replace with real API calls to GET /api/v1/discovery/feed
    private fun loadMockData() {
        _uiState.update {
            it.copy(
                topHustlers = listOf(
                    TopHustler(
                        id = "1",
                        providerName = "Wanjiku M.",
                        serviceTitle = "Braids by Wanjiku",
                        category = ServiceCategory.SALON,
                        rating = 4.9f,
                        priceLabel = "from KES 500"
                    ),
                    TopHustler(
                        id = "2",
                        providerName = "James K.",
                        serviceTitle = "Calculus 101",
                        category = ServiceCategory.TUTORING,
                        rating = 5.0f,
                        priceLabel = "per hr KES 300"
                    ),
                    TopHustler(
                        id = "3",
                        providerName = "Grace N.",
                        serviceTitle = "Graphic Design",
                        category = ServiceCategory.DESIGN,
                        rating = 4.7f,
                        priceLabel = "from KES 800"
                    )
                ),
                availableNow = listOf(
                    LiveService(
                        id = "4",
                        title = "Fast Laundry",
                        price = "KES 250",
                        location = "Hall 6, Room 102"
                    ),
                    LiveService(
                        id = "5",
                        title = "Phone Fix",
                        price = "KES 1K",
                        location = "Student Center"
                    ),
                    LiveService(
                        id = "6",
                        title = "Event Pics",
                        price = "KES 500",
                        location = "Sports Field"
                    ),
                    LiveService(
                        id = "7",
                        title = "Fresh Fruit",
                        price = "KES 50",
                        location = "Gate A"
                    )
                ),
                isLoading = false
            )
        }
    }
}
