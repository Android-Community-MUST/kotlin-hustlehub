package must.kdroiders.hustlehub.ui.features.admin.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.admin.data.remote.dto.AdminActionRequestDto
import must.kdroiders.hustlehub.ui.features.admin.data.remote.dto.AdminAnalyticsDto
import must.kdroiders.hustlehub.ui.features.admin.data.remote.dto.AuditLogResponseDto
import must.kdroiders.hustlehub.ui.features.admin.data.remote.dto.ReportResponseDto
import must.kdroiders.hustlehub.ui.features.admin.data.remote.dto.UserAdminViewDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {
    @GET("/api/v1/admin/analytics")
    suspend fun getAnalytics(): ApiResponse<AdminAnalyticsDto>

    @GET("/api/v1/admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): ApiResponse<PageResponse<UserAdminViewDto>>

    @GET("/api/v1/admin/users/{id}")
    suspend fun getUserDetail(
        @Path("id") id: String,
    ): ApiResponse<UserAdminViewDto>

    @POST("/api/v1/admin/users/{id}/suspend")
    suspend fun suspendUser(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @POST("/api/v1/admin/users/{id}/unsuspend")
    suspend fun unsuspendUser(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @POST("/api/v1/admin/users/{id}/verify-pro")
    suspend fun verifyPro(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @POST("/api/v1/admin/users/{id}/revoke-pro")
    suspend fun revokePro(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @POST("/api/v1/admin/services/{id}/delist")
    suspend fun delistService(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @POST("/api/v1/admin/services/{id}/relist")
    suspend fun relistService(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @GET("/api/v1/admin/reports")
    suspend fun getReports(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): ApiResponse<PageResponse<ReportResponseDto>>

    @POST("/api/v1/admin/reports/{id}/resolve")
    suspend fun resolveReport(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @POST("/api/v1/admin/reports/{id}/dismiss")
    suspend fun dismissReport(
        @Path("id") id: String,
        @Body body: AdminActionRequestDto,
    ): ApiResponse<Unit>

    @GET("/api/v1/admin/audit-logs")
    suspend fun getAuditLogs(
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): ApiResponse<PageResponse<AuditLogResponseDto>>
}
