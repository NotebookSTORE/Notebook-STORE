package com.notebook.android.ui.myAccount.wallet.redeem

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.R
import com.notebook.android.databinding.FragmentWalletRedeemHistoryBinding
import com.notebook.android.model.ActivityState
import com.notebook.android.ui.popupDialogFrag.LoadingDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class WalletRedeemHistoryFragment : Fragment(), KodeinAware {

    private lateinit var fragmentWalletBinding: FragmentWalletRedeemHistoryBinding
    override val kodein by kodein()
    private val viewModelFactory: WalletRedeemViewModelFactory by instance()
    private val walletRedeemViewModel: WalletRedeemViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(WalletRedeemViewModel::class.java)
    }
    private val loadingDialog: LoadingDialog by lazy {
        LoadingDialog()
    }
    private lateinit var adapter: WalletRedeemHistoryAdapter

    private var userId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        walletRedeemViewModel.walletRedeemHistoryState.observe(viewLifecycleOwner, redeemObserver)
        fragmentWalletBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_wallet_redeem_history, container, false
        )
        fragmentWalletBinding.lifecycleOwner = this

        if (arguments != null) {
            val args = WalletRedeemHistoryFragmentArgs.fromBundle(requireArguments())
            userId = args.userId
        }

        return fragmentWalletBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragmentWalletBinding.srlRedeemItems.setOnRefreshListener {
            fragmentWalletBinding.srlRedeemItems.isRefreshing = false
            loadRedeemHistory()
        }
        adapter = WalletRedeemHistoryAdapter()
        fragmentWalletBinding.rvRedeemItems.adapter = adapter
        loadRedeemHistory()
    }

    private fun loadRedeemHistory() {
        userId?.let {
            if (!loadingDialog.isVisible) {
                loadingDialog.show(childFragmentManager, "loading")
            }
            walletRedeemViewModel.loadWalletRedeemHistory(it)
        }
    }

    private val redeemObserver = Observer<ActivityState> {
        loadingDialog.dismiss()
        when (it) {
            is WalletRedeemViewModel.WalletRedeemHistorySuccessState -> {
                adapter.clear()
                adapter.addItems(it.history)
                adapter.notifyDataSetChanged()
            }
            is WalletRedeemViewModel.WalletRedeemHistoryErrorState -> {
                showErrorDialog(it.message)
            }
        }
    }

    private fun showErrorDialog(message: String) {
        context?.let {
            AlertDialog.Builder(it)
                .setMessage(message)
                .setPositiveButton(
                    R.string.okay
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }
}
