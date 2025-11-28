package com.rideincab.user.common.utils

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage utils
 * @category CommonMethods
 * @author SMR IT Solutions
 *
 */

import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rideincab.user.BuildConfig
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.datamodels.JsonResponse
import com.rideincab.user.common.helper.CommonDialog
import com.rideincab.user.common.helper.Constants
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.pushnotification.Config
import com.rideincab.user.common.pushnotification.MyFirebaseMessagingService
import com.rideincab.user.common.pushnotification.NotificationUtils
import com.rideincab.user.common.views.CommonActivity
import com.rideincab.user.taxi.firebase_keys.FirebaseDbKeys
import com.rideincab.user.taxi.sendrequest.DriverRatingActivity
import com.rideincab.user.taxi.sendrequest.SendingRequestActivity
import com.rideincab.user.taxi.views.customize.CustomDialog
import com.rideincab.user.taxi.views.firebaseChat.ActivityChat
import com.rideincab.user.taxi.views.main.MainActivity

import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.jvm.Throws

/*****************************************************************
 * CommonMethods
 */
class CommonMethods {
    @Inject
    lateinit var sessionManager: SessionManager
    lateinit var context: Context
    private var mFirebaseDatabase: DatabaseReference? = null
    private val TAG = MyFirebaseMessagingService::class.java.simpleName

    lateinit var mProgressDialog: Dialog

    private var stripe: Stripe? = null
    private var overlayPermissionGranted = true


    init {
        AppController.appComponent.inject(this)
    }

