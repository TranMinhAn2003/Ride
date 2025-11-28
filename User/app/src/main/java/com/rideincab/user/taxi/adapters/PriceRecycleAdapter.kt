package com.rideincab.user.taxi.adapters

/**
 * @package com.cloneappsolutions.cabmeuserdriver.rating
 * @subpackage rating
 * @category CommentsRecycleAdapter
 * @author SMR IT Solutions
 * 
 */

import android.content.Context

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.rideincab.user.R
import com.rideincab.user.common.custompalette.FontCache
import com.rideincab.user.taxi.datamodels.trip.InvoiceModel
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods

import java.util.ArrayList

import javax.inject.Inject

/* ************************************************************
                CommentsRecycleAdapter
Its used to view the feedback comments with rider screen page function
*************************************************************** */
class PriceRecycleAdapter(private val context: Context, private val pricelist: ArrayList<InvoiceModel>) : RecyclerView.Adapter<PriceRecycleAdapter.ViewHolder>() {
    @Inject
    lateinit var commonMethods: CommonMethods
    lateinit var dialog: AlertDialog
    private val Other_reason: String? = null

    init {
        AppController.appComponent.inject(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.app_price_layout, viewGroup, false)

        return ViewHolder(view)
    }

    /*
     *  Get rider feedback list bind
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {

        viewHolder.fareTxt.text = pricelist[i].key
        viewHolder.fareAmt.text = pricelist[i].value.replace("\"", "")
        if (!pricelist[i].fareComments.equals("", ignoreCase = true)) {
            viewHolder.fareInfo.visibility = View.VISIBLE
        } else {
            viewHolder.fareInfo.visibility = View.GONE
        }



        if (pricelist[i].bar == "1") {

            //viewHolder.basrFareLayout.background = context.resources.getDrawable(R.drawable.d_topboarder)
            viewHolder.basrFareLayout.background = ContextCompat.getDrawable(context,R.drawable.d_topboarder)
            println("Key check feedback : " + pricelist[i].key)
        } else {
            viewHolder.basrFareLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
        }

        if (pricelist[i].colour == "black") {
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareTxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(ContextCompat.getColor(context,R.color.cabme_app_black_dark))
            viewHolder.fareTxt.setTextColor(ContextCompat.getColor(context,R.color.cabme_app_black_dark))
            viewHolder.fareTxt.textSize = 16F
            viewHolder.fareAmt.textSize = 16F
        }

        if (pricelist[i].colour == "yellow") {
            viewHolder.fareAmt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareTxt.typeface = FontCache.getTypeface(context.resources.getString(R.string.fonts_UBERMedium), context)
            viewHolder.fareAmt.setTextColor(ContextCompat.getColor(context,R.color.app_primary_color))
            viewHolder.fareTxt.setTextColor(ContextCompat.getColor(context,R.color.app_primary_color))
            viewHolder.fareTxt.textSize = 16F
            viewHolder.fareAmt.textSize = 16F
        }

        viewHolder.fareInfo.setOnClickListener {
            dialog = commonMethods.getAlertDialog(context)
            commonMethods.showMessage(context, dialog, pricelist[i].fareComments)
        }
        // if ()

    }

    override fun getItemCount(): Int {
        return pricelist.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
         val fareTxt: TextView
         val fareAmt: TextView
         val isBase: TextView
         val fareInfo: ImageView
         val rltPrice: LinearLayout
         val basrFareLayout: LinearLayout

        init {
            fareInfo = view.findViewById<View>(R.id.fareinfo) as ImageView
            fareTxt = view.findViewById<View>(R.id.faretxt) as TextView
            fareAmt = view.findViewById<View>(R.id.fareAmt) as TextView
            isBase = view.findViewById<View>(R.id.baseview) as TextView
            rltPrice = view.findViewById<View>(R.id.rltprice) as LinearLayout
            basrFareLayout = view.findViewById<View>(R.id.basrfarelayout) as LinearLayout

        }
    }


}
