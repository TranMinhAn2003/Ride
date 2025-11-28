package com.rideincab.user.taxi.views.main.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rideincab.user.common.network.AppController
import com.rideincab.user.databinding.UpdateFiltersBinding

class FeaturesInVehicleAdapter(private val featuresInCarModel: List<FeaturesInCarModel>, private var featureSelectListener: FeatureSelectListener) : RecyclerView.Adapter<FeaturesInVehicleAdapter.ViewHolder>() {
    init {
        AppController.appComponent.inject(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UpdateFiltersBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return featuresInCarModel.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(featuresInCarModel[position])
    }


    inner class ViewHolder(val binding: UpdateFiltersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(featuresInCarModel: FeaturesInCarModel) {
            binding.tvFilters.text = featuresInCarModel.name
            binding.cbSelectFilter.isChecked = featuresInCarModel.isSelected
            binding.cbSelectFilter.setOnCheckedChangeListener { buttonView, isChecked ->
                featureSelectListener.onFeatureChoosed(featuresInCarModel.id,isChecked)
            }
            binding.features = featuresInCarModel
            binding.executePendingBindings()
        }

    }
}