package com.rideincab.user.common.backgroundtask
/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage backgroundtask
 * @category ImageCompressAsyncTask
 * @author SMR IT Solutions
 *
 */

import android.graphics.Bitmap
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.interfaces.ImageListener
import com.rideincab.user.common.network.AppController
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.jvm.Throws

/*****************************************************************
 * Compress image while upload in background
 */
class ImageCompressAsyncTask(activity: AppCompatActivity, filePath: String?, private val imageListener: ImageListener?) : AsyncTask<Void, Void, Void>() {
    @Inject
    lateinit var sessionManager: SessionManager

    private var filePath = ""
    private var compressPath = ""
    private val compressImgWeakRef: WeakReference<AppCompatActivity>?
    private var requestBody: RequestBody? = null

    init {
        AppController.appComponent.inject(this)
        this.compressImgWeakRef = WeakReference(activity)
        if (filePath != null) {
            this.filePath = filePath
        }
    }

    /**
     * Call when before call the WS.
     */
    override fun onPreExecute() {
        if (this.compressImgWeakRef == null) {
            this.cancel(true)
        }
    }

    /**
     * action to be performed in background
     */
    override fun doInBackground(vararg params: Void): Void? {
        try {
            var file = File(filePath)
            if (file.exists()) {
                publishProgress()

                // Compress the file synchronously
                runBlocking {
                    file = compressFile(file)
                }

                compressPath = file.path
                requestBody = uploadImgParam(compressPath)
            }
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    // Suspend function to compress file
    private suspend fun compressFile(file: File): File = withContext(Dispatchers.IO) {
        Compressor.compress(compressImgWeakRef!!.get()!!, file) {
            resolution(1080, 1920)
            quality(75)
            format(Bitmap.CompressFormat.JPEG)
        }
    }

    /**
     * called after the WS return the response.
     */
    override fun onPostExecute(value: Void?) {
        if (compressImgWeakRef?.get() != null && requestBody != null) {
            imageListener?.onImageCompress(compressPath, requestBody!!)
        } else {
            imageListener?.onImageCompress(compressPath, null)
        }
    }

    @Throws(IOException::class)
    fun uploadImgParam(imagePath: String): RequestBody {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        var file: File?
        try {
            file = File(imagePath)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            multipartBody.addFormDataPart("image", "IMG_$timeStamp.jpg", RequestBody.create("image/png".toMediaTypeOrNull(), file))
            multipartBody.addFormDataPart("token", sessionManager.accessToken.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return multipartBody.build()
    }
}
