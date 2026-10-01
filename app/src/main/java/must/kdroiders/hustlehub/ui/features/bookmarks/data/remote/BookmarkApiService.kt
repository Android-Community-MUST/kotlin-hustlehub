package must.kdroiders.hustlehub.ui.features.bookmarks.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.service.data.remote.dto.ServiceResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BookmarkApiService {
    @GET("bookmarks")
    suspend fun getBookmarks(): ApiResponse<List<ServiceResponse>>

    @POST("bookmarks/{serviceId}")
    suspend fun addBookmark(
        @Path("serviceId") serviceId: String,
    ): ApiResponse<Unit>

    @DELETE("bookmarks/{serviceId}")
    suspend fun removeBookmark(
        @Path("serviceId") serviceId: String,
    ): ApiResponse<Unit>

    @GET("bookmarks/{serviceId}/status")
    suspend fun getStatus(
        @Path("serviceId") serviceId: String,
    ): ApiResponse<Map<String, Boolean>>
}
