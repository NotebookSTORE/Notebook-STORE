package com.notebook.android.ui.filter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.common.base.Splitter
import com.google.gson.Gson
import com.innovattic.rangeseekbar.RangeSeekBar
import com.max.ecomaxgo.maxpe.view.flight.utility.showSnackBar
import com.notebook.android.R
import com.notebook.android.adapter.filterBy.*
import com.notebook.android.databinding.FragmentFilterByProductBinding
import com.notebook.android.model.filter.FilterRequestData
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class FilterByProductFrag : Fragment(), KodeinAware, FilterResponseListener {

    override val kodein by kodein()
    private val viewModelFactory : FilterVMFactory by instance<FilterVMFactory>()
    private lateinit var filterByProdBinding:FragmentFilterByProductBinding
    private val filterVM:FilterByVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(FilterByVM::class.java)
    }
    private lateinit var navController: NavController
    private var brandId = 0
    private var colorId = 0
    private var discountValue = 0
    private var price1 = 0
    private var price2 = 0
    private var couponId = 0
    private var ratingTitle = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        filterVM.getFilterData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        filterByProdBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_filter_by_product, container, false)

        filterByProdBinding.lifecycleOwner = this
        filterVM.filterResponseListener = this
        setupRecyclerView()
        return filterByProdBinding.root
    }

    private fun setupRecyclerView(){
        val flexboxLayoutManagerBrand = FlexboxLayoutManager(requireContext())
        flexboxLayoutManagerBrand.flexWrap = FlexWrap.WRAP
        flexboxLayoutManagerBrand.flexDirection = FlexDirection.ROW
        flexboxLayoutManagerBrand.alignItems = AlignItems.STRETCH
        filterByProdBinding.recViewBrandFilter.layoutManager = flexboxLayoutManagerBrand
        filterByProdBinding.recViewBrandFilter.itemAnimator = DefaultItemAnimator()
        filterByProdBinding.recViewBrandFilter.hasFixedSize()

        val flexboxLayoutManagerDiscount = FlexboxLayoutManager(requireContext())
        flexboxLayoutManagerDiscount.flexWrap = FlexWrap.WRAP
        flexboxLayoutManagerDiscount.flexDirection = FlexDirection.ROW
        flexboxLayoutManagerDiscount.alignItems = AlignItems.STRETCH
        filterByProdBinding.recViewDiscountFilter.layoutManager = flexboxLayoutManagerDiscount
        filterByProdBinding.recViewDiscountFilter.itemAnimator = DefaultItemAnimator()
        filterByProdBinding.recViewDiscountFilter.hasFixedSize()

        val flexboxLayoutManagerRating = FlexboxLayoutManager(requireContext())
        flexboxLayoutManagerRating.flexWrap = FlexWrap.WRAP
        flexboxLayoutManagerRating.flexDirection = FlexDirection.ROW
        flexboxLayoutManagerRating.alignItems = AlignItems.STRETCH
        filterByProdBinding.recViewRatingFilter.layoutManager = flexboxLayoutManagerRating
        filterByProdBinding.recViewRatingFilter.itemAnimator = DefaultItemAnimator()
        filterByProdBinding.recViewRatingFilter.hasFixedSize()

        val flexboxLayoutManagerColor = FlexboxLayoutManager(requireContext())
        flexboxLayoutManagerColor.flexWrap = FlexWrap.WRAP
        flexboxLayoutManagerColor.flexDirection = FlexDirection.ROW
        flexboxLayoutManagerColor.alignItems = AlignItems.STRETCH
        filterByProdBinding.recViewColorFilter.layoutManager = flexboxLayoutManagerColor
        filterByProdBinding.recViewColorFilter.itemAnimator = DefaultItemAnimator()
        filterByProdBinding.recViewColorFilter.hasFixedSize()

        val flexboxLayoutManagerCoupon = FlexboxLayoutManager(requireContext())
        flexboxLayoutManagerColor.flexWrap = FlexWrap.WRAP
        flexboxLayoutManagerColor.flexDirection = FlexDirection.ROW
        flexboxLayoutManagerColor.alignItems = AlignItems.STRETCH

        filterByProdBinding.recViewCouponFilter.layoutManager = flexboxLayoutManagerCoupon
        filterByProdBinding.recViewCouponFilter.itemAnimator = DefaultItemAnimator()
        filterByProdBinding.recViewCouponFilter.hasFixedSize()
    }

    override fun onFailure(msg: String) {
        filterByProdBinding.root.showSnackBar(msg)
    }

    private var brandIDArray:List<Int> ?=null
    private var colorIDArray:List<Int> ?=null
    private var discValueArray:List<Int> ?=null
    private var rateValueArray:List<Int> ?=null
    private var couponIDArray:List<Int> ?= null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        filterByProdBinding.clApplyFilter.setOnClickListener{
            val filterRawData = FilterRequestData(brandIDArray?:ArrayList(), price1, price2,
                discValueArray?:ArrayList(), colorIDArray?:ArrayList(),
                rateValueArray?:ArrayList(), couponIDArray?:ArrayList(),
                "", 0, 0)
            Log.e("raw data", " :: ${Gson().toJson(filterRawData)}")

            navController.previousBackStackEntry?.savedStateHandle?.set("filterData", Gson().toJson(filterRawData))
            navController.popBackStack()
        }

        filterVM.getBrandFilterFromDB().observe(viewLifecycleOwner, Observer {

            Log.e("brand size", " :: ${it.size}")
            val brandFilterAdapter = BrandFilterAdapter(requireContext(), it,
                object : BrandFilterAdapter.BrandFilterDataListener{
                    override fun getBrandData(
                        brandID: Int, brandTitle: String,
                        brandIDArray: ArrayList<Int>) {
                        Log.e("brands", " :: $brandID :: $brandTitle :: $brandIDArray")
                        this@FilterByProductFrag.brandIDArray = brandIDArray
                        brandId = brandID
                    }
                })
            filterByProdBinding.recViewBrandFilter.adapter = brandFilterAdapter
        })

        filterVM.getColorFilterFromDB().observe(viewLifecycleOwner, Observer {
            val colorFilterAdapter = ColorFilterAdapter(requireContext(), it,
                object : ColorFilterAdapter.ColorFilterDataListener{
                override fun getColorData(colorID: Int, colorTitle: String, colorIDArray:ArrayList<Int>) {
                    Log.e("color data", " :: $colorID :: $colorTitle :: $colorIDArray")
                    this@FilterByProductFrag.colorIDArray = colorIDArray
                    colorId = colorID
                }
            })
            filterByProdBinding.recViewColorFilter.adapter = colorFilterAdapter
        })

        filterVM.getDiscountFilterFromDB().observe(viewLifecycleOwner, Observer {
            val discountFilterAdapter = DiscountFilterAdapter(requireContext(), it,
                object : DiscountFilterAdapter.DiscountFilterDataListener{
                    override fun getDiscountData(discountID: Int, discountTitle: Int, discountValueArray:ArrayList<Int>) {
                        Log.e("discounted", " :: $discountID :: $discountTitle :: $discountValueArray")
                        this@FilterByProductFrag.discValueArray = discountValueArray
                        discountValue = discountTitle
                    }
            })
            filterByProdBinding.recViewDiscountFilter.adapter = discountFilterAdapter
        })

        filterVM.getPriceFilterFromDB().observe(viewLifecycleOwner, Observer {
            try {
                for(element in it){
                    Log.e("price value", " :: ${element.price1} ::  ${element.price2}")
                    price1 = Integer.parseInt(element.price1!!)
                    price2 = Integer.parseInt(element.price2!!)

                    filterByProdBinding.sbPriceRange.max  = price2
                    filterByProdBinding.sbPriceRange.minRange  = price1
                    filterByProdBinding.tvPriceMinValue.text = element.price1!!
                    filterByProdBinding.tvPriceMaxValue.text = element.price2!!
                }
            }catch (ex:NumberFormatException){
               Log.e("number format exception"," :: ${ ex.message}")
            }
        })

        filterVM.getRatingFilterFromDB().observe(viewLifecycleOwner, Observer {

            val ratingFilterAdapter = RatingFilterAdapter(requireContext(), it,
                object : RatingFilterAdapter.RatingFilterDataListener{
                override fun getRatingData(rateID: Int, rateTitle: String, ratingArray:List<Int>) {
                    Log.e("Ratings", " :: $rateID :: $rateTitle :: $ratingArray")
                    this@FilterByProductFrag.rateValueArray = ratingArray
                    ratingTitle = rateTitle
                }
            })
            filterByProdBinding.recViewRatingFilter.adapter = ratingFilterAdapter
        })

        filterVM.getCouponFilterFromDB().observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                filterByProdBinding.tvCoupon.visibility = View.GONE
                filterByProdBinding.clCoupon.visibility = View.GONE
            }else{
                filterByProdBinding.tvCoupon.visibility = View.VISIBLE
                filterByProdBinding.clCoupon.visibility = View.VISIBLE
            }
            val couponFilterAdapter = CouponFilterAdapter(requireContext(), it,
                object : CouponFilterAdapter.CouponFilterDataListener{
                    override fun getCouponData(couponID: Int, couponTitle: String, couponIDArray: ArrayList<Int>) {
                        Log.e("color data", " :: $couponID :: $couponTitle :: $couponIDArray")
                        this@FilterByProductFrag.couponIDArray = couponIDArray
                        couponId = couponID

                        if (it.isNotEmpty()) {
                            filterByProdBinding.tvCoupon.visibility = View.VISIBLE
                        } else {
                            filterByProdBinding.tvCoupon.visibility = View.GONE
                        }
                    }
                })

            filterByProdBinding.recViewCouponFilter.adapter = couponFilterAdapter
        })

        filterByProdBinding.sbPriceRange.seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener{

            override fun onStartedSeeking() {}

            override fun onStoppedSeeking() {}

            override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                Log.e("seekbarchange", " :: ${minThumbValue} :: ${maxThumbValue}")
                filterByProdBinding.tvPriceMaxValue.text = maxThumbValue.toString()
                filterByProdBinding.tvPriceMinValue.text = minThumbValue.toString()

                price1 = minThumbValue
                price2 = maxThumbValue
            }
        }
    }


}
