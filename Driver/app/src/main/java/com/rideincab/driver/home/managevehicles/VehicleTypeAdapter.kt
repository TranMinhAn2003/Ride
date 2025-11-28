package com.rideincab.driver.home.managevehicles

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.rideincab.driver.databinding.VehicleLayoutBinding
import com.rideincab.driver.home.datamodel.VehicleTypes
import com.rideincab.driver.common.network.AppController


class VehicleTypeAdapter(context : Context,private val vehiclesModelListSelected: List<VehicleTypes>,private val vehiclesModelList: List<VehicleTypes>,var onClickListener : OnClickListener) : RecyclerView.Adapter<VehicleTypeAdapter.ViewHolder>(){
    var selectedItem = -1  // Check selected item from list
    var first = true  // Check car is first
    var isItemChanged = false  // Check car is first

    init {
        AppController.getAppComponent().inject(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = VehicleLayoutBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = vehiclesModelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(vehiclesModelList[position])

    }


    interface OnClickListener {
        fun onClick(pos:Int,isChecked : Boolean)
    }



    inner class ViewHolder(val binding: VehicleLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicleType: VehicleTypes) {

            var i = 0
            while(i<vehiclesModelListSelected.size){

                if(vehicleType.id.equals(vehiclesModelListSelected.get(i).id)){
                    binding.cbVehicleType.isChecked = true
                    vehiclesModelList.get(adapterPosition).isChecked = true
                    if(vehiclesModelListSelected.get(i).location == ""){
                        binding.tvLocation.visibility = View.GONE
                    }else{
                        binding.tvLocation.visibility = View.VISIBLE
                    }
                    break
                }else{
                    binding.cbVehicleType.isChecked = false
                }
                i++
            }


            binding.cbVehicleType.setOnCheckedChangeListener { buttonView, isChecked ->
                vehiclesModelList.get(adapterPosition).isChecked = isChecked
            }


            binding.vehicleType = vehicleType

            binding.executePendingBindings()
        }
    }


}

