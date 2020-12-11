package com.notebook.android.adapter.home

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.Category
import com.notebook.android.databinding.CategorySectionHomeLayoutBinding
import java.util.*

class HomeProductSectionAdapter(val mCtx: Context, val productCategList:ArrayList<Category>,
categProductListener:CategoryProductListener)
    : RecyclerView.Adapter<HomeProductSectionAdapter.ProductSectionViewHolder>() {

    private var categProductListener:CategoryProductListener ?= null
    init {
        this.categProductListener = categProductListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductSectionViewHolder {
        val subCategItemBinding:CategorySectionHomeLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.category_section_home_layout, parent, false)
        return ProductSectionViewHolder(subCategItemBinding)
    }

    inner class ProductSectionViewHolder(val productBinding:CategorySectionHomeLayoutBinding)
        :RecyclerView.ViewHolder(productBinding.root) {

        fun bind(productCateg:Category){
            productBinding.setVariable(BR.category, productCateg)
            productBinding.executePendingBindings()
            productBinding.clCategoryView.setBackgroundColor(getRandomColorCode())

            productBinding.root.setOnClickListener {
                categProductListener?.getCategProdID(productCateg.id!!, productCateg.title!!)
            }
        }
    }

    fun getRandomColorCode(): Int {
        val random = Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    override fun getItemCount(): Int {
        return productCategList.size
    }

    override fun onBindViewHolder(holder: ProductSectionViewHolder, position: Int) {
        holder.bind(productCategList[position])
    }

    interface CategoryProductListener{
        fun getCategProdID(categID:Int, title:String)
    }
}