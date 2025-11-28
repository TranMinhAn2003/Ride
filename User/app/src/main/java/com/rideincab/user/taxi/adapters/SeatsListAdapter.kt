package com.rideincab.user.taxi.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rideincab.user.R

class SeatsListAdapter(private var onClickListener: OnClickListener, private val context: Context, private val seatslist: ArrayList<String>):RecyclerView.Adapter<SeatsListAdapter.SeatsAdapter>() {
   private var rowPosition=0
  // lateinit private var onClickListener : OnClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatsAdapter {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.layout_seat,parent,false)
        return SeatsAdapter(view)
    }

    override fun getItemCount(): Int {
      return seatslist.size
    }

    override fun onBindViewHolder(holder: SeatsAdapter, position: Int) {

        holder.tv_seat.text=seatslist.get(position)
        holder.tv_seat.setOnClickListener(View.OnClickListener {
            rowPosition=position;
            onClickListener.onClick(seatslist.get(position))
            notifyDataSetChanged()
        })
        if(rowPosition==position)
            holder.tv_seat.setBackgroundResource(R.drawable.circular_border)
        else
            holder.tv_seat.setBackgroundResource(R.drawable.circle_shape_light_gray)
          /*  holder.tv_seat.backgroundTintList=ContextCompat.getColorStateList(context,R.color.transparent)*/
    }


    interface OnClickListener {
        fun onClick(pos:String)
    }


    class SeatsAdapter(private var rowView: View):RecyclerView.ViewHolder(rowView)
    {
        internal val tv_seat:TextView
        init {
            tv_seat=rowView.findViewById(R.id.tv_seat)
        }
    }
}