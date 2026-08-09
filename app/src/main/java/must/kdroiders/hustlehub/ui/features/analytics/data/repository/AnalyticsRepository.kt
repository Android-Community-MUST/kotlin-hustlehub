package must.kdroiders.hustlehub.ui.features.analytics.data.repository

import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto

interface AnalyticsRepository {
    suspend fun getProviderAnalytics(): Result<ProviderAnalyticsDto>
}
