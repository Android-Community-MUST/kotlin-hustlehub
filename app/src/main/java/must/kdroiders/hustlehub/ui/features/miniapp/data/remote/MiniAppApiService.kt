package must.kdroiders.hustlehub.ui.features.miniapp.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.miniapp.data.remote.dto.MiniAppRemoteDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MiniAppApiService {
    @GET("miniapps")
    suspend fun getActiveMiniApps(): ApiResponse<List<MiniAppRemoteDto>>

    @GET("miniapps/{appId}")
    suspend fun getMiniApp(
        @Path("appId") appId: String,
    ): ApiResponse<MiniAppRemoteDto>
}
