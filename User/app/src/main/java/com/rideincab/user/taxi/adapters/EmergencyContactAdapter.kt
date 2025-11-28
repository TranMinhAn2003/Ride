package com.rideincab.user.taxi.adapters

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage emergency
 * @category Emergency contact adapter
 * @author SMR IT Solutions
 * 
 */

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView

import com.rideincab.user.R
import com.rideincab.user.taxi.datamodels.EmergencyContactModel

import java.util.ArrayList

/* ************************************************************************
                Adapter for  Emergency contacts
*************************************************************************** */

class EmergencyContactAdapter
/**
 * EmergencyContactAdapter Constructor to intialize getEmergencyContactDetails and Context context
 *
 * @param getEmergencyContactDetails array list
 * @param context                    context of the emergency context
 */
(var getEmergencyContactDetails: ArrayList<EmergencyContactModel>, var context: Context) : RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder>() {
    var alterpostion: Int = 0
    lateinit var id1: String
    private val inflater: LayoutInflater
    lateinit private var mItemClickListener: OnItemClickListener

    init {
        inflater = LayoutInflater.from(context)
        for (i in getEmergencyContactDetails.indices) {

        }
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.app_emergency_contact_detail, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.testName.text = getEmergencyContactDetails[position].name
        id1 = getEmergencyContactDetails[position].id.toString()
        holder.testPhoneNumber.text = getEmergencyContactDetails[position].mobileNumber
        holder.deleteMenu.setOnClickListener { v ->
            val popupMenu = PopupMenu(context, v)
            popupMenu.inflate(R.menu.emergency_delete)
            popupMenu.setOnMenuItemClickListener { item ->
                // TODO Auto-generated method stub
                val id = item.itemId
                if (id == R.id.delete_menu) {
                    alterpostion = position
                    val mobilenumber = getEmergencyContactDetails[position].mobileNumber.replace(" ".toRegex(), "")
                    getEmergencyContactDetails[position].name.let { getEmergencyContactDetails[position].id.let { it1 -> mItemClickListener.onItemClickListener(mobilenumber, it, it1, position.toString()) } }
                }
                true
            }

            popupMenu.show()
        }


    }

    override fun getItemCount(): Int {
        return getEmergencyContactDetails.size
    }

    interface OnItemClickListener {
        fun onItemClickListener(number: String, name: String, id: String, positionz: String)
    }

    /**
     * View Holder class for emergency contact
     */

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
         val testName: TextView
         val testPhoneNumber: TextView
         val deleteMenu: ImageView

        init {

            testName = view.findViewById<View>(R.id.nameofcontact) as TextView
            testPhoneNumber = view.findViewById<View>(R.id.numberofcontact) as TextView
            deleteMenu = view.findViewById<View>(R.id.deletemenu) as ImageView

        }
    }

}