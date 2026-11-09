package must.kdroiders.hustlehub.ui.features.miniapp.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.CreateMiniAppRequestDto
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MiniAppApiService {
    @GET("miniapps")
    suspend fun getActiveMiniApps(): ApiResponse<List<MiniAppRemoteDto>>

    @GET("miniapps/{appId}")
    suspend fun getMiniApp(
        @Path("appId") appId: String,
    ): ApiResponse<MiniAppRemoteDto>

    @POST("miniapps/apply")
    suspend fun applyForMiniApp(
        @Body request: CreateMiniAppRequestDto,
    ): ApiResponse<MiniAppRemoteDto>

    @GET("miniapps/my")
    suspend fun getMyMiniApps(): ApiResponse<List<MiniAppRemoteDto>>
}
