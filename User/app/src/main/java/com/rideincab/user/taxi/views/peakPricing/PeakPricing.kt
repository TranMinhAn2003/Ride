package com.rideincab.user.taxi.views.peakPricing

import android.os.Bundle
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys

import javax.inject.Inject


import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.AppPeekPricingBinding

class PeakPricing : CommonActivity() {


    @Inject
    lateinit var sessionManager: SessionManager

    fun acceptHigherPriceButtonClick() {
        setResult(CommonKeys.PEAK_PRICE_ACCEPTED)
        finish()
    }

    fun declinedPeakPriceButtonClick() {
        setResult(CommonKeys.PEAK_PRICE_DENIED)
        finish()
    }

    fun closeActivity() {
        declinedPeakPriceButtonClick()
    }


    private lateinit var binding: AppPeekPricingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= AppPeekPricingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        
        AppController.appComponent.inject(this)

        try {
            binding.tvPeakPricePercentage.text = intent.getStringExtra(CommonKeys.KEY_PEAK_PRICE) + "x"
            binding.tvMinimumFare.text = sessionManager.currencySymbol + intent.getStringExtra(CommonKeys.KEY_MIN_FARE)
            binding.tvPerMinutes.text = sessionManager.currencySymbol + intent.getStringExtra(CommonKeys.KEY_PER_MINUTES)
            binding.tvPerDistance.text = sessionManager.currencySymbol + intent.getStringExtra(CommonKeys.KEY_PER_KM)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.tvAcceptHigherPrice.setOnClickListener { acceptHigherPriceButtonClick() }
        binding.tvTryLaterHigherPrice.setOnClickListener { declinedPeakPriceButtonClick() }
        binding.imgvuCloseIcon.setOnClickListener { closeActivity() }

    }
}