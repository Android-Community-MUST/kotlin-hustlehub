package must.kdroiders.hustlehub.core.ui

/**
 * Centralized semantic test tags for UI testing (Compose UI tests & Maestro).
 */
object TestTags {
    // Discovery & Home
    const val SEARCH_BAR = "search_bar"
    const val CATEGORY_CHIPS = "category_chips"
    const val SERVICE_GRID = "service_grid"
    const val SERVICE_CARD = "service_card"
    const val SERVICE_DETAIL_SCREEN = "service_detail_screen"
    const val VIEW_PROFILE_BUTTON = "view_profile_button"

    // Authentication & Onboarding
    const val SIGNUP_NAME = "signup_name_input"
    const val SIGNUP_EMAIL = "signup_email_input"
    const val SIGNUP_PASSWORD = "signup_password_input"
    const val SIGNUP_CONFIRM_PASSWORD = "signup_confirm_password_input"
    const val SIGNUP_BUTTON = "signup_button"
    const val VERIFY_EMAIL_BUTTON = "verify_email_button"
    const val RESEND_EMAIL_BUTTON = "resend_email_button"
    const val PROFILE_SETUP_COMPLETE = "complete_setup_button"
    const val PROFILE_SETUP_SKIP = "skip_profile_setup_button"

    // Service Creation
    const val CREATE_SERVICE_TITLE = "create_service_title"
    const val CREATE_SERVICE_PRICE = "create_service_price"
    const val CREATE_SERVICE_DESCRIPTION = "create_service_description"
    const val PUBLISH_SERVICE_BUTTON = "publish_service_button"

    // Chat
    const val CONVERSATION_ITEM = "conversation_item"
    const val CHAT_INPUT_FIELD = "chat_input_field"
    const val SEND_MESSAGE_BUTTON = "send_message_button"
    const val MESSAGE_BUBBLE = "message_bubble"

    // Reviews
    const val REVIEW_RATING_BAR = "review_rating_bar"
    const val REVIEW_CONTENT_INPUT = "review_content_input"
    const val SUBMIT_REVIEW_BUTTON = "submit_review_button"
}
