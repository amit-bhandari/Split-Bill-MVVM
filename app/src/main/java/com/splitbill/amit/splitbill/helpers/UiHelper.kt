package com.splitbill.amit.splitbill.helpers

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Drawable
import com.splitbill.amit.splitbill.MyApp
import com.splitbill.amit.splitbill.R


fun getBackGradient(): Drawable {
    val colors = intArrayOf(MyApp.instance.resources.getColor(R.color.gradient1)
       // ,MyApp.instance.resources.getColor(R.color.gradient2)
        ,MyApp.instance.resources.getColor(R.color.gradient3))

    return GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        colors
    )
}