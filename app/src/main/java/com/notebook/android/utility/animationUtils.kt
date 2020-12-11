package com.notebook.android.utility

import android.view.View
import android.view.animation.TranslateAnimation

fun View.slideUp(duration: Int = 500) {
    visibility = View.GONE
    val animate = TranslateAnimation(0f, 0f, this.height.toFloat(), 0f)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.slideDown(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(0f, 0f, 0f, this.height.toFloat())
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}