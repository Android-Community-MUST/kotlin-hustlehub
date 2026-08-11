package must.kdroiders.hustlehub.ui.features.analytics.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import retrofit2.http.GET

interface AnalyticsApiService {
    @GET("analytics/me")
    suspend fun getProviderAnalytics(): ApiResponse<ProviderAnalyticsDto>
}
