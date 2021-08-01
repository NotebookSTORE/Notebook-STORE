package com.notebook.android.utility

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class PaginationScrollListener(var layoutManager: GridLayoutManager) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        Log.d(TAG, "onScrolled: ")
        if (dy>0) {

            val visibleThreshold = 2
            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val lastItem = layoutManager.findLastCompletelyVisibleItemPosition()
            val currentTotalCount = layoutManager.itemCount


            if (currentTotalCount <= lastItem + visibleThreshold) {
                Log.d(TAG, "onScrolled: load more")
                loadMoreItems()
            } else {
                Log.d(TAG, "onScrolled: skip loading more")
            }
        }
    }

    protected abstract fun loadMoreItems()
    abstract val isLastPage: Boolean

    private val TAG = "PaginationScrollListene"

}
