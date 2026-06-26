package must.kdroiders.hustlehub.ui.features.profile.domain.usecase

import must.kdroiders.hustlehub.ui.features.profile.domain.model.User
import must.kdroiders.hustlehub.ui.features.profile.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase
    @Inject
    constructor(private val repository: UserRepository) {
        suspend operator fun invoke(
            name: String,
            bio: String,
            phone: String,
            campusLocation: String,
            avatarUrl: String? = null,
        ): Result<User> = repository.updateProfile(name, bio, phone, campusLocation, avatarUrl)
    }
