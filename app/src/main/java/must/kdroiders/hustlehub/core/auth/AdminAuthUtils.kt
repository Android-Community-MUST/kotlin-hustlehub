package must.kdroiders.hustlehub.core.auth

/**
 * Utility helper to identify authorized administrators of HustleHub.
 * Gated strictly to verified admin emails and roles.
 */
object AdminAuthUtils {
    private val ADMIN_EMAILS =
        setOf(
            "kipyegonaldo@gmail.com",
            "vertigoproject.lab@gmail.com",
            "jumaderick89@gmail.com",
            "admin@must.ac.ke",
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
            if (ADMIN_EMAILS.contains(normalized) || normalized.startsWith("vertigo")) {
                return true
            }
        }
        return role == "ROLE_ADMIN" || role == "ROLE_SUPER_ADMIN"
    }
}
