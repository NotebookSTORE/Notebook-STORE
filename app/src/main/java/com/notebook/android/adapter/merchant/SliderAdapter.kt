package com.notebook.android.adapter.merchant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.notebook.android.R
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.utility.Constant

class SliderAdapter(val mCtx: Context, val imgList:List<Banner>) : PagerAdapter() {

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
        Glide.with(mCtx).load("${Constant.BANNER_IMAGE_PATH}${imgList[position].image}").into(imgSlider)

        val viewPager = container as ViewPager
        viewPager.addView(view, 0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}