package must.kdroiders.hustlehub.ui.features.monetization.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import must.kdroiders.hustlehub.ui.features.monetization.data.remote.dto.SubscriptionResponseDto
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.GetSubscriptionUseCase
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.InitiateStkPushUseCase
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.PaymentPollState
import must.kdroiders.hustlehub.ui.features.monetization.domain.usecase.PollPaymentStatusUseCase
import timber.log.Timber
import javax.inject.Inject

/** UI state for the subscription status card on [SubscriptionScreen]. */
sealed interface SubscriptionUiState {
    data object Loading : SubscriptionUiState
    data class Success(val data: SubscriptionResponseDto?) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
}

/** Payment lifecycle states, exposed to [SubscriptionScreen] and [PaymentStatusScreen]. */
sealed interface PaymentUiState {
    data object Idle : PaymentUiState
    data object Submitting : PaymentUiState
    data class Polling(val attempt: Int) : PaymentUiState
    data class Success(val receiptNumber: String) : PaymentUiState
    data class Failed(val message: String) : PaymentUiState
    data object Timeout : PaymentUiState
}

@HiltViewModel
class MonetizationViewModel
    @Inject
    constructor(
        private val initiateStkPushUseCase: InitiateStkPushUseCase,
        private val pollPaymentStatusUseCase: PollPaymentStatusUseCase,
        private val getSubscriptionUseCase: GetSubscriptionUseCase,
    ) : ViewModel() {
        private val _subscriptionState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
        val subscriptionState: StateFlow<SubscriptionUiState> = _subscriptionState.asStateFlow()

        private val _paymentState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
        val paymentState: StateFlow<PaymentUiState> = _paymentState.asStateFlow()

        /**
         * Emits the checkoutRequestId once after a successful STK push trigger.
         * The [SubscriptionScreen] observes this to navigate to [PaymentStatusScreen].
         * Reset to null after navigation is consumed.
         */
        private val _pendingCheckoutId = MutableStateFlow<String?>(null)
        val pendingCheckoutId: StateFlow<String?> = _pendingCheckoutId.asStateFlow()

        init {
            loadSubscription()
        }

        /** Fetches the current subscription status and caches Pro flag to DataStore. */
        fun loadSubscription() {
            viewModelScope.launch {
                _subscriptionState.update { SubscriptionUiState.Loading }
                getSubscriptionUseCase()
                    .onSuccess { subscription ->
                        _subscriptionState.update { SubscriptionUiState.Success(subscription) }
                    }.onFailure { e ->
                        Timber.e(e, "Failed to load subscription")
                        _subscriptionState.update { SubscriptionUiState.Error("Could not load subscription. Check your connection.") }
                    }
            }
        }

        /**
         * Triggers the M-Pesa STK push and immediately starts polling for status.
         * Phone normalization (07XX → 254XX) is handled inside [InitiateStkPushUseCase].
         */
        fun triggerPayment(
            rawPhone: String,
            planType: String,
            serviceId: String? = null,
        ) {
            viewModelScope.launch {
                _paymentState.update { PaymentUiState.Submitting }
                initiateStkPushUseCase(rawPhone, planType, serviceId)
                    .onSuccess { response ->
                        // Signal the screen to navigate before polling starts
                        _pendingCheckoutId.update { response.checkoutRequestId }
                    }.onFailure { e ->
                        Timber.e(e, "STK push failed")
                        _paymentState.update { PaymentUiState.Failed(e.message ?: "Payment initiation failed.") }
                    }
            }
        }

        /** Consume the pending checkout navigation event — call after navigation is triggered. */
        fun consumePendingCheckoutId() {
            _pendingCheckoutId.update { null }
        }

        /** Starts the polling flow for a given [checkoutRequestId] and maps states to [_paymentState]. */
        fun pollStatus(checkoutRequestId: String) {
            viewModelScope.launch {
                pollPaymentStatusUseCase(checkoutRequestId).collect { pollState ->
                    _paymentState.update {
                        when (pollState) {
                            is PaymentPollState.Polling -> PaymentUiState.Polling(pollState.attempt)
                            is PaymentPollState.Completed -> {
                                loadSubscription() // Refresh Pro status on success
                                PaymentUiState.Success(pollState.receiptNumber)
                            }
                            is PaymentPollState.Failed -> PaymentUiState.Failed(pollState.reason)
                            PaymentPollState.Timeout -> PaymentUiState.Timeout
                        }
                    }
                }
            }
        }

        /** Resets payment state back to Idle — call when navigating away from PaymentStatusScreen. */
        fun resetPaymentState() {
            _paymentState.update { PaymentUiState.Idle }
        }
    }
