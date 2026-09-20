package must.kdroiders.hustlehub.ui.features.analytics.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.ProviderAnalyticsDto
import must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto.TrackViewRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AnalyticsApiService {
    @GET("analytics/me")
    suspend fun getProviderAnalytics(): ApiResponse<ProviderAnalyticsDto>

    @POST("analytics/track-view")
    suspend fun trackServiceView(
        @Body request: TrackViewRequest,
    ): Response<Unit>
}
