package must.kdroiders.hustlehub.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.datastore.UserPreferences
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstLaunchComplete()
        }
    }
}
