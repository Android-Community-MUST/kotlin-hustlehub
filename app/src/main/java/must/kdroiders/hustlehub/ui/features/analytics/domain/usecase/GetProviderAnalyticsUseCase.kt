package must.kdroiders.hustlehub.ui.features.analytics.domain.usecase

import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import must.kdroiders.hustlehub.ui.features.analytics.data.repository.AnalyticsRepository
import javax.inject.Inject

class GetProviderAnalyticsUseCase
    @Inject
    constructor(
        private val repository: AnalyticsRepository,
    ) {
        suspend operator fun invoke(): Result<ProviderAnalyticsDto> = repository.getProviderAnalytics()
    }
