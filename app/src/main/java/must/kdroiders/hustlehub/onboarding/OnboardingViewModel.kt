package must.kdroiders.hustlehub.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import must.kdroiders.hustlehub.datastore.UserPreferences
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    // suspend so caller can await before navigating
    suspend fun completeOnboarding() {
        userPreferences.setFirstLaunchComplete()
    }
}
