package com.notebook.android.ui.myAccount.wallet.redeem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notebook.android.model.ActivityState
import com.notebook.android.model.wallet.redeem.WalletRedeemHistory
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.launch

class WalletRedeemViewModel(private val walletRedeemRepo: WalletRedeemRepo) : ViewModel() {
    private val _walletRedeemHistoryState: MutableLiveData<ActivityState> = MutableLiveData()
    val walletRedeemHistoryState: LiveData<ActivityState> = _walletRedeemHistoryState


    fun loadWalletRedeemHistory(userId: Int) {
        viewModelScope.launch {
            try {
                val walletRedeemHistoryResponse = walletRedeemRepo.fetchRedeemHistory(userId)
                if (walletRedeemHistoryResponse == null || walletRedeemHistoryResponse.status == 0) {
                    _walletRedeemHistoryState.value = WalletRedeemHistoryErrorState(
                        walletRedeemHistoryResponse?.msg ?: "Please try again later"
                    )
                } else {
                    _walletRedeemHistoryState.value =
                        WalletRedeemHistorySuccessState(walletRedeemHistoryResponse.history)
                }
            } catch (e: ApiException) {
                _walletRedeemHistoryState.value =
                    WalletRedeemHistoryErrorState(e.message ?: "Please try again later")
            } catch (e: NoInternetException) {
                _walletRedeemHistoryState.value =
                    WalletRedeemHistoryErrorState(e.message ?: "Please try again later")
            }
        }
    }

    data class WalletRedeemHistorySuccessState(val history: List<WalletRedeemHistory>) : ActivityState()
    data class WalletRedeemHistoryErrorState(val message: String) : ActivityState()
}