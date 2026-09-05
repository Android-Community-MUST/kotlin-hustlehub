package must.kdroiders.hustlehub.ui.features.analytics.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.core.api.userFriendlyMessage
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import must.kdroiders.hustlehub.ui.features.analytics.domain.usecase.GetProviderAnalyticsUseCase
import javax.inject.Inject

enum class AnalyticsTab { OVERVIEW, PAYMENTS }

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val analytics: ProviderAnalyticsDto? = null,
    val selectedTab: AnalyticsTab = AnalyticsTab.OVERVIEW,
)

@HiltViewModel
class AnalyticsViewModel
    @Inject
    constructor(
        private val getProviderAnalytics: GetProviderAnalyticsUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _state = MutableStateFlow(AnalyticsUiState())
        val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

        init {
            val initialTab = savedStateHandle.get<String>("initialTab")
            if (initialTab == "PAYMENTS") {
                _state.value = _state.value.copy(selectedTab = AnalyticsTab.PAYMENTS)
            }
            loadAnalytics()
        }

        fun selectTab(tab: AnalyticsTab) {
            _state.value = _state.value.copy(selectedTab = tab)
        }

        fun loadAnalytics() {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true, error = null)
                getProviderAnalytics()
                    .onSuccess { data ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            analytics = data,
                        )
                    }.onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.userFriendlyMessage("Failed to load analytics"),
                        )
                    }
            }
        }
    }
