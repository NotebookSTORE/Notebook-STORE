package com.notebook.android.ui.popupDialogFrag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.notebook.android.R
import com.notebook.android.databinding.FragmentRemoveItemDialogBinding
import com.notebook.android.ui.dashboard.listener.RemoveItemListener

class RemoveItemDialog : DialogFragment(), View.OnClickListener {

    private lateinit var fragRemoveItemBinding: FragmentRemoveItemDialogBinding
    private lateinit var removeItemListener: RemoveItemListener

    fun setRemoveListener(removeItemListener: RemoveItemListener){
        this.removeItemListener = removeItemListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragRemoveItemBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_remove_item_dialog, container, false)
        return fragRemoveItemBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragRemoveItemBinding.tvRemoveYes.setOnClickListener(this)
        fragRemoveItemBinding.tvRemoveNo.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0){
            fragRemoveItemBinding.tvRemoveYes -> {
                removeItemListener.onRemovedYes(true)
                dismissAllowingStateLoss()
            }

            fragRemoveItemBinding.tvRemoveNo -> {
                dismissAllowingStateLoss()
            }
        }
    }
}
