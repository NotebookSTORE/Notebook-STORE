package com.notebook.android.adapter.genericAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class GenericAdapter<T, D>(val context: Context?, arrayList: List<T>?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mContext: Context? = null
    private var mArrayList: List<T>? = null
    init {
        mContext = context
        mArrayList = arrayList
    }

    abstract fun getLayoutResId(): Int

    abstract fun onBindData(model: T, position: Int, dataBinding: D)

    abstract fun onItemClick(model: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ItemViewHolder {
        val dataBinding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(context),
            getLayoutResId(),
            parent,
            false
        )
        return ItemViewHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindData(mArrayList!![position], position,
            (holder as GenericAdapter<*, *>.ItemViewHolder).mDataBinding as D
        )
        (holder.mDataBinding as ViewDataBinding).root
            .setOnClickListener { onItemClick(mArrayList!![position], position) }
    }

    override fun getItemCount(): Int {
        return mArrayList!!.size
    }

    fun addItems(arrayList: ArrayList<T>?) {
        mArrayList = arrayList
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return mArrayList!![position]
    }

    inner class ItemViewHolder(binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var mDataBinding: D = binding as D

    }

}