package com.rideincab.user.common.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.core.content.ContextCompat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.multidex.BuildConfig.APPLICATION_ID

import com.rideincab.user.R
import com.rideincab.user.common.views.applySystemBarsPadding
import com.rideincab.user.databinding.PopupPermissionHelperBinding

import java.util.Objects


/*
 * Created by: umasankar
 * description: this class will handle all runtime permission in single dialog fragment*/
class RuntimePermissionDialogFragment : DialogFragment() {

    private lateinit var binding: PopupPermissionHelperBinding

    //    icon Lists
    val cameraIcon = R.drawable.app_ic_permission_camera
    val locationIcon = R.drawable.app_ic_permission_location
    val storageIcon = R.drawable.app_ic_permission_gallery
    val contactIcon = R.drawable.app_ic_permission_contact
    val phoneIcon = R.drawable.app_ic_permission_phone
    val defaultIcon = cameraIcon

    private var requestCodeForCallbackIdentificationSubDivision: Int = 0


    //butterknife view binds
   /* @BindView(R.id.imgv_df_permissionImage)
    lateinit var permissionTypeImage: ImageView

    @BindView(R.id.tv_df_permissionAllow)
    lateinit var permissionAllow: TextView

    @BindView(R.id.binding.tvDfPermissionNotAllow)
    lateinit var permissionNotAllow: TextView

    @BindView(R.id.tv_df_permissionDescription)
    lateinit var tv_permissionDescription: TextView*/


    private val PERMISSION_REQUEST_CODE = 11
    private var permissionsRequestFor: Array<String>? = null
    private var mContext: Context? = null
    private var callbackListener: RuntimePermissionRequestedCallback? = null
    private var permissionIcon: Int = 0 // default 0
    private var permissionDescription: String? = null


