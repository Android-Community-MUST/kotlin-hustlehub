package must.kdroiders.hustlehub.core.auth

/**
 * Utility helper to identify authorized administrators of HustleHub.
 * Gated strictly to verified admin emails and roles.
 */
object AdminAuthUtils {
    private val ADMIN_EMAILS =
        setOf(
            "admin@must.ac.ke",
            "nganga124007@students.must.ac.ke",
            "nganga124007@students.ac.ke",
        )

    /**
     * Returns true if the user's email or assigned role confers administrative privileges.
     */
    fun isAuthorizedAdmin(
        email: String?,
        role: String? = null,
    ): Boolean {
        if (!email.isNullOrBlank()) {
            val normalized = email.trim().lowercase()
            if (ADMIN_EMAILS.contains(normalized)) {
                return true
            }
        }
        return role == "ROLE_ADMIN" || role == "ROLE_SUPER_ADMIN"
    }
}
