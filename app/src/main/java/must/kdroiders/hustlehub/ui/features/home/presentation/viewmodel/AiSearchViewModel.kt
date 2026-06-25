package must.kdroiders.hustlehub.ui.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchMatch
import must.kdroiders.hustlehub.ui.features.home.data.remote.AiSearchRequest
import must.kdroiders.hustlehub.ui.features.home.data.remote.QueryUnderstanding
import must.kdroiders.hustlehub.ui.features.home.domain.usecase.AiSearchUseCase
import javax.inject.Inject

data class AiSearchUiState(
    val query: String = "",
    val matches: List<AiSearchMatch> = emptyList(),
    val queryUnderstanding: QueryUnderstanding? = null,
    val isLoading: Boolean = false,
    /**
     * True when the backend fell back to keyword search (Gemini unavailable).
     * Derived heuristic: understanding has no category/location and service == raw query.
     */
    val usedFallback: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AiSearchViewModel
    @Inject
    constructor(
        private val aiSearchUseCase: AiSearchUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AiSearchUiState())
        val uiState: StateFlow<AiSearchUiState> = _uiState.asStateFlow()

        fun onQueryChanged(query: String) {
            _uiState.update { it.copy(query = query, error = null) }
        }

        fun onSearch(userLocation: AiSearchRequest.UserLocationDto? = null) {
            val query = _uiState.value.query.trim()
            if (query.isBlank()) return

            _uiState.update { it.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                aiSearchUseCase(query = query, userLocation = userLocation)
                    .onSuccess { response ->
                        val understanding = response.queryUnderstanding
                        // Heuristic: if Gemini returned an understanding where service == raw
                        // query and no category/location was parsed, the backend likely fell back.
                        val likelyFallback = understanding.category == null &&
                            understanding.location == null &&
                            understanding.service?.equals(query, ignoreCase = true) == true

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                matches = response.matches,
                                queryUnderstanding = understanding,
                                usedFallback = likelyFallback,
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                    }
            }
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }
    }
