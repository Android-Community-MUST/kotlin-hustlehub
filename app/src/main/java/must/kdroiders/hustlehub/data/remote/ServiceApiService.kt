package must.kdroiders.hustlehub.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.data.remote.dto.AvailabilityRequest
import must.kdroiders.hustlehub.data.remote.dto.CreateServiceRequest
import must.kdroiders.hustlehub.data.remote.dto.ServiceResponse
import must.kdroiders.hustlehub.data.remote.dto.UpdateServiceRequest
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
    ): ApiResponse<PageResponse<ServiceResponse>>
}
