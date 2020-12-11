package com.notebook.android.adapter.cart

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.databinding.CartItemLayoutBinding
import java.util.*

class CartProductAdapter(val mCtx: Context, val cartList: ArrayList<Cart>,
                         val cartActionListener: CartActionListener)
    : RecyclerView.Adapter<CartProductAdapter.CartVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartVH {
        val cartItemBinding: CartItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.cart_item_layout, parent, false)
        return CartVH(cartItemBinding)
    }

    inner class CartVH(val cartProdBinding: CartItemLayoutBinding)
        : RecyclerView.ViewHolder(cartProdBinding.root) {

        fun bind(cartProd: Cart){
            cartProdBinding.tvProductOfferPrice.paintFlags = cartProdBinding.tvProductOfferPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            cartProdBinding.setVariable(BR.cardDataModel, cartProd)
            cartProdBinding.executePendingBindings()

            Log.e("cartId", " :: ${cartProd.cartproduct_id}")

            var cartQty = cartProd.cartquantity!!.toInt()
            cartProdBinding.tvQuantityCount.text = "Quantity : ${cartQty}"

            cartProdBinding.clRemoveCartItem.setOnClickListener {
                cartActionListener.cartDeleteItem(cartProd.cartproduct_id)
                notifyItemRangeRemoved(adapterPosition, cartList.size)
            }

            cartProdBinding.imgIncreaseCartItem.setOnClickListener {
                cartActionListener.cartUpdated(false)
                if (cartQty == cartProd.quantity) {
                    cartProdBinding.tvQuantityCount.text = "Quantity : ${cartQty}"
                    cartProdBinding.tvInStock.text = mCtx.getString(R.string.strInStock) + " reached maximum product available"
                    cartProdBinding.imgIncreaseCartItem.visibility = View.GONE
                    return@setOnClickListener
                }

                if (cartQty >= cartProd.quantity) {
                    //error shows
                    cartActionListener.cartErrorShows("You not add more than available Quantity !!")
                }else if (cartQty < cartProd.quantity){
                    cartQty++
                    cartProdBinding.tvQuantityCount.text = "Quantity : ${cartQty}"
                }
            }

            cartProdBinding.imgCartProduct.setOnClickListener {
                cartActionListener.cartProductDetail(cartProd)
            }

            cartProdBinding.clUpdateCartItem.setOnClickListener{
                cartActionListener.updateCartItem(cartProd.cartproduct_id, cartQty)
            }

            cartProdBinding.imgDecreaseCartItem.setOnClickListener {
                cartProdBinding.tvInStock.text = mCtx.getString(R.string.strInStock)
                if(cartQty == 1){
                    //error shows
                    cartActionListener.cartDeleteLastItem(cartProd.cartproduct_id)
                }else{
                    --cartQty
                    cartProdBinding.imgIncreaseCartItem.visibility = View.VISIBLE
                    cartActionListener.cartUpdated(false)
                    cartProdBinding.tvQuantityCount.text = "Quantity : ${cartQty}"
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: CartVH, position: Int) {
        holder.bind(cartList[position])
    }

    interface CartActionListener{
        fun cartDeleteItem(cartId:String)
        fun updateCartItem(cartId:String, cartQty:Int)
        fun cartErrorShows(msg:String)
        fun cartDeleteLastItem(qty:String)
        fun cartProductDetail(cartProd: Cart)
        fun cartUpdated(isUpdated:Boolean)
    }

}