package com.notebook.android.adapter.address

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.AddressItemLayoutBinding
import com.notebook.android.databinding.CartItemLayoutBinding
import java.util.*
import kotlin.collections.ArrayList

class FetchAddressAdapter(val mCtx: Context,
                          val addressList: ArrayList<Address>,
                          val addrActionListener: AddressActionListener)
    : RecyclerView.Adapter<FetchAddressAdapter.CartVH>() {

    private var notebookPrefs:NotebookPrefs = NotebookPrefs(mCtx)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartVH {
        val addrItemBinding: AddressItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.address_item_layout, parent, false)
        return CartVH(addrItemBinding)
    }

    inner class CartVH(val addrItemBinding: AddressItemLayoutBinding)
        : RecyclerView.ViewHolder(addrItemBinding.root) {

        fun bind(addressData: Address){
            /* android:text="29, K2 Group India, Shivaji Marg, Moti Nagar Metro Station, New Delhi - 110015"*/
            val address = "${addressData.street}, ${addressData.locality}, ${addressData.city}," +
                    " ${addressData.state}, ${addressData.country}  -  ${addressData.pincode}"
            addrItemBinding.tvAddressDetail.text = address
            val addressModal = Gson().toJson(addressData)

            if(addressData.defaultaddress == 1){
                notebookPrefs.defaultAddr = address
                notebookPrefs.defaultAddrModal = addressModal
                addrItemBinding.tvMakeDefaultAddr.setTextColor(mCtx.resources.getColor(R.color.colorMakeDefault))
            }else{
                addrItemBinding.tvMakeDefaultAddr.setTextColor(mCtx.resources.getColor(R.color.colorMakeDefaultNot))
            }

            addrItemBinding.tvMakeDefaultAddr.setOnClickListener {
                if(addressData.defaultaddress == 1){
                    addrActionListener.addressAlreadyDefault(true)
                }else{
                    addrActionListener.addressMakeDefault(addressData.id)
                }
            }

            addrItemBinding.clAddressUpdateView.setOnClickListener{
                addrActionListener.addressUpdateData(addressData)
            }

            addrItemBinding.imgRemoveAddress.setOnClickListener {
                addrActionListener.addressDelete(addressData.id)
//                addressList.removeAt(adapterPosition)
                notifyItemRangeRemoved(adapterPosition, addressList.size)
            }
        }
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: CartVH, position: Int) {
        holder.bind(addressList[position])
    }

    interface AddressActionListener{
        fun addressDelete(multiAddrID:Int)
        fun addressMakeDefault(multiAddrID:Int)
        fun addressAlreadyDefault(alreadyDefault:Boolean)
        fun addressUpdateData(address:Address)
    }
}