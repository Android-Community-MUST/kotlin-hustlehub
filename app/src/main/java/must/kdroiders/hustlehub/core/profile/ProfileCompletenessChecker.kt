package must.kdroiders.hustlehub.core.profile

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User

object ProfileCompletenessChecker {
    fun needsLocationForBooking(user: User): Boolean = user.campusLocation.isBlank()

    fun needsProfileForListing(user: User): Boolean = user.bio.isBlank() || user.phone.isBlank() || user.campusLocation.isBlank()
}
