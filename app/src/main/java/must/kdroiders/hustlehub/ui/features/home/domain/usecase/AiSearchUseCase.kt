package must.kdroiders.hustlehub.ui.features.home.domain.usecase

import must.kdroiders.hustlehub.data.remote.AiSearchRequest
import must.kdroiders.hustlehub.data.remote.AiSearchResponse
import must.kdroiders.hustlehub.ui.features.home.domain.repository.AiSearchRepository
import javax.inject.Inject

class AiSearchUseCase
    @Inject
    constructor(
        private val repository: AiSearchRepository,
    ) {
        suspend operator fun invoke(
            query: String,
            userLocation: AiSearchRequest.UserLocationDto? = null,
            maxResults: Int = 10,
        ): Result<AiSearchResponse> = repository.aiSearch(query, userLocation, maxResults)
    }
