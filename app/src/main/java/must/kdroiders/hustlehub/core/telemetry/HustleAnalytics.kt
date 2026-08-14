package must.kdroiders.hustlehub.core.telemetry

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HustleAnalytics
    @Inject
    constructor(
        private val analytics: FirebaseAnalytics?,
    ) {
        fun setCollectionEnabled(enabled: Boolean) {
            analytics?.setAnalyticsCollectionEnabled(enabled)
        }

        fun setUserProperties(
            role: String?,
            campus: String?,
            isVerifiedPro: Boolean? = null,
        ) {
            role?.let { analytics?.setUserProperty("role", it) }
            campus?.let { analytics?.setUserProperty("campus", it) }
            isVerifiedPro?.let { analytics?.setUserProperty("is_verified_pro", it.toString()) }
        }

        fun logSignupCompleted(method: String = "email") {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, method)
            }
            analytics?.logEvent("signup_completed", bundle)
        }

        fun logServiceCreated(
            serviceId: String,
            category: String,
        ) {
            val bundle = Bundle().apply {
                putString("service_id", serviceId)
                putString("category", category)
            }
            analytics?.logEvent("service_created", bundle)
        }

        fun logServiceViewed(
            serviceId: String,
            category: String,
        ) {
            val bundle = Bundle().apply {
                putString("service_id", serviceId)
                putString("category", category)
            }
            analytics?.logEvent("service_viewed", bundle)
        }

        fun logMessageSent(
            conversationId: String,
            type: String = "TEXT",
        ) {
            val bundle = Bundle().apply {
                putString("conversation_id", conversationId)
                putString("message_type", type)
            }
            analytics?.logEvent("message_sent", bundle)
        }

        fun logVoiceNoteSent(
            conversationId: String,
            durationSec: Int,
        ) {
            val bundle = Bundle().apply {
                putString("conversation_id", conversationId)
                putInt("duration_sec", durationSec)
            }
            analytics?.logEvent("voice_note_sent", bundle)
        }

        fun logAiSearchUsed(query: String) {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            }
            analytics?.logEvent("ai_search_used", bundle)
        }

        fun logReviewSubmitted(
            serviceId: String,
            rating: Float,
        ) {
            val bundle = Bundle().apply {
                putString("service_id", serviceId)
                putFloat("rating", rating)
            }
            analytics?.logEvent("review_submitted", bundle)
        }

        fun logMapOpened() {
            analytics?.logEvent("map_opened", null)
        }

        fun logProviderContacted(providerId: String) {
            val bundle = Bundle().apply {
                putString("provider_id", providerId)
            }
            analytics?.logEvent("provider_contacted", bundle)
        }
    }
