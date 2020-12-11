package com.offlinerecharge.utils

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.animation.Interpolator
import androidx.viewpager.widget.ViewPager
import com.notebook.android.utility.autoScroll.DurationScroller
import java.lang.ref.WeakReference

class AutoScrollViewPager : ViewPager {
    /**
     * get auto scroll time in milliseconds, default is [.DEFAULT_INTERVAL].
     *
     * @return the interval.
     */
    /**
     * set auto scroll time in milliseconds, default is [.DEFAULT_INTERVAL].
     *
     * @param interval the interval to set.
     */
    var interval = DEFAULT_INTERVAL.toLong()
    private var direction = RIGHT

    /**
     * whether automatic cycle when auto scroll reaching the last or first item, default is true.
     *
     * @return the isCycle.
     */
    var isCycleScroll = true
        private set
    /**
     * whether stop auto scroll when touching, default is true.
     *
     * @return the stopScrollWhenTouch.
     */
    /**
     * set whether stop auto scroll when touching, default is true.
     */
    var isStopScrollWhenTouch = true
    /**
     * get how to process when sliding at the last or first item.
     *
     * @return the slideBorderMode [.SLIDE_BORDER_MODE_NONE],
     * [.SLIDE_BORDER_MODE_TO_PARENT],
     * [.SLIDE_BORDER_MODE_CYCLE], default is [.SLIDE_BORDER_MODE_NONE]
     */
    /**
     * set how to process when sliding at the last or first item.
     *
     * @param slideBorderMode [.SLIDE_BORDER_MODE_NONE], [.SLIDE_BORDER_MODE_TO_PARENT],
     * [.SLIDE_BORDER_MODE_CYCLE], default is [.SLIDE_BORDER_MODE_NONE]
     */
    var slideBorderMode = SLIDE_BORDER_MODE_NONE

    /**
     * whether animating when auto scroll at the last or first item, default is true.
     */
    var isBorderAnimationEnabled = true
        private set
    private var autoScrollFactor = 1.0
    private var swipeScrollFactor = 1.0

    private var scroller: DurationScroller? = null

    constructor(paramContext: Context?) : super(paramContext!!) {
        init()
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?) : super(paramContext!!, paramAttributeSet) {
        init()
    }

    private fun init() {
        Companion.handler = MyHandler(this)
        setViewPagerScroller()
    }

    /**
     * start auto scroll, first scroll delay time is [.getInterval].
     */
    fun startAutoScroll() {
        if (scroller != null) {
            sendScrollMessage(
                    (interval + scroller!!.duration / autoScrollFactor * swipeScrollFactor).toLong())
        }
    }

    /**
     * start auto scroll.
     *
     * @param delayTimeInMills first scroll delay time.
     */
    fun startAutoScroll(delayTimeInMills: Int) {
        sendScrollMessage(delayTimeInMills.toLong())
    }

    /**
     * stop auto scroll.
     */
    fun stopAutoScroll() {
        handler!!.removeMessages(SCROLL_WHAT)
    }

    /**
     * set the factor by which the duration of sliding animation will change while swiping.
     */
    fun setSwipeScrollDurationFactor(scrollFactor: Double) {
        swipeScrollFactor = scrollFactor
    }

    /**
     * set the factor by which the duration of sliding animation will change while auto scrolling.
     */
    fun setAutoScrollDurationFactor(scrollFactor: Double) {
        autoScrollFactor = scrollFactor
    }

    private fun sendScrollMessage(delayTimeInMills: Long) {
        /** remove messages before, keeps one message is running at most  */
        Companion. handler!!.removeMessages(SCROLL_WHAT)
        Companion.handler!!.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills)
    }

    /**
     * set ViewPager scroller to change animation duration when sliding.
     */
    private fun setViewPagerScroller() {
        try {
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            val interpolatorField = ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolatorField.isAccessible = true
            scroller = DurationScroller(context, interpolatorField[null] as Interpolator)
            scrollerField[this] = scroller
        } catch (e: IllegalAccessException) {
           Log.e("e", "setViewPagerScroller: ")
            //  Timber.e(e);
        } catch (e: NoSuchFieldException) {
            Log.e("e", "setViewPagerScroller: ")
            // Timber.e(e);
        }
    }

    /**
     * scroll only once.
     */
    fun scrollOnce() {
        val adapter = adapter
        var currentItem = currentItem
        val totalCount = adapter?.count ?: -100
        if (adapter == null || totalCount <= 1) {
            return
        }
        val nextItem = if (direction == LEFT) --currentItem else ++currentItem
        if (nextItem < 0) {
            if (isCycleScroll) {
                setCurrentItem(totalCount - 1, isBorderAnimationEnabled)
            }
        } else if (nextItem == totalCount) {
            if (isCycleScroll) {
                setCurrentItem(0, isBorderAnimationEnabled)
            }
        } else {
            setCurrentItem(nextItem, true)
        }
    }

    private class MyHandler(autoScrollViewPager: AutoScrollViewPager) : Handler() {
        private val autoScrollViewPager: WeakReference<AutoScrollViewPager> = WeakReference(autoScrollViewPager)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == SCROLL_WHAT) {
                val pager = autoScrollViewPager.get()
                if (pager != null && pager.scroller != null) {
                    pager.scroller!!.setScrollDurationFactor(pager.autoScrollFactor)
                    pager.scrollOnce()
                    pager.scroller!!.setScrollDurationFactor(pager.swipeScrollFactor)
                    pager.sendScrollMessage(pager.interval + pager.scroller!!.duration)
                }
            }
        }

    }

    /**
     * get auto scroll direction.
     *
     * @return [.LEFT] or [.RIGHT], default is [.RIGHT]
     */
    fun getDirection(): Int {
        return if (direction == LEFT) LEFT else RIGHT
    }

    /**
     * set auto scroll direction.
     *
     * @param direction [.LEFT] or [.RIGHT], default is [.RIGHT]
     */
    fun setDirection(direction: Int) {
        this.direction = direction
    }

    /**
     * set whether automatic cycle when auto scroll reaching the last or first item, default is true.
     *
     * @param isCycle the isCycle to set.
     */
    fun setCycle(isCycle: Boolean) {
        isCycleScroll = isCycle
    }

    /**
     * set whether animating when auto scroll at the last or first item, default is true.
     */
    fun setBorderAnimation(isBorderAnimation: Boolean) {
        isBorderAnimationEnabled = isBorderAnimation
    }

    companion object {
        const val DEFAULT_INTERVAL = 1500
        const val LEFT = 0
        const val RIGHT = 1
        private const val TAG = "AutoScrollViewPager"
        const val SLIDE_BORDER_MODE_NONE = 0
        const val SLIDE_BORDER_MODE_CYCLE = 1
        const val SLIDE_BORDER_MODE_TO_PARENT = 2
        const val SCROLL_WHAT = 0
        var handler : Handler = Handler()

    }
}