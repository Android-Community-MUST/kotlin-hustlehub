package must.kdroiders.hustlehub.ui.features.service.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.AvailabilityRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.CreateReviewRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.CreateServiceRequest
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ReviewResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.UpdateServiceRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ServiceApiService {
    @POST("services")
    suspend fun createService(
        @Body request: CreateServiceRequest,
    ): ApiResponse<ServiceResponse>

    @GET("services/{serviceId}")
    suspend fun getServiceById(
        @Path("serviceId") serviceId: String,
    ): ApiResponse<ServiceResponse>

    @PUT("services/{serviceId}")
    suspend fun updateService(
        @Path("serviceId") serviceId: String,
        @Body request: UpdateServiceRequest,
    ): ApiResponse<ServiceResponse>

    @DELETE("services/{serviceId}")
    suspend fun deleteService(
        @Path("serviceId") serviceId: String,
    ): ApiResponse<Unit>

    @PUT("services/{serviceId}/availability")
    suspend fun updateAvailability(
        @Path("serviceId") serviceId: String,
        @Body request: AvailabilityRequest,
    ): ApiResponse<ServiceResponse>

    @GET("services/me")
    suspend fun getMyServices(): ApiResponse<List<ServiceResponse>>

    @GET("discovery/services")
    suspend fun browseServices(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("availability") availability: String? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radiusKm") radiusKm: Double? = null,
        @Query("sortBy") sortBy: String? = null,
    ): ApiResponse<PageResponse<ServiceResponse>>

    /**
     * Full-text keyword search across service titles, descriptions, and tags.
     * Maps to GET /api/v1/discovery/search — separate from the filtered browse endpoint.
     * Results are always sorted by avgRating descending.
     */
    @GET("discovery/search")
    suspend fun searchServices(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<PageResponse<ServiceResponse>>

    /** Returns paginated reviews for a single service. Maps to GET /api/v1/services/{serviceId}/reviews. */
    @GET("services/{serviceId}/reviews")
    suspend fun getServiceReviews(
        @Path("serviceId") serviceId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): ApiResponse<PageResponse<ReviewResponse>>

    /** Submits a new review. Maps to POST /api/v1/services/{serviceId}/reviews. Returns 409 on duplicate (same user + service). */
    @POST("services/{serviceId}/reviews")
    suspend fun submitReview(
        @Path("serviceId") serviceId: String,
        @Body request: CreateReviewRequest,
    ): ApiResponse<ReviewResponse>
}
