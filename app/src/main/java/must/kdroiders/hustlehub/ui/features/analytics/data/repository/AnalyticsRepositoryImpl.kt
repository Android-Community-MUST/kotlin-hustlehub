package must.kdroiders.hustlehub.ui.features.analytics.data.repository

import must.kdroiders.hustlehub.ui.features.analytics.data.remote.AnalyticsApiService
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import javax.inject.Inject

class AnalyticsRepositoryImpl
    @Inject
    constructor(
        private val api: AnalyticsApiService,
    ) : AnalyticsRepository {
        override suspend fun getProviderAnalytics(): Result<ProviderAnalyticsDto> =
            runCatching {
                val response = api.getProviderAnalytics()
                response.data ?: throw IllegalStateException("No analytics data returned")
            }
    }
