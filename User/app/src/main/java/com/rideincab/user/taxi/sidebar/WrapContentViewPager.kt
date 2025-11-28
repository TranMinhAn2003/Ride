package com.rideincab.user.taxi.sidebar

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar
 * @category WrapContentViewPager
 * @author SMR IT Solutions
 * 
 */

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.View

/* ************************************************************
   Custom view pager
    *********************************************************** */
class WrapContentViewPager : ViewPager {
    private var mCurrentPagePosition = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    /**
     * Wrap content size measure
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpecs: Int) {
        var heightMeasureSpec = heightMeasureSpecs
        try {
            val child = getChildAt(mCurrentPagePosition)
            if (child != null) {
                child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                val h = child.measuredHeight
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun reMeasureCurrentPage(position: Int) {
        mCurrentPagePosition = position
        requestLayout()
    }
}
