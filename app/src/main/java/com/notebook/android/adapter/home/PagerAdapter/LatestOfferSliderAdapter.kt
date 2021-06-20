package com.notebook.android.adapter.home.PagerAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImage
import com.max.ecomaxgo.maxpe.view.flight.utility.loadAllTypeImageWithSize
import com.notebook.android.R
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.LatestOffer
import com.notebook.android.utility.Constant.BANNER_IMAGE_PATH
import com.notebook.android.utility.Constant.LATEST_OFFER_IMAGE_PATH

class LatestOfferSliderAdapter(val mCtx: Context, val imgList:ArrayList<LatestOffer>,
val latestOfferSliderListener: LatestOfferSliderListener) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return imgList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_slider, null)

        val imgSlider:ImageView = view.findViewById(R.id.imgSlider)
        loadAllTypeImageWithSize(imgSlider, LATEST_OFFER_IMAGE_PATH, imgList[position].image,1080,1000)
//        Glide.with(mCtx).load("${LATEST_OFFER_IMAGE_PATH}${imgList[position].image}").into(imgSlider)
        val viewPager = container as ViewPager
        viewPager.addView(view, 0)

        view.setOnClickListener {
            latestOfferSliderListener.onOfferSliderClick(imgList[position].url?:"", imgList[position].offer)
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    interface LatestOfferSliderListener{
        fun onOfferSliderClick(offerUrl:String, offerType:Int)
    }
}