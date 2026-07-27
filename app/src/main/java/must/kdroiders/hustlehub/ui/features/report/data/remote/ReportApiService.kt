package must.kdroiders.hustlehub.ui.features.report.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.ui.features.report.data.remote.dto.ReportResponse
import must.kdroiders.hustlehub.ui.features.report.data.remote.dto.SubmitReportRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApiService {
    @POST("reports")
    suspend fun submitReport(
        @Body request: SubmitReportRequest
    ): ApiResponse<ReportResponse>
}
