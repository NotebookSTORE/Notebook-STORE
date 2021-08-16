package com.notebook.android.adapter.order

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.OrderHistory
import com.notebook.android.databinding.MyOrderItemLayoutBinding

class OrderHistoryAdapter(
    val mCtx: Context, private val orderDataList: List<OrderHistory>,
    val orderDataListener: OrderDataListener
) : RecyclerView.Adapter<OrderHistoryAdapter.MyOrderVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderHistoryAdapter.MyOrderVH {
        val orderItemBinding: MyOrderItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx),
            R.layout.my_order_item_layout,
            parent, false
        )

        return MyOrderVH(orderItemBinding)
    }

    override fun getItemCount(): Int {
        return orderDataList.size
    }

    override fun onBindViewHolder(holder: OrderHistoryAdapter.MyOrderVH, position: Int) {
        holder.bind(orderDataList[position])
    }

    inner class MyOrderVH(private val orderItemBinding: MyOrderItemLayoutBinding) :
        RecyclerView.ViewHolder(orderItemBinding.root) {

        fun bind(orderData: OrderHistory) {
            orderItemBinding.setVariable(BR.orderDataModel, orderData)
            orderItemBinding.executePendingBindings()

            if (orderData.tracking_url.isNullOrBlank())
                orderItemBinding.tvTrackOrder.visibility = View.GONE


            orderItemBinding.tvWriteReview.setOnClickListener {
                orderDataListener.onWriteReviewClick(orderData)
            }

            orderItemBinding.clOrderMainView.setOnClickListener {
                orderDataListener.onItemViewClicked(orderData)
            }

            orderItemBinding.tvTrackOrder.setOnClickListener {
                orderDataListener.onTrackOrderClick(orderData)
            }

            orderItemBinding.itemImage.setOnClickListener {
                Log.e("anil"," id = " + orderData.cartproduct_id + "  title = " +  orderData.title)
            }
        }
    }

    interface OrderDataListener {
        fun onWriteReviewClick(orderData: OrderHistory)
        fun onTrackOrderClick(orderData: OrderHistory)
        fun onItemViewClicked(orderData: OrderHistory)
    }
}