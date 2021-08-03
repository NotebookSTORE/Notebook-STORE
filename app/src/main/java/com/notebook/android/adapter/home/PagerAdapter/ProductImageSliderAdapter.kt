package com.notebook.android.adapter.home.PagerAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImageWithSize
import com.notebook.android.R
import com.notebook.android.model.productDetail.ProductDetailData
import com.notebook.android.utility.Constant
import com.notebook.android.utility.Constant.PRODUCTIMAGE_IMAGE_PATH
import com.notebook.android.utility.Constant.PRODUCT_IMAGE_PATH

class ProductImageSliderAdapter(val mCtx: Context, val imgList:ArrayList<ProductDetailData.ProductImageData>,
                                private val prodImageSliderListener: ProductImageSliderListener) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return imgList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.product_detail_item_slider_layout, null)

        val imgSlider:ImageView = view.findViewById(R.id.imgSlider)
        if (imgList[position].id == 0) {
            loadAllTypeImageWithSize(imgSlider,PRODUCT_IMAGE_PATH, imgList[position].image,1080,1000)
        } else {
            loadAllTypeImageWithSize(imgSlider,PRODUCTIMAGE_IMAGE_PATH, imgList[position].image,1080,1000)
        }

        val viewPager = container as ViewPager
        viewPager.addView(view, 0)

        view.setOnClickListener {
            prodImageSliderListener.onSliderClick(
                if (imgList[position].id == 0) {
                    "${PRODUCT_IMAGE_PATH}${imgList[position].image}"
                } else {
                    "${PRODUCTIMAGE_IMAGE_PATH}${imgList[position].image}"
                }
            )
            
            val imageArray:ArrayList<String>  = arrayListOf()
            imgList.forEachIndexed { i: Int, productImageData: ProductDetailData.ProductImageData ->
                val imageUrl =  if (imgList[i].id == 0) {
                    "${PRODUCT_IMAGE_PATH}${imgList[i].image}"
                } else {
                    "${PRODUCTIMAGE_IMAGE_PATH}${imgList[i].image}"
                }
                imageArray.add(imageUrl)
            }
            
            prodImageSliderListener.onSliderClick(imageArray.toTypedArray())
            
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    interface ProductImageSliderListener{
        fun onSliderClick(offerUrl:String){}
        fun onSliderClick(offerUrl: Array<String>)
    }
}