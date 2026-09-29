package must.kdroiders.hustlehub.ui.features.analytics.data.repository

import must.kdroiders.hustlehub.ui.features.analytics.data.remote.AnalyticsApiService
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.TrackViewRequest
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

        override suspend fun trackServiceView(serviceId: String): Result<Unit> =
            runCatching {
                val response = api.trackServiceView(TrackViewRequest(serviceId))
                if (!response.isSuccessful) {
                    throw IllegalStateException("Failed to track view: ${response.code()}")
                }
            }
    }
