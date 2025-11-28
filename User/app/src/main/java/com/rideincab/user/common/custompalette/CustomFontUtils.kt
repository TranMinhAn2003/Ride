package com.rideincab.user.common.custompalette

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage custompalette
 * @category customfonts
 * @author SMR IT Solutions
 *
 */

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.rideincab.user.R

/* ************************************************************
                CustomFontUtils
Used for user custom fonts if you want to change the font name to change from string file
*************************************************************** */

object CustomFontUtils {

    /**
     * Apply for custom fonts
     */
    fun applyCustomFont(customFontTextView: TextView, context: Context, attrs: AttributeSet?) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView)

        val fontName = attributeArray.getString(R.styleable.FontTextView_fontName)

        val customFont = selectTypeface(context, fontName!!)
        customFontTextView.typeface = customFont

        attributeArray.recycle()
    }

    /**
     * Apply for custom fonts
     */
    fun applyCustomFont(customFontTextView: EditText, context: Context, attrs: AttributeSet) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView)

        val fontName = attributeArray.getString(R.styleable.FontTextView_fontName)


        val customFont = selectTypeface(context, fontName!!)
        customFontTextView.typeface = customFont

        attributeArray.recycle()
    }

    /**
     * Apply for custom fonts
     */
    fun applyCustomFont(customFontTextView: Button, context: Context, attrs: AttributeSet?) {
        val attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.FontTextView)

        val fontName = attributeArray.getString(R.styleable.FontTextView_fontName)

        val customFont = selectTypeface(context, fontName!!)
        customFontTextView.typeface = customFont

        attributeArray.recycle()
    }

    /**
     * Select font typeface
     */
    private fun selectTypeface(context: Context, fontName: String): Typeface? {
        return if (fontName.contentEquals(context.resources.getString(R.string.font_PermanentMarker))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_PermanentMarker), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_Book))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_Book), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_Medium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_Medium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_NarrBook))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_NarrBook), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_NarrMedium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_NarrMedium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_NarrNews))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_NarrNews), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_News))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_News), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_WideMedium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_WideMedium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_WideNews))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_WideNews), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UBERBook))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERBook), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UBERMedium))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UBERNews))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERNews), context)
        } else if (fontName.contentEquals(context.resources.getString(R.string.font_UberClone))) {
            FontCache.getTypeface(context.resources.getString(R.string.fonts_UberClone), context)
        } else {
            null
        }
    }
}