    // this variable is declared to handle the allow Textview onClick process Dynamically,
    // if true -> it will request permission,
    // else open settings page to grand permission by user manually
    protected var ableToRequestPermission = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PopupPermissionHelperBinding.inflate(inflater, container, false)
        isCancelable = false
        Objects.requireNonNull(dialog!!.window)?.setBackgroundDrawableResource(android.R.color.transparent)
        setImageResourceAndPermissionDescriptionForPopup()
        binding.tvDfPermissionNotAllow.setOnClickListener { afterPermissionDenied() }
        binding.tvDfPermissionAllow.setOnClickListener { allowPermission() }
        binding.root.applySystemBarsPadding()
        return binding.root
    }

    fun notAllowPermission() {
        afterPermissionDenied()
    }

    fun allowPermission() {
        if (ableToRequestPermission) {
            requestNecessaryPermissions()
        } else {
            mContext!!.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + APPLICATION_ID)
                )
            )
            dismiss()
        }
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
        callbackListener = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (permissions.isNotEmpty() && grantResults.isNotEmpty()) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (!shouldShowRequestPermissionRationale(permission) && grantResult != PackageManager.PERMISSION_GRANTED) {
                    notAbleToRequestPermission()
                    return
                } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    afterPermissionDenied()
                    return
                }
            }
            callbackListener!!.permissionGranted(
                requestCodeForCallbackIdentifications,
                requestCodeForCallbackIdentificationSubDivision
            )
            dismiss()

        } else {
            Toast.makeText(mContext, "permission size 0", Toast.LENGTH_SHORT).show()
        }
    }

    fun setImageResourceAndPermissionDescriptionForPopup() {
        getPermissionRequestedForIconAndDescription()
        binding.imgvDfPermissionImage.setImageResource(permissionIcon)
        binding.tvDfPermissionDescription.text = permissionDescription
    }

    fun getPermissionRequestedForIconAndDescription() {
        when (permissionsRequestFor!![0]) {
            CAMERA_PERMISSION -> {
                permissionIcon = cameraIcon
                permissionDescription =
                    mContext!!.resources.getString(R.string.camera_permission_description)
            }

            WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                permissionIcon = storageIcon
                permissionDescription =
                    mContext!!.resources.getString(R.string.storage_permission_description)
            }

            LOCATION_PERMISSION -> {
                permissionIcon = locationIcon
                permissionDescription =
                    mContext!!.resources.getString(R.string.location_permission_description)
            }

            RECORD_AUDIO_PERMISSION -> {
                permissionIcon = phoneIcon
                permissionDescription =
                    mContext!!.resources.getString(R.string.audio_permission_description)
            }

            CONTACT_PERMISSION -> {
                permissionIcon = contactIcon
                permissionDescription =
                    mContext!!.resources.getString(R.string.contact_permission_description)
            }

            else -> {
                permissionIcon = cameraIcon
                permissionDescription =
                    mContext!!.resources.getString(R.string.camera_permission_description)
            }
        }
    }

    private fun requestNecessaryPermissions() {
        ableToRequestPermission = true
        requestPermissions(permissionsRequestFor!!, PERMISSION_REQUEST_CODE)
    }

    private fun notAbleToRequestPermission() {
        binding.tvDfPermissionAllow.text = mContext!!.resources.getString(R.string.settings)
        ableToRequestPermission = false

    }

    private fun afterPermissionDenied() {
        showPermissionDeniedMessageToUser()
        callbackListener!!.permissionDenied(
            requestCodeForCallbackIdentifications,
            requestCodeForCallbackIdentificationSubDivision
        )
        dismiss()
    }

    fun showPermissionDeniedMessageToUser() {
        Toast.makeText(
            mContext,
            mContext!!.resources.getString(R.string.enable_permissions_to_proceed_further),
            Toast.LENGTH_SHORT
        ).show()

    }

    interface RuntimePermissionRequestedCallback {
        fun permissionGranted(
            requestCodeForCallbackIdentificationCode: Int,
            requestCodeForCallbackIdentificationCodeSubDivision: Int
        )

        fun permissionDenied(
            requestCodeForCallbackIdentificationCode: Int,
            requestCodeForCallbackIdentificationCodeSubDivision: Int
        )
    }

    companion object {

        /*if you need to add permission
     * first add in permission list and then add icon list*/

        //    Permissions List
        val CAMERA_PERMISSION = Manifest.permission.CAMERA
        val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val READ_MEDIA_IMAGES_STORAGE_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
        val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        val CONTACT_PERMISSION = Manifest.permission.READ_CONTACTS
        val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
        val MODIFY_AUDIO_PERMISSION = Manifest.permission.MODIFY_AUDIO_SETTINGS
        val DEFAULT_PERMISSION_CODE = Manifest.permission.INTERNET
        //public final int storageIcon = android.R.drawable.sym_contact_card; // this may be used as alternative icon for SDcard access


        // request Codes For Callback Identifications
        const val cameraAndGallaryCallBackCode = 0
        const val externalStoreageCallbackCode = 1
        const val locationCallbackCode = 2
        const val contactCallbackCode = 3
        const val audioCallbackCode = 4
        private var requestCodeForCallbackIdentifications = 0

        fun checkPermissionStatus(
            mContext: Context,
            fragmentManager: FragmentManager,
            callbackListener: RuntimePermissionRequestedCallback,
            permissionsRequestFor: Array<String>,
            requestCodeForCallbackIdentification: Int,
            requestCodeForCallbackIdentificationSubDivision: Int
        ) {
            requestCodeForCallbackIdentifications = requestCodeForCallbackIdentification
            /*
         * here function check permission status and then checks shouldAskPermissionForThisAndroidOSVersion or not
         * because some custom phone below Android M request permission from user at Run time example: redmi phones*/

            var allPermissionGranted: Boolean? = true

            for (permissionRequestFor in permissionsRequestFor) {
                if (checkSelfPermissions(mContext, permissionRequestFor)) {
                    allPermissionGranted = false
                    break
                }
            }
            if (!(allPermissionGranted)!!) {
                if (shouldAskPermissionForThisAndroidOSVersion()) {
                    val runtimePermissionDialogFragment = RuntimePermissionDialogFragment()
                    runtimePermissionDialogFragment.permissionsRequestFor = permissionsRequestFor
                    runtimePermissionDialogFragment.callbackListener = callbackListener
                    runtimePermissionDialogFragment.requestCodeForCallbackIdentificationSubDivision =
                        requestCodeForCallbackIdentificationSubDivision
                    runtimePermissionDialogFragment.mContext = mContext
                    runtimePermissionDialogFragment.show(
                        fragmentManager,
                        RuntimePermissionDialogFragment::class.java.name
                    )
                } else {
                    //                we write code here becoz of static method, it not static method we call afterPermissionDenied()
                    callbackListener.permissionDenied(
                        requestCodeForCallbackIdentification,
                        requestCodeForCallbackIdentificationSubDivision
                    )
                    Toast.makeText(
                        mContext,
                        mContext.resources.getString(R.string.enable_permissions_to_proceed_further),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } else {
                callbackListener.permissionGranted(
                    requestCodeForCallbackIdentification,
                    requestCodeForCallbackIdentificationSubDivision
                )
            }
        }

        fun checkSelfPermissions(mContext: Context, permissionRequestFor: String): Boolean {
            return ContextCompat.checkSelfPermission(
                mContext,
                permissionRequestFor
            ) != PackageManager.PERMISSION_GRANTED
        }

        fun shouldAskPermissionForThisAndroidOSVersion(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }
    }
}
