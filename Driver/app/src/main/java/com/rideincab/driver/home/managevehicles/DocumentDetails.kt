package com.rideincab.driver.home.managevehicles

import android.os.Bundle
import android.util.Log
import android.widget.TextView

import com.rideincab.driver.R
import com.rideincab.driver.home.datamodel.DocumentsModel
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.common.util.CommonKeys
import com.rideincab.driver.common.util.CommonMethods
import com.rideincab.driver.common.views.CommonActivity
import com.rideincab.driver.common.views.applySystemBarsBottomPadding
import com.rideincab.driver.common.views.applySystemBarsPadding
import com.rideincab.driver.common.views.applySystemBarsTopPadding
import com.rideincab.driver.databinding.ActivityDocumentDetailsBinding
import javax.inject.Inject


class DocumentDetails : CommonActivity() {

    lateinit var binding: ActivityDocumentDetailsBinding

    @Inject
    lateinit var commonMethods: CommonMethods

    var documentDetails = ArrayList<DocumentsModel>()
    var documentPosition: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rltHeader.root.applySystemBarsTopPadding()
        binding.root.applySystemBarsBottomPadding()
        AppController.getAppComponent().inject(this)
        binding.rltHeader.ivBack.setOnClickListener { onBackPressed() }
        getIntentValues()
    }

    private fun getIntentValues() {

        if (intent.extras != null) {
            documentDetails =
                intent.getSerializableExtra(CommonKeys.Intents.DocumentDetailsIntent) as ArrayList<DocumentsModel>
            setHeader(getString(R.string.manage_documents))
        }

    }


    internal fun getAppCompatActivity(): CommonActivity {
        return this
    }


    fun setHeader(title: String) {
        try {
            Log.i("MNG_DOC", "setHeader: Doc title=$title")
            Log.i("MNG_DOC", "setHeader: Doc tvTitle is null=${(binding.rltHeader.tvTitle == null)}")
            if (null != binding.rltHeader.tvTitle)
                binding.rltHeader.tvTitle.text = title
        } catch (e: Exception) {
            Log.i("MNG_DOC", "setHeader: Error=${e.localizedMessage}")
        }
    }


}
