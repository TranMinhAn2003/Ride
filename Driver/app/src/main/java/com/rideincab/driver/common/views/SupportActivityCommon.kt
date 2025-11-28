package com.rideincab.driver.common.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.Toast
import com.google.gson.Gson
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.model.CheckVersionModel
import com.rideincab.driver.common.model.Support
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.databinding.ActivitySupportCommonBinding
import java.util.*
import javax.inject.Inject


class SupportActivityCommon : CommonActivity(), SupportAdapter.OnClickListener {

    private lateinit var binding: ActivitySupportCommonBinding

    var supportList: ArrayList<Support> = ArrayList()

    lateinit var checkVersionModel: CheckVersionModel

    @Inject
    lateinit var commonMethods: CommonMethods

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportCommonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.getAppComponent().inject(this)
        commonMethods.setheaderText(resources.getString(R.string.support), binding.commonHeader.headertext)
        checkVersionModel = Gson().fromJson(sessionManager.checkVersionModel, CheckVersionModel::class.java)
        //commonMethods.imageChangeforLocality(this, arrow)
        initViews()
        binding.commonHeader.arrow.setOnClickListener { onBackPressed() }
    }

    private fun initViews() {
        supportList.clear()
        supportList.addAll(checkVersionModel.support)
        binding.rvSupportList.adapter = SupportAdapter(this, supportList, this)
    }

    override fun onClick(pos: Int) {
        9
        if (checkVersionModel.support[pos].id == 1) {
            onClickWhatsApp(checkVersionModel.support[pos].link)
        } else {
            redirectWeb(checkVersionModel.support[pos].link)
        }
    }

    private fun redirectWeb(link: String) {
        if (URLUtil.isValidUrl(link) || Patterns.WEB_URL.matcher(link).matches()) {
            val redirectLink: String =
                if (!(link.contains("https://") || link.contains("http://"))) {
                    "http://$link"
                } else {
                    link
                }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(redirectLink)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        } else {
            Toast.makeText(this, resources.getString(R.string.not_valid_data), Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun onClickWhatsApp(phoneNumberWithCountryCode: String) {
        //val phoneNumberWithCountryCode = "+9112345678"
        val message = ""

        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    String.format(
                        getString(R.string.whatsapp_url),
                        phoneNumberWithCountryCode,
                        message
                    )
                )
            )
        )

    }
}