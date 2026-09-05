package must.kdroiders.hustlehub.core.notification

object ActiveConversationTracker {
    @Volatile
    var activeConversationId: String? = null
}
