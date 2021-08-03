package com.notebook.android.adapter.order

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.adapter.DetailProduct.DetailProductSpinnerAdpater
import com.notebook.android.data.db.entities.OrderSummaryProduct
import com.notebook.android.data.db.entities.Product
import com.notebook.android.databinding.OrderSummaryProductLayoutBinding

class OrderSummaryAdapter(val mCtx: Context, private val prodListData: List<OrderSummaryProduct>,
                          private val orderPriceListener: OrderPriceListener)
: RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderSummaryVH {
        val orderSummaryBinding: OrderSummaryProductLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.order_summary_product_layout, parent, false)
        return OrderSummaryVH(orderSummaryBinding)
    }

    inner class OrderSummaryVH(val orderSummaryBinding: OrderSummaryProductLayoutBinding)
        : RecyclerView.ViewHolder(orderSummaryBinding.root) {

        private var prodQty = 0

        fun bind(prodData: OrderSummaryProduct){
            orderSummaryBinding.setVariable(BR.productOrderModel, prodData)
            orderSummaryBinding.executePendingBindings()

            Log.e("orderSummaryAdapter", " :: pos->${adapterPosition} :: qty->${prodData.cartQuantity}" +
                    " :: totalAmount->${prodData.cartTotalAmount} :: ${prodData.discount} :: ${prodData.price}")
            orderSummaryBinding.tvProdQuantity.text = "${prodData.cartQuantity} Pack"
            val result = (prodData.price.times(prodData.discount)).div(100)
            val finalResult = Math.round(prodData.price.minus(result))
            orderSummaryBinding.tvProdAmount.text = "₹ ${prodData.cartQuantity.times(finalResult)}"
            Log.e("result", " :: result -> ${result} :: finalResult -> ${finalResult} :: ${prodQty.times(finalResult)}")
//            orderSummaryBinding.tvProdDeliveryBy.text = "Delivery by Tomorrow,Fri | ₹ ${prodData.delivery_charges}"

            if(prodData.quantity != 0){
                val qtyIntList = setQuantityIntegerList(prodData.quantity)
                val qtyList = setQuantityList(prodData.quantity)
                val qtyAdapter = DetailProductSpinnerAdpater(mCtx, qtyList)
                orderSummaryBinding.spProductQuantity.adapter = qtyAdapter

                var cartQtyPosition = 0
                for(pos in 0 until qtyList.size){
                    Log.e("itemPosAdapter", " :: ${prodData.cartQuantity} :: ${qtyIntList[pos]}")
                    if(prodData.cartQuantity == qtyIntList[pos]){
                        cartQtyPosition = pos
                        Log.e("loopPos", " :: $pos")
                    }
                }

                var shouldFireCallback = false
                orderSummaryBinding.spProductQuantity.setSelection(cartQtyPosition)

                orderSummaryBinding.spProductQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                        prodQty = qtyIntList[p2]

                        orderSummaryBinding.tvProdQuantity.text = "${prodQty} Pack"
                        val result = (prodData.price.times(prodData.discount)).div(100)
                        val finalResult = Math.round(prodData.price.minus(result))
                        orderSummaryBinding.tvProdAmount.text = "₹ ${prodQty.times(finalResult)}"

                        Log.e("totalAmountCheckSpinner", " :: ${prodQty.times(finalResult)}")
                        prodData.cartQuantity = prodQty
                        prodData.cartTotalAmount = prodQty.times(prodData.price)
                        if (shouldFireCallback) {
                            orderPriceListener.onChangeProdQuantity(adapterPosition, prodData.id.toInt(), prodQty, prodQty.times(finalResult).toFloat())
                        } else {
                            shouldFireCallback = true
                        }
                    }
                }
            }


            orderSummaryBinding.imgProductImage.setOnClickListener {
                val prod = Product(
                    prodData.id, prodData.keyfeature,
                    prodData.material,
                    prodData.title, prodData.alias, prodData.image,
                    prodData.status, prodData.short_description,
                    prodData.description, prodData.data_sheet,
                    prodData.quantity, prodData.price, prodData.offer_price,
                    prodData.product_code, prodData.product_condition,
                    prodData.discount, prodData.latest,
                    prodData.best, prodData.brandtitle, prodData.colortitle)

                orderPriceListener.onProductImageClick(prod)
            }

        }

        private fun setQuantityList(qty:Int) : ArrayList<String> {
            val qtyArray = ArrayList<String>()
            for(i in 0 until qty){
                qtyArray.add("${i+1} Pcs")
            }
            return qtyArray
        }

        private fun setQuantityIntegerList(qty:Int) : ArrayList<Int> {
            val qtyArray = ArrayList<Int>()
            for(i in 0 until qty){
                qtyArray.add(i+1)
            }
            return qtyArray
        }
    }


    override fun getItemCount(): Int {
        return prodListData.size
    }

    override fun onBindViewHolder(holder: OrderSummaryVH, position: Int) {
        holder.bind(prodListData[position])
    }

    interface OrderPriceListener{
        fun onChangeProdQuantity(orderItemPosition:Int, prodID:Int, orderQty:Int, orderAmount:Float)
        fun onPriceCheckOnCoupon(isGreater:Boolean)
        fun errorMessage(msg:String)
        fun onProductImageClick(prod:Product)
    }
}