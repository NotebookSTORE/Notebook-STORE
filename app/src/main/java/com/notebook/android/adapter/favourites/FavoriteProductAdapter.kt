package com.notebook.android.adapter.favourites

import android.content.Context
import android.graphics.Paint
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
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.Wishlist
import com.notebook.android.databinding.CartItemLayoutBinding
import com.notebook.android.databinding.WishlistItemLayoutBinding
import java.util.*
import kotlin.collections.ArrayList

class FavoriteProductAdapter(val mCtx: Context, val favList: ArrayList<Wishlist>,
val favDeleteListener: FavDeleteListener)
    : RecyclerView.Adapter<FavoriteProductAdapter.FavouriteVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouriteVH {
        val favItemBinding: WishlistItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.wishlist_item_layout, parent, false)
        return FavouriteVH(favItemBinding)
    }

    inner class FavouriteVH(val favItemBinding: WishlistItemLayoutBinding)
        : RecyclerView.ViewHolder(favItemBinding.root) {

        private var prodQty = 0

        fun bind(favProd: Wishlist){
            favItemBinding.setVariable(BR.wishlistData, favProd)
            favItemBinding.executePendingBindings()

            favItemBinding.clAddToCartFav.setOnClickListener{
                favDeleteListener.onFavAddToCart(favProd.id!!, prodQty)
            }

            favItemBinding.clRemoveFav.setOnClickListener{
                favDeleteListener.onFavItemDelete(favProd.favId)

                favList.removeAt(adapterPosition)
                notifyItemRangeRemoved(adapterPosition, favList.size)
            }

            favItemBinding.tvActualPrice.paintFlags =
                favItemBinding.tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            if(favProd.quantity != 0){
                val qtyIntList = setQuantityIntegerList(favProd.quantity)
                val qtyList = setQuantityList(favProd.quantity)
                val qtyAdapter = DetailProductSpinnerAdpater(mCtx, qtyList)
                favItemBinding.spProductQuantity.adapter = qtyAdapter
                favItemBinding.spProductQuantity.setSelection(favProd.favQuantity, true)
                Log.e("fav qty", " :: ${favProd.favQuantity}")
//                prodQty = qtyIntList[favProd.favQuantity]

                favItemBinding.spProductQuantity.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        prodQty = qtyIntList[p2]
                    }
                }
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
        return favList.size
    }

    override fun onBindViewHolder(holder: FavouriteVH, position: Int) {
        holder.bind(favList[position])
    }

    interface FavDeleteListener{
        fun onFavItemDelete(favID:Int)
        fun onFavAddToCart(prodID:Int, prodQty:Int)
    }
}