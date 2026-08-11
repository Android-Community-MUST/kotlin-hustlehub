package must.kdroiders.hustlehub.ui.features.analytics.data.remote.dto

import java.math.BigDecimal

data class ProviderAnalyticsDto(
    val totalServices: Int,
    val totalReviews: Long,
    val averageRating: Double,
    val totalInquiries: Long,
    val totalProfileViews: Long,
    val totalSearchImpressions: Long,
    val weeklyInquiries: List<DailyCountDto>,
    val weeklyViews: List<DailyCountDto>,
    val ratingDistribution: Map<String, Long>,
    val totalPayments: BigDecimal,
    val monthlyPayments: BigDecimal,
    val transactionCount: Long,
    val recentTransactions: List<TransactionSummaryDto>,
)

data class DailyCountDto(
    val date: String,
    val count: Long,
)

data class TransactionSummaryDto(
    val amount: BigDecimal,
    val status: String,
    val type: String,
    val date: String,
)
