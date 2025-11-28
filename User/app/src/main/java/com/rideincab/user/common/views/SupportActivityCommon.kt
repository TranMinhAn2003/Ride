package com.rideincab.user.common.views

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.Toast
import com.google.gson.Gson
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.CheckVersionModel
import com.rideincab.user.common.datamodels.Support
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods
import com.rideincab.user.databinding.AppActivitySupportCommonBinding
import java.util.*
import javax.inject.Inject


class SupportActivityCommon : CommonActivity(), SupportAdapter.OnClickListener {
   
    private lateinit var binding: AppActivitySupportCommonBinding
    
    /*@BindView(R.id.rv_support_list)
    lateinit var binding.rvSupportList: RecyclerView*/

    var supportList: ArrayList<Support> = ArrayList()

    lateinit var checkVersionModel: CheckVersionModel

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var commonMethods: CommonMethods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= AppActivitySupportCommonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        AppController.appComponent.inject(this)
        checkVersionModel = Gson().fromJson(sessionManager.checkVersionModel, CheckVersionModel::class.java);

        /**Commmon Header Text View */
        commonMethods.setHeaderText(resources.getString(R.string.support), binding.commonHeader.tvHeadertext)
        initViews()

        binding.commonHeader.arrow.setOnClickListener {
            onBackPressed()
        }

    }

    private fun initViews() {
        supportList.clear()
        supportList.addAll(checkVersionModel.support)
        binding.rvSupportList.adapter = SupportAdapter(this, supportList, this)
    }

    override fun onClick(pos: Int) {
        if (checkVersionModel.support[pos].id == 1) {
            onClickWhatsApp(checkVersionModel.support[pos].link)
        } else {
            redirectWeb(checkVersionModel.support[pos].link)
        }

    }

   /* override fun onClick(pos: Int) {
        if (checkVersionModel.support[pos].id == 1) {
            onClickWhatsApp(checkVersionModel.support[pos].link)
        } else if (checkVersionModel.support[pos].id == 2) {
            openSkype(this, checkVersionModel.support[pos].link)
        } else {
            redirectWeb(checkVersionModel.support[pos].link)
        }

    }*/

    private fun redirectWeb(link: String) {
        if (URLUtil.isValidUrl(link) || Patterns.WEB_URL.matcher(link).matches()) {
            val redirectLink: String = if (!(link.contains("https://") || link.contains("http://"))) {
                "http://$link"
            } else {
                link
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(redirectLink)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        } else {
            Toast.makeText(this, resources.getString(R.string.not_valid_data), Toast.LENGTH_SHORT).show()
        }
    }


    private fun openSkype(context: Context, skypeId: String) {

        // Make sure the Skype for Android client is installed
      /*  if (!isSkypeClientInstalled(context)) {
            goToMarket(context)
            return
        }*/
        val mySkypeUri = "skype:" + skypeId + "?chat"
        // Create the Intent from our Skype URI.
        val skypeUri = Uri.parse(mySkypeUri)
        val myIntent = Intent(Intent.ACTION_VIEW, skypeUri)

        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK



        try {
            context.startActivity(myIntent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Determine whether the Skype for Android client is installed on this device.
     */
    fun isSkypeClientInstalled(myContext: Context): Boolean {
        val myPackageMgr: PackageManager = myContext.getPackageManager()
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    /**
     * Install the Skype client through the market: URI scheme.
     */
    fun goToMarket(myContext: Context) {
        val marketUri = Uri.parse("market://details?id=com.skype.raider")
        val myIntent = Intent(Intent.ACTION_VIEW, marketUri)
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        myContext.startActivity(myIntent)
    }

    fun onClickWhatsApp(phoneNumberWithCountryCode: String) {
        //val phoneNumberWithCountryCode = "+9112345678"
        val message = ""

        startActivity(
                Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format(getString(R.string.whatsapp_url), phoneNumberWithCountryCode, message))
                )
        )

    }
}