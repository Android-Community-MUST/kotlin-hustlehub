package must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

@Keep
data class ProviderAnalyticsDto(
    @SerializedName("totalServices")
    val totalServices: Int,
    @SerializedName("totalReviews")
    val totalReviews: Long,
    @SerializedName("averageRating")
    val averageRating: Double,
    @SerializedName("totalInquiries")
    val totalInquiries: Long,
    @SerializedName("totalProfileViews")
    val totalProfileViews: Long,
    @SerializedName("totalSearchImpressions")
    val totalSearchImpressions: Long,
    @SerializedName("weeklyInquiries")
    val weeklyInquiries: List<DailyCountDto>,
    @SerializedName("weeklyViews")
    val weeklyViews: List<DailyCountDto>,
    @SerializedName("ratingDistribution")
    val ratingDistribution: Map<String, Long>,
    @SerializedName("totalPayments")
    val totalPayments: BigDecimal,
    @SerializedName("monthlyPayments")
    val monthlyPayments: BigDecimal,
    @SerializedName("transactionCount")
    val transactionCount: Long,
    @SerializedName("recentTransactions")
    val recentTransactions: List<TransactionSummaryDto>,
)

@Keep
data class DailyCountDto(
    @SerializedName("date")
    val date: String,
    @SerializedName("count")
    val count: Long,
)

@Keep
data class TransactionSummaryDto(
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("status")
    val status: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("date")
    val date: String,
)