    fun showProgressDialog(context: Context) {
        try {
            if (this::mProgressDialog.isInitialized && mProgressDialog != null && mProgressDialog.isShowing) {
                mProgressDialog.dismiss()
            }
            mProgressDialog = getLoadingDialog(context, R.layout.app_loader_view)
            mProgressDialog.setCancelable(true)
            mProgressDialog.setCanceledOnTouchOutside(false)
            mProgressDialog.setOnKeyListener { dialog, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
            mProgressDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getDialog(mContext: Context, mLayout: Int): Dialog {
        val mDialog = Dialog(mContext)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mDialog.setContentView(mLayout)
        mDialog.window!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        return mDialog
    }


    fun hideProgressDialog() {
        if (this::mProgressDialog.isInitialized && mProgressDialog.isShowing) {
            try {
                mProgressDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun dismissDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                mProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLoadingDialog(mContext: Context, mLay: Int): Dialog {

        val mDialog = getDialog(mContext, mLay)
        mDialog.setCancelable(true)
        mDialog.setCanceledOnTouchOutside(true)

        mDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        mDialog.window!!.setGravity(Gravity.CENTER)

        return mDialog
    }


    /*fun getDialog(mContext: Context, mLayout: Int): Dialog {

        val mDialog = Dialog(mContext, R.style.AppTheme)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        mDialog.setContentView(mLayout)
        mDialog.window!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)
        mDialog.setCancelable(true)
        mDialog.setCanceledOnTouchOutside(false)
        val msgTxt: TextView = mDialog.findViewById(R.id.tv_loading)
        msgTxt.text = "This might take some few seconds"
        return mDialog
    }*/

    fun showProgressDialog(mActivity: CommonActivity?, customDialog: CustomDialog?) {
        if (mActivity == null || customDialog == null || customDialog.dialog != null && customDialog.dialog!!.isShowing)
            return
        progressDialog = CustomDialog(true, mActivity.resources.getString(R.string.loading))
        progressDialog!!.show(mActivity.supportFragmentManager, "")
    }

    /*fun hideProgressDialog() {
        if (progressDialog == null || progressDialog!!.dialog == null || !progressDialog!!.dialog?.isShowing!!)
            return
        progressDialog!!.dismissAllowingStateLoss()
        progressDialog = null
    }*/


    fun showProgressDialogPaypal(mActivity: CommonActivity?, customDialog: CustomDialog?, message: String = "") {
        if (mActivity == null || customDialog == null || customDialog.dialog != null && customDialog.dialog!!.isShowing)
            return



        progressDialogPaypal = CustomDialog(true, message)
        progressDialogPaypal!!.show(mActivity.supportFragmentManager, "")
    }

    fun hideProgressDialogPaypal() {
        println("Progress dialog dismiss : one ")
        if (progressDialogPaypal == null || progressDialogPaypal!!.dialog == null || !progressDialogPaypal!!.dialog?.isShowing!!)
            return

        println("Progress dialog dismiss : two " + !progressDialogPaypal!!.dialog?.isShowing!!)
        progressDialogPaypal!!.dismissAllowingStateLoss()
        progressDialogPaypal = null
    }


    fun isProgressDialogShowing(): Boolean {
        return progressDialogPaypal!!.dialog?.isShowing!!
    }

    //Show Dialog with message
    fun showMessage(context: Context?, dialog: AlertDialog?, msg: String) {
        if (context != null && dialog != null && !(context as Activity).isFinishing) {
            try {
                dialog.setMessage(msg)
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    //Create and Get Dialog
    fun getAlertDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setPositiveButton(context.resources.getString(R.string.ok_c)) { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        return dialog
    }


    ////////////////// CAMERA ///////////////////////////////////
    fun getDefaultFileName(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), context.resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png")
    }


    fun isNotEmpty(s: Any?): Boolean {
        if (s == null) {
            return false
        }
        if (s is String && s.trim { it <= ' ' }.length > 0) {
            return true
        }
        if (s is ArrayList<*>) {
            return !s.isEmpty()
        }
        if (s is Map<*, *>) {
            return !s.isEmpty()
        }
        if (s is List<*>) {
            return !s.isEmpty()
        }

        return if (s is Array<*>) {
            s.size != 0
        } else false
    }

    /* fun imageChangeforLocality(context: Context, image: ImageView) {
         if (context.resources.getString(R.string.layout_direction) == "1") {
             image.rotation = 180f
         } else
             image.rotation = 0f

     }
 */

    /* public boolean isTaped() {
        if (SystemClock.elapsedRealtime() - Constants.mLastClickTime < 1000) return true;
        Constants.mLastClickTime = SystemClock.elapsedRealtime();
        return false;
    }*/

    fun cameraFilePath(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), context.resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png")
    }

    fun refreshGallery(context: Context, file: File) {
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file) //out is your file you saved/deleted/moved/copied
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    ////////////////// CAMERA ///////////////////////////////////
    /*  public File getDefaultFileName(Context context) {
        File imageFile;
        Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (isSDPresent) { // External storage path
            imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.FILE_NAME + System.currentTimeMillis() + ".png");
        } else {  // Internal storage path
            imageFile = new File(context.getFilesDir() + File.separator + Constants.FILE_NAME + System.currentTimeMillis() + ".png");
        }
        return imageFile;
    }*/

    fun clearFileCache(path: String) {
        try {
            if (!TextUtils.isEmpty(path)) {
                val file = File(path)
                if (file.exists() && !file.isDirectory) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isOnline(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    fun getJsonValue(jsonString: String, key: String, `object`: Any): Any {
        var object1 = `object`
        try {
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.has(key)) object1 = jsonObject.get(key)
        } catch (e: Exception) {
            e.printStackTrace()
            return Any()
        }

        return object1
    }

    /**
     * This ThreeDSecureRequest for Custom Ui
     * It may differ for Custom UI
     * @return ThreeDSecureRequest For 3D Secure
     */
    //Old version code
    /*fun threeDSecureRequest(amount: String): ThreeDSecureRequest {
        val address = ThreeDSecurePostalAddress()
                .givenName(sessionManager!!.firstName)
                .phoneNumber(sessionManager!!.phoneNumber)

        val additionalInformation = ThreeDSecureAdditionalInformation()
                .shippingAddress(address)

        return ThreeDSecureRequest()
                .amount(amount)
                .billingAddress(address)
                .versionRequested(ThreeDSecureRequest.VERSION_2)
                .additionalInformation(additionalInformation)
    }*/

    /* fun threeDSecureRequest(amount: String): ThreeDSecureRequest {
         val address = ThreeDSecurePostalAddress()
         address.givenName = sessionManager!!.firstName
         address.phoneNumber = sessionManager!!.phoneNumber

         val additionalInformation = ThreeDSecureAdditionalInformation()
         additionalInformation.shippingAddress = address

         val threeDSecureRequest = ThreeDSecureRequest()
         threeDSecureRequest.amount = amount
         threeDSecureRequest.billingAddress = address
         threeDSecureRequest.versionRequested = ThreeDSecureRequest.VERSION_2
         threeDSecureRequest.additionalInformation = additionalInformation

         return threeDSecureRequest

     }*/

    companion object {

        var popupWindow: PopupWindow? = null
        var progressDialog: CustomDialog? = null
        var progressDialogPaypal: CustomDialog? = null
        fun gotoMainActivityWithoutHistory(mActivity: Activity) {
            val mainActivityIntent = Intent(mActivity, MainActivity::class.java)
            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mActivity.startActivity(mainActivityIntent)

        }

        fun getCardImage(brand: String, getRes: Resources): Drawable {
            val card: Drawable
            when (brand) {
                "Visa" -> {
                    card = getRes.getDrawable(R.drawable.card_visa)
                }
                "MasterCard" -> {
                    card = getRes.getDrawable(R.drawable.card_master)
                    //card = ContextCompat.getDrawable(AppController.getContext(),R.drawable.card_master)!!
                }
                "Discover" -> {
                    card = getRes.getDrawable(R.drawable.card_discover)
                }
                "Amex", "American Express" -> {
                    card = getRes.getDrawable(R.drawable.card_amex)
                }
                "JCB", "JCP" -> {
                    card = getRes.getDrawable(R.drawable.card_jcp)
                }
                "Diner", "Diners" -> {
                    card = getRes.getDrawable(R.drawable.card_diner)
                }
                "UnionPay", "Union" -> {
                    card = getRes.getDrawable(R.drawable.card_unionpay)
                }
                else -> {
                    card = getRes.getDrawable(R.drawable.card)
                }
            }

            return card
            /*if (brand.contains("Visa")) {
            card = getRes.getDrawable(R.drawable.card_visa);
        } else if (brand.contains("MasterCard")) {
            card = getRes.getDrawable(R.drawable.card_master);
        } else if (brand.contains("Discover")) {
            card = getRes.getDrawable(R.drawable.card_discover);
        } else if (brand.contains("Amex") || brand.contains("American Express")) {
            card = getRes.getDrawable(R.drawable.card_amex);
        } else if (brand.contains("JCB") || brand.contains("JCP")) {
            card = getRes.getDrawable(R.drawable.card_jcp);
        } else if (brand.contains("Diner") || brand.contains("Diners")) {
            card = getRes.getDrawable(R.drawable.card_diner);
        } else if ("Union".contains(brand) || "UnionPay".contains(brand)) {
            card = getRes.getDrawable(R.drawable.card_unionpay);
        } else {
            card = getRes.getDrawable(R.drawable.card_basic);
        }*/
            // return getRes.getDrawable(R.drawable.card_basic);

        }

        /*public void dropDownMenu(AppCompatActivity mActivity, View v, int[] images, final String[] listItems, final DropDownClickListener listener) {
        LayoutInflater layoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        *//*View popupView = layoutInflater.inflate(R.layout.drop_down_menu_list, null);
        ListView listView = (ListView) popupView.findViewById(R.id.drop_down_list);

        DropDownMenuAdapter adapter = new DropDownMenuAdapter(mActivity, R.layout.drop_down_menu_row, images, listItems);
        listView.setAdapter(adapter);
        listView.setSelected(false);*//*

        Rect displayFrame = new Rect();
        v.getWindowVisibleDisplayFrame(displayFrame);

        int displayFrameWidth = displayFrame.right - displayFrame.left;
        int[] loc = new int[2];
        v.getLocationInWindow(loc);

        if (popupWindow == null) {
            int width = (int) (120 * Resources.getSystem().getDisplayMetrics().density);
            popupWindow = new PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        if (!popupWindow.isShowing()) {
            popupView.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.popup_anim_in));
            int margin = (displayFrameWidth - (loc[0] + v.getWidth()));
            int xOff = (displayFrameWidth - margin - popupWindow.getWidth()) - loc[0];
            popupWindow.showAsDropDown(v, (int) (xOff / 0.8), 0);
            popupWindow.setAnimationStyle(R.anim.popup_anim_in);

        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (popupWindow != null) popupWindow = null;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (popupWindow != null && popupWindow.isShowing()) popupWindow.dismiss();
                listener.onDropDrownClick(listItems[position]);
            }
        });
    }

    public boolean isSupportTransition() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }*/

        fun startChatActivityFrom(mActivity: Activity) {
            val startActivityIntent = Intent(mActivity, ActivityChat::class.java)
            startActivityIntent.putExtra(CommonKeys.FIREBASE_CHAT_ACTIVITY_SOURCE_ACTIVITY_TYPE_CODE, CommonKeys.FIREBASE_CHAT_ACTIVITY_REDIRECTED_FROM_RIDER_OR_DRIVER_PROFILE)
            mActivity.startActivity(startActivityIntent)
        }

        fun getAppVersionNameFromGradle(): String {
            var versionName: String
            try {
                versionName = AppController.getContext().packageManager.getPackageInfo(AppController.getContext().packageName, 0).versionName.toString()
            } catch (e: PackageManager.NameNotFoundException) {
                versionName = "0.0"
                e.printStackTrace()
            }

            return versionName
        }

        val appPackageName: String
            get() {
                var packageName: String
                try {
                    packageName = AppController.getContext().packageName
                } catch (e: Exception) {
                    packageName = ""
                    e.printStackTrace()
                }

                return packageName
            }

        fun DebuggableLogE(tag: String, message: String) {
            try {
                if (CommonKeys.isLoggable!!) {
                    Log.e(tag, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun DebuggableLogE(tag: String, message: String, tr: Throwable) {
            try {
                if (CommonKeys.isLoggable!!) {
                    Log.e(tag, message, tr)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun DebuggableLogI(tag: String, message: String?) {
            try {
                if (CommonKeys.isLoggable!!) {
                    Log.i(tag, message!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun DebuggableLogD(tag: String, message: String) {
            try {
                if (CommonKeys.isLoggable!!) {
                    Log.d(tag, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun DebuggableLogV(tag: String, message: String) {
            try {
                if (CommonKeys.isLoggable!!) {
                    Log.v(tag, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun DebuggableToast(mContext: Context, message: String) {
            try {
                if (CommonKeys.isLoggable!!) {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun showUserMessage(view: View, message: String) {
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            snackbar.show()
        }

        fun showUserMessage(mContext: Context, message: String) {
            try {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        fun gotoMainActivityFromChatActivity(mActivity: Activity) {
            val mainActivityIntent = Intent(mActivity, MainActivity::class.java)
            mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mActivity.startActivity(mainActivityIntent)

            mActivity.finish()
        }

        fun startFirebaseChatListenerService(mActivity: Activity) {
            //mActivity.startService(Intent(mActivity, FirebaseChatNotificationService::class.java))
        }

        fun stopFirebaseChatListenerService(mContext: Context) {
            //mContext.stopService(Intent(mContext, FirebaseChatNotificationService::class.java))
        }

        fun isMyBackgroundServiceRunning(serviceClass: Class<*>, mContext: Context): Boolean {
            val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun showUserMessage(message: String?) {
            try {
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(AppController.getContext(), message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun showServerInternalErrorMessage(context: Context) {
            showUserMessage(context.resources.getString(R.string.internal_server_error))

        }

        fun copyContentToClipboard(mContext: Context, textToBeCopied: String) {
            val cManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val cData = ClipData.newPlainText("text", textToBeCopied)
            if (!TextUtils.isEmpty(textToBeCopied)) {
                cManager.setPrimaryClip(cData)
                showUserMessage(mContext.resources.getString(R.string.referral_code_copied))
            } else {
                showUserMessage(mContext.resources.getString(R.string.referral_code_not_copied))
            }

        }

        fun openPlayStore(context: Context) {
            val appPackageName = appPackageName // getPackageName() from Context or Activity object
            try {
                val playstoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                playstoreIntent.setPackage("com.android.vending")
                context.startActivity(playstoreIntent)
            } catch (anfe: android.content.ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }

        }

        fun playVibration() {
            try {
                val v = AppController.getContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    //deprecated in API 26
                    v.vibrate(500)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }



        fun showInternetNotAvailableForStoredDataViewer(mContext: Context) {
            /*val inflater: LayoutInflater = mContext.getLayoutInflater()
            val layout: View = inflater.inflate(R.layout.toast_layout,mContext.findViewById(android.R.id.toast_layout_root) as ViewGroup?)
            val toast = Toast(mContext)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            toast.duration = Toast.LENGTH_LONG
            toast.setView(layout)
            toast.show()*/

            val toast: Toast = Toast.makeText(mContext, mContext.resources.getString(R.string.you_are_viewing_old_data), Toast.LENGTH_LONG)
            /* val toastView = toast.view // This'll return the default View of the Toast.
             val toastMessage = toastView?.findViewById(android.R.id.message)
             toastMessage.setTextSize(20f)
             toastMessage.setTextColor(Color.RED)
             //toastMessage.setCompoundDrawablesWithIntrinsicBounds(android.R.mipmap.ic_fly, 0, 0, 0)
             toastMessage.gravity = Gravity.CENTER
             toastMessage.compoundDrawablePadding = 16
             toastView.setBackgroundColor(Color.YELLOW)*/
            toast.show()
        }

        fun showNoInternetAlert(context: Context, iNoInternetCustomAlertCallBack: INoInternetCustomAlertCallback) {
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setIcon(R.drawable.ic_wifi_off)
            builder.setTitle(context.resources.getString(R.string.no_connection))
            builder.setMessage(context.resources.getString(R.string.enable_connection_and_come_back))

            builder.setPositiveButton(context.resources.getString(R.string.ok_c)) { dialogInterface, i ->
                iNoInternetCustomAlertCallBack.onOkayClicked()
                dialogInterface.dismiss()
            }
            builder.setNeutralButton(context.resources.getString(R.string.retry)) { dialogInterface, i ->
                iNoInternetCustomAlertCallBack.onRetryClicked()
                dialogInterface.dismiss()
            }
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.round_shape_corner_20dp))
            dialog.show()
        }
    }


    private fun stopSinchService(context: Context) {

    }



    fun getFireBaseToken(): String {
        var pos = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isComplete) {
                try {
                    pos = it.result.toString()
                    println("Device Id  : new two " + pos)
                    sessionManager.deviceId = pos
                    // DO your thing with your firebase token
                }catch (e:Exception){
                    Log.i(TAG, "getFireBaseToken: Error=${e.localizedMessage}")
                }

            }
        }.addOnFailureListener {
            println("Device Id  : new two exception " + it.stackTrace)
        }

        return pos

    }

    fun getAuthType(): String {
        return if (sessionManager.facebookId!!.isNotEmpty()) {
            "facebook"
        } else if (sessionManager.googleId!!.isNotEmpty()) {
            "google"
        } else if (sessionManager.appleId!!.isNotEmpty()) {
            "apple"
        } else {
            "email"
        }
    }

    fun getAuthId(): String? {
        return if (sessionManager.facebookId!!.isNotEmpty()) {
            sessionManager.facebookId
        } else if (sessionManager.googleId!!.isNotEmpty()) {
            sessionManager.googleId
        } else if (sessionManager.appleId!!.isNotEmpty()) {
            sessionManager.appleId
        } else {
            ""
        }
    }

    fun removeLiveTrackingNodesAfterCompletedTrip(context: Context) {
        try {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.LIVE_TRACKING_NODE).child(sessionManager.tripId!!).removeValue()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        removeChatNodesAfterCompletedTrip(context)
    }

    private fun removeChatNodesAfterCompletedTrip(context: Context) {
        try {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.chatFirebaseDatabaseName).child(sessionManager.tripId!!).removeValue()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeTripNodesAfterCompletedTrip(context: Context) {
        try {
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(sessionManager.tripId!!).removeValue()
            mFirebaseDatabase!!.child(FirebaseDbKeys.TRIP_PAYMENT_NODE).child(FirebaseDbKeys.TRIPLIVEPOLYLINE).removeValue()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * init Stripe
     */
    fun initStripeData(context: Context) {
        PaymentConfiguration.init(context, sessionManager.stripePublishKey!!)
        stripe = Stripe(context, PaymentConfiguration.getInstance(context).publishableKey)
    }

    /**
     * Stripe Instance
     */
    fun stripeInstance(): Stripe? {
        return stripe
    }

    /**
     * Get Client Secret From Response
     */
    fun getClientSecret(jsonResponse: JsonResponse, activity: CommonActivity) {
        val clientSecret = getJsonValue(jsonResponse.strResponse, "two_step_id", String::class.java) as String
        if (stripeInstance() != null) {
            stripeInstance()!!.confirmPayment(activity as ComponentActivity, createPaymentIntentParams(clientSecret, activity))
        } else {
            hideProgressDialog()
            Toast.makeText(activity.applicationContext, activity.applicationContext.resources.getString(R.string.internal_server_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun handleDataMessage(json: JSONObject, context: Context) {
        if (json.getJSONObject("custom").has("id")) {
            val notificationId = json.getJSONObject("custom").getString("id")
            if (sessionManager.notificationID.equals(notificationId))
                return
            else
                sessionManager.notificationID = json.getJSONObject("custom").getString("id")
        }

        try {
            val mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(context.getString(R.string.real_time_db))
            mFirebaseDatabase!!.child(FirebaseDbKeys.Notification).child(sessionManager.userId!!).removeValue()

            if (json.getJSONObject("custom").has("id")) {
                val requestId = json.getJSONObject("custom").getString("id")
                if (sessionManager.requestId.equals(requestId))
                    return
                else
                    sessionManager.requestId = json.getJSONObject("custom").getString("id")
            }
            DebuggableLogE(TAG, "push json: $json")




            sessionManager.pushJson = json.toString()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (!Settings.canDrawOverlays(context))
                    overlayPermissionGranted=false


            if (!NotificationUtils.isAppIsInBackground(context)&&overlayPermissionGranted) {
                DebuggableLogE(TAG, "IF: $json")
                // app is in foreground, broadcast the push message
                val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                pushNotification.putExtra("message", "message")
                LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)

                if (json.getJSONObject("custom").has("accept_request")) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    /* if (!isSendingRequestisLive) {
                         val rating = Intent(context, SendingRequestActivity::class.java)
                         rating.putExtra("loadData", "loadData")
                         val locationHashMap = HashMap<String, String>()
                         locationHashMap["pickup_latitude"] = "0.0"
                         locationHashMap["pickup_longitude"] = "0.0"
                         locationHashMap["drop_latitude"] = "0.0"
                         locationHashMap["drop_longitude"] = "0.0"
                         rating.putExtra("hashMap", locationHashMap)
                         rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                         startActivity(rating)
                     }*/
                    if (!SendingRequestActivity.isSendingRequestisLive) {
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)
                        val trip_id = json.getJSONObject("custom").getJSONObject("accept_request").getString("trip_id")
                        sessionManager.tripId = trip_id
                        sessionManager.tripStatus = "accept_request"
                        sessionManager.isDriverAndRiderAbleToChat = true
                    }


                } else if (json.getJSONObject("custom").has("arrive_now")) {

                    MainActivity.isMainActivity = true
                    /*if (!MainActivity.isMainActivity) {
                        Intent dialogs = new Intent(getApplicationContext(), CommonDialog.class);
                        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        dialogs.putExtra("message", getString(R.string.driverarrrive));
                        dialogs.putExtra("type", 0);
                        startActivity(dialogs);
                    }*/

                    val trip_id = json.getJSONObject("custom").getJSONObject("arrive_now").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "arrive_now"
                    sessionManager.isDriverAndRiderAbleToChat = true

                } else if (json.getJSONObject("custom").has("begin_trip")) {

                    if (!MainActivity.isMainActivity) {


                        val dialogs = Intent(context, CommonDialog::class.java)
                        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        dialogs.putExtra("message", context.getString(R.string.yourtripbegin))
                        dialogs.putExtra("type", 0)
                        context.startActivity(dialogs)


                    }

                    val trip_id = json.getJSONObject("custom").getJSONObject("begin_trip").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "end_trip"
                    sessionManager.isDriverAndRiderAbleToChat = true

                } else if (json.getJSONObject("custom").has("end_trip")) {
                    val trip_id = json.getJSONObject("custom").getJSONObject("end_trip").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "end_trip"
                    sessionManager.clearDriverNameRatingAndProfilePicture()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(context)
                    /*startActivity(new Intent(getApplicationContext(), PaymentAmountPage.class));*/
                    stopSinchService(context)
                    val rating = Intent(context, DriverRatingActivity::class.java)
                    rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    rating.putExtra("imgprofile", json.getJSONObject("custom").getJSONObject("end_trip").getString("driver_thumb_image"))
                    context.startActivity(rating)
                } else if (json.getJSONObject("custom").has("cancel_trip")) {
                    sessionManager.clearTripID()
                    sessionManager.clearDriverNameRatingAndProfilePicture()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(context)
                    sessionManager.isrequest = false
                    sessionManager.isTrip = false
                    if (!MainActivity.isMainActivity) {


                        val dialogs = Intent(context, CommonDialog::class.java)
                        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        dialogs.putExtra("message", context.getString(R.string.yourtripcancelledbydriver))
                        dialogs.putExtra("type", 1)
                        context.startActivity(dialogs)


                    }
                } else if (json.getJSONObject("custom").has("trip_payment")) {
                    removeLiveTrackingNodesAfterCompletedTrip(context)

                    removeTripNodesAfterCompletedTrip(context)
                    sessionManager.clearTripID()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    /*Intent rating = new Intent(getApplicationContext(), MainActivity.class);
                    rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(rating);*/
                    sessionManager.isrequest = false
                    sessionManager.isTrip = false
                    //statusDialog("Your trip cancelled by driver",1);
                    if (!MainActivity.isMainActivity) {

                        val dialogs = Intent(context, CommonDialog::class.java)
                        dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        dialogs.putExtra("message", context.resources.getString(R.string.paymentcompleted))
                        //dialogs.putExtra("driverImage",json.getJSONObject("custom").getJSONObject("trip_payment").getString("driver_thumb_image"));
                        dialogs.putExtra("type", 1)
                        context.startActivity(dialogs)


                    }
                } else if (json.getJSONObject("custom").has("chat_notification")) {
                    val tripId = json.getJSONObject("custom").getJSONObject("chat_notification").getString("trip_id")
                    if (!Constants.chatPageVisible || !sessionManager.tripId!!.equals(tripId)) {
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

                        val notificationIntent = Intent(context, ActivityChat::class.java)
                        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP


                        val notificationUtils = NotificationUtils(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            notificationUtils.playNotificationSound()
                        }
                        val message = json.getJSONObject("custom").getJSONObject("chat_notification").getString("message_data")
                        val title = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_name")
                        println("ChatNotification : Rider " + message)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, notificationIntent, null)
                    }

                } else if (json.getJSONObject("custom").has("user_calling")) {
                    initSinchService(context)
                } else {
                    if (json.getJSONObject("custom").has("custom_message")) {
                        // statusDialog("Your Trip Begins Now...");
                        val notificationUtils = NotificationUtils(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            notificationUtils.playNotificationSound()
                        }
                        val message = json.getJSONObject("custom").getJSONObject("custom_message").getString("message_data")
                        val title = json.getJSONObject("custom").getJSONObject("custom_message").getString("title")

                        notificationUtils.generateNotification(context, message, title)
                    }
                }
                // play notification sound
                // notificationUtils.playNotificationSound();
                // notificationUtils.generateNotification(getApplicationContext(),message);
            } else {
                DebuggableLogE(TAG, "ELSE: $json")

                val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                pushNotification.putExtra("message", "message")
                if ("" != sessionManager.accessToken) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                }

                if (json.getJSONObject("custom").has("accept_request")) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    /*  if (!isSendingRequestisLive) {
                          val rating = Intent(context, SendingRequestActivity::class.java)
                         *//* rating.putExtra("loadData", "loadData")
                        val locationHashMap = HashMap<String, String>()
                        locationHashMap["pickup_latitude"] = "0.0"
                        locationHashMap["pickup_longitude"] = "0.0"
                        locationHashMap["drop_latitude"] = "0.0"
                        locationHashMap["drop_longitude"] = "0.0"
                        rating.putExtra("hashMap", locationHashMap)*//*
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(rating)
                    }*/
                    val trip_id = json.getJSONObject("custom").getJSONObject("accept_request").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "accept_request"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && sessionManager.appInBackGround) {
                        CommonKeys.isRequestAccepted = true
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.accepted_request)
                        //val message = json.getJSONObject("custom").getString("title")
                        val title = context.resources.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, rating, null)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)

                    }


                }

                /*else if(json.getJSONObject("custom").has("no_cars")) {
                    sessionManager.setIsrequest(false);
                    sessionManager.setIsTrip(true);
                    Intent rating=new Intent(getApplicationContext(), SendingRequestActivity.class);
                    rating.putExtra("loadData","loadData");
                    rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(rating);

                   *//* Intent pushNotifications = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotifications.putExtra("message", "message");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotifications);*//*
                }*/
                else if (json.getJSONObject("custom").has("arrive_now")) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    val trip_id = json.getJSONObject("custom").getJSONObject("arrive_now").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "arrive_now"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        CommonKeys.isArrowNow = true
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.driverarrrive)
                        //val message = json.getJSONObject("custom").getString("title")
                        val title = context.resources.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, null)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)
                        if (!MainActivity.isMainActivity) {
                            val dialogs = Intent(context, CommonDialog::class.java)
                            dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            dialogs.putExtra("message", context.resources.getString(R.string.driverarrrive))
                            dialogs.putExtra("type", 0)
                            context.startActivity(dialogs)
                        }
                    }
                } else if (json.getJSONObject("custom").has("begin_trip")) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    val trip_id = json.getJSONObject("custom").getJSONObject("begin_trip").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "end_trip"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        CommonKeys.isBeginTrip = true
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        //{custom={"begin_trip":{"trip_id":"2082"},"end_time":1619605567,"id":1619605552,"title":"Viagem começou por motorista"}}
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.yourtripbegin)
                        //val message = json.getJSONObject("custom").getString("title")
                        val title = context.resources.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, null)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)
                        if (!MainActivity.isMainActivity) {

                            val dialogs = Intent(context, CommonDialog::class.java)
                            dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            dialogs.putExtra("message", context.resources.getString(R.string.yourtripbegin))
                            dialogs.putExtra("type", 0)
                            context.startActivity(dialogs)


                        }
                    }


                } else if (json.getJSONObject("custom").has("end_trip")) {
                    sessionManager.isrequest = false
                    sessionManager.isTrip = true
                    val driver_image = json.getJSONObject("custom").getJSONObject("end_trip").getString("driver_thumb_image")
                    sessionManager.driverProfilePic = driver_image
                    sessionManager.isDriverAndRiderAbleToChat = false
                    CommonMethods.stopFirebaseChatListenerService(context)
                    val trip_id = json.getJSONObject("custom").getJSONObject("end_trip").getString("trip_id")
                    sessionManager.tripId = trip_id
                    sessionManager.tripStatus = "end_trip"
                    stopSinchService(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        CommonKeys.isEndTrip = true
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val rating = Intent(context, DriverRatingActivity::class.java)
                        rating.putExtra("imgprofile", json.getJSONObject("custom").getJSONObject("end_trip").getString("driver_thumb_image"))
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.rate_your_ride)
                        //val message = json.getJSONObject("custom").getString("title")
                        val title = context.resources.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, rating, null)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val rating = Intent(context, DriverRatingActivity::class.java)
                        rating.putExtra("imgprofile", json.getJSONObject("custom").getJSONObject("end_trip").getString("driver_thumb_image"))
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)
                    }

                } else if (json.getJSONObject("custom").has("cancel_trip")) {
                    sessionManager.clearTripID()
                    sessionManager.isDriverAndRiderAbleToChat = false
                    sessionManager.isrequest = false
                    sessionManager.isTrip = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        CommonKeys.isPaymentOrCancel = true
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.yourtripcancelledbydriver)
                        //val message = json.getJSONObject("custom").getString("title")
                        val title = context.resources.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, rating, null)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)
                        if (!MainActivity.isMainActivity) {

                            val dialogs = Intent(context, CommonDialog::class.java)
                            dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            dialogs.putExtra("message", context.resources.getString(R.string.yourtripcancelledbydriver))
                            dialogs.putExtra("type", 1)
                            context.startActivity(dialogs)
                        }
                    }

                } else if (json.getJSONObject("custom").has("trip_payment")) {
                    removeLiveTrackingNodesAfterCompletedTrip(context)
                    removeTripNodesAfterCompletedTrip(context)
                    sessionManager.clearTripID()
                    sessionManager.isDriverAndRiderAbleToChat = false

                    sessionManager.isrequest = false
                    sessionManager.isTrip = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && NotificationUtils.isAppIsInBackground(context)) {
                        CommonKeys.isPaymentOrCancel = true
                        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        //notificationIntent.putExtra(CommonKeys.FIREBASE_CHAT_FROM_PUSH, json.toString())
                        val notificationUtils = NotificationUtils(context)
                        notificationUtils.playNotificationSound()
                        val message = context.resources.getString(R.string.paymentcompleted)
                        //val message = json.getJSONObject("custom").getString("title")
                        val title = context.resources.getString(R.string.app_name)
                        notificationUtils.showNotificationMessage(title, message, timeStamp, rating, null)
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("message", "message")
                        LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification)
                    } else {
                        val rating = Intent(context, MainActivity::class.java)
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(rating)

                        if (!MainActivity.isMainActivity) {

                            val dialogs = Intent(context, CommonDialog::class.java)
                            dialogs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            dialogs.putExtra("message", context.resources.getString(R.string.paymentcompleted))
                            //dialogs.putExtra("driverImage",json.getJSONObject("custom").getJSONObject("trip_payment").getString("driver_thumb_image"));
                            dialogs.putExtra("type", 1)
                            context.startActivity(dialogs)


                        }
                    }

                } else if (json.getJSONObject("custom").has("user_calling")) {
                    CommonKeys.keyCaller = json.getJSONObject("custom").getJSONObject("user_calling").getString("title")

                    initSinchService(context)
                } else if (json.getJSONObject("custom").has("chat_notification")) {


                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    //String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new Date());

                    val notificationIntent = Intent(context, ActivityChat::class.java)
                    notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP


                    val notificationUtils = NotificationUtils(context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        notificationUtils.playNotificationSound()
                    }
                    val message = json.getJSONObject("custom").getJSONObject("chat_notification").getString("message_data")
                    val title = json.getJSONObject("custom").getJSONObject("chat_notification").getString("user_name")
                    println("ChatNotification : Rider " + message)
                    notificationUtils.showNotificationMessage(title, message, timeStamp, notificationIntent, null)

                } else {
                    if (json.getJSONObject("custom").has("custom_message")) {
                        sessionManager.isrequest = false
                        sessionManager.isTrip = false
                        /*Intent rating = new Intent(getApplicationContext(), MainActivity.class);
                        rating.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(rating);*/
                        val notificationUtils = NotificationUtils(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            notificationUtils.playNotificationSound()

                        val message = json.getJSONObject("custom").getJSONObject("custom_message").getString("message_data")
                        val title = json.getJSONObject("custom").getJSONObject("custom_message").getString("title")
                        notificationUtils.generateNotification(context, message, title)
                    }
                }
                // play notification sound
                // notificationUtils.playNotificationSound();
                // notificationUtils.generateNotification(getApplicationContext(),message);

            }
        } catch (e: JSONException) {
            DebuggableLogE(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            DebuggableLogE(TAG, "Exception: " + e.message)
        }

    }


    private fun initSinchService(context: Context) {
       /* if (sessionManager.accessToken != "") {
            context.startService(Intent(context, CabmeSinchService::class.java))
        }*/

    }

    /**
     * Create a Payment to Start Payment Process
     */
    private fun createPaymentIntentParams(clientSecret: String, context: Context): ConfirmPaymentIntentParams {
        return ConfirmPaymentIntentParams.create(clientSecret)
    }

    fun getRiderProfile(isFilterUpdate: Boolean, isUpdateFilter: LinkedHashSet<Int>?): HashMap<String, String> {
        val riderProfile = HashMap<String, String>()
        riderProfile["token"] = sessionManager.accessToken!!
        if (isFilterUpdate) {
            riderProfile["options"] = TextUtils.join(",", isUpdateFilter!!)
        }

        return riderProfile
    }


    interface INoInternetCustomAlertCallback {
        fun onOkayClicked()
        fun onRetryClicked()
    }

    fun updateLocale(languageCode: String, newBase: Context?): Context? {
        var newBase = newBase
        val lang: String = languageCode!! // your language or load from SharedPref
        val locale = Locale(lang)
        val config = Configuration(newBase?.resources?.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        newBase = newBase?.createConfigurationContext(config)
        newBase?.resources?.updateConfiguration(config, newBase.resources.displayMetrics)
        return newBase
    }

    fun vectorToBitmap(@DrawableRes id: Int, context: Context): BitmapDescriptor? {
        val vectorDrawable: Drawable = ResourcesCompat.getDrawable(context.resources, id, null)!!
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun setHeaderText(string: String, view: View) {
        (view as TextView).text = string
    }

    fun cameraIntent(imageFile: File, activity: AppCompatActivity) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageUri = FileProvider.getUriForFile(activity,  BuildConfig.APPLICATION_ID + ".provider", imageFile)
        try {
            val resolvedIntentActivities = activity.packageManager.queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolvedIntentInfo in resolvedIntentActivities) {
                val packageName = resolvedIntentInfo.activityInfo.packageName
                activity.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            cameraIntent.putExtra("return-data", true)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        activity.startActivityForResult(cameraIntent, Constants.PICK_IMAGE_REQUEST_CODE)
        refreshGallery(activity, imageFile)
    }

    fun galleryIntent(activity: AppCompatActivity){
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(i, Constants.SELECT_FILE)
    }

    /**
     * Input output Stream
     */
    @Throws(IOException::class)
    fun copyStream(input: InputStream?, output: FileOutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input!!.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    fun clearImageCacheWhenAppOpens(context: Context){
        val dir = File (context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                File(dir, children[i]).delete()
            }
        }
    }

    fun getAddressFromLatLng(context: Context,latitude: Double, longitude: Double): String {
        var fullAddress = "Unknown"
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                fullAddress = addresses[0].getAddressLine(0)
                Log.i(TAG, "getAddressFromLatLng: fullAddress=$fullAddress")
                fullAddress
            } else {
                Log.i(TAG, "getAddressFromLatLng: fullAddress else =$fullAddress")
                fullAddress
            }
        } catch (e: IOException) {
            Log.i(TAG, "getAddressFromLatLng: Error=${e.localizedMessage}")
            e.printStackTrace()
            fullAddress
        }
    }

    // Add this extension in a file like ViewExtensions.kt
    // Add this below line of code in every activity and fragment
    // binding.root.applySystemBarsPadding() // Adjusts padding automatically
    fun View.applySystemBarsPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

}