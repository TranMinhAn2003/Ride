package com.rideincab.user.taxi.views.firebaseChat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys

import java.util.ArrayList

import javax.inject.Inject

class AdapterFirebaseRecylcerview(private val mContext: Context) : RecyclerView.Adapter<AdapterFirebaseRecylcerview.RecyclerViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(mContext)
    private val chatList = ArrayList<FirebaseChatModelClass>()

    @Inject
    lateinit var sessionManager: SessionManager

    init {
        AppController.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = inflater.inflate(R.layout.app_adapter_firebase_chat_single_row, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        if (chatList[position].type != null) {
            if (chatList[position].type == CommonKeys.FIREBASE_CHAT_TYPE_RIDER) {
                holder.myMessageCard.visibility = View.VISIBLE
                holder.opponentChatMessageLayout.visibility = View.GONE
                holder.myMessage.text = chatList[position].message
            } else {
                holder.opponentChatMessageLayout.visibility = View.VISIBLE
                holder.myMessageCard.visibility = View.GONE
                holder.opponentMessage.text = chatList[position].message

                handleOpponentProfilePicture(holder, position)
            }
        }
    }

    private fun handleOpponentProfilePicture(holder: RecyclerViewHolder, position: Int) {
        try {
            if (position != 0) {
                if (chatList[position].type != null && chatList[position - 1].type == CommonKeys.FIREBASE_CHAT_TYPE_RIDER) {
                    Picasso.get().load(sessionManager.driverProfilePic).error(R.drawable.car).into(holder.opponentProfileImageView)
                    holder.imageCardView.visibility = View.VISIBLE
                    holder.opponentProfileImageView.visibility = View.VISIBLE
                } else {
                    holder.opponentProfileImageView.visibility = View.INVISIBLE
                    holder.imageCardView.visibility = View.INVISIBLE
                }
            } else {
                Picasso.get().load(sessionManager.driverProfilePic).error(R.drawable.car).into(holder.opponentProfileImageView)
                holder.imageCardView.visibility = View.VISIBLE
                holder.opponentProfileImageView.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun updateChat(firebaseChatModelClass: FirebaseChatModelClass) {
        chatList.add(firebaseChatModelClass)
        notifyItemChanged(chatList.size - 1)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }


    class RecyclerViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var opponentMessage: TextView = itemView.findViewById(R.id.tv_opponent_message)
        internal var myMessage: TextView = itemView.findViewById(R.id.tv_my_message)
        internal var opponentProfileImageView: ImageView = itemView.findViewById(R.id.imgv_opponent_profile_pic)
        internal var imageCardView: CardView = itemView.findViewById(R.id.card_view)
        internal var myMessageCard: CardView = itemView.findViewById(R.id.cv_my_messages)
        internal var opponentChatMessageLayout: LinearLayout = itemView.findViewById(R.id.lv_opponnent_chat_messages)

    }
}