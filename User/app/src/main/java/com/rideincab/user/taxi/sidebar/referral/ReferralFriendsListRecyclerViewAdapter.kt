package com.rideincab.user.taxi.sidebar.referral

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.taxi.sidebar.referral.model.CompletedOrPendingReferrals

import com.rideincab.user.common.utils.CommonKeys.CompletedReferralArray
import com.rideincab.user.common.utils.CommonKeys.IncompleteReferralArray

class ReferralFriendsListRecyclerViewAdapter(private val mContext: Context, internal var referredFriendsModelArrayList: List<CompletedOrPendingReferrals>?, private val referralArrayType: Int) : RecyclerView.Adapter<ReferralFriendsListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_recyclerview_adapter_referral_friends_status_sing_row, parent, false)


        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val referredFriendsModelArrayListSingleObject = referredFriendsModelArrayList!![position]

        holder.tvReferredFriend.text = referredFriendsModelArrayListSingleObject.name
        holder.tvAmountGain.text = referredFriendsModelArrayListSingleObject.earnableAmount

        if (referralArrayType == IncompleteReferralArray) {
            if (referredFriendsModelArrayListSingleObject.remainingTrips != 0L && referredFriendsModelArrayListSingleObject.remainingDays != 0L) {
                val tripsAndDaysRemainingText = StringBuilder(referredFriendsModelArrayListSingleObject.remainingDays.toString() + " " + mContext.getString(R.string.days_left) + " | " + mContext.getString(R.string.need_to_complete) + " " + referredFriendsModelArrayListSingleObject.remainingTrips + " " + mContext.getString(R.string.trips))
                holder.tvRemainingDaysAndTrips.text = tripsAndDaysRemainingText
            } else {
                holder.tvRemainingDaysAndTrips.visibility = View.GONE
            }
        } else if (referralArrayType == CompletedReferralArray) {
            if (referredFriendsModelArrayListSingleObject.status == "Expired") {
                holder.tvRemainingDaysAndTrips.setTextColor(ContextCompat.getColor(mContext,R.color.red_text_color))
                holder.tvRemainingDaysAndTrips.text = mContext.getString(R.string.referral_expired)
            } else if (referredFriendsModelArrayListSingleObject.status == "Completed") {
                holder.tvRemainingDaysAndTrips.visibility = View.GONE
                /*holder.tvRemainingDaysAndTrips.setTextColor(mContext.getResources().getColor(R.color.ub__green));
                holder.tvRemainingDaysAndTrips.setText(mContext.getString(R.string.referral_completed));*/
            } else {
                //empty else
            }
        } else {
            //empty else
        }


        try {
            Picasso.get().load(referredFriendsModelArrayListSingleObject.profileImage).into(holder.profilePicture)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun getItemCount(): Int {
        return referredFriendsModelArrayList!!.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
         val profilePicture: ImageView
         val tvReferredFriend: TextView
         val tvRemainingDaysAndTrips: TextView
         val tvAmountGain: TextView


        init {

            profilePicture = view.findViewById(R.id.profile_image1)
            tvReferredFriend = view.findViewById(R.id.tv_referral_friend_name)
            tvRemainingDaysAndTrips = view.findViewById(R.id.tv_remaining_days_and_trips)
            tvAmountGain = view.findViewById(R.id.tv_amount_gain)

        }
    }
}
