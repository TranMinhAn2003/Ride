package com.rideincab.user.common.drawpolyline

/**
 * @package com.cloneappsolutions.cabmeuserdriver
 * @subpackage map.drawpolyline
 * @category DownloadTask
 * @author SMR IT Solutions
 * 
 */

import android.content.Context
import android.os.AsyncTask


import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonMethods

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.URL

import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection
import kotlin.jvm.Throws

class DownloadTask(polylineOptionsInterface: PolylineOptionsInterface, internal var mContext: Context) : AsyncTask<String, Void, String>() {

    internal var polylineOptionsInterface: PolylineOptionsInterface
    @Inject
    lateinit var commonMethods: CommonMethods

    init {
        this.polylineOptionsInterface = polylineOptionsInterface
        AppController.appComponent.inject(this)
    }

    override fun doInBackground(vararg url: String): String {

        var data = ""

        try {
            if (commonMethods.isOnline(mContext)) {
                data = downloadUrl(url[0])
            } else {
                //commonMethods.showMessage(mContext, dialog, mContext.getResources().getString(R.string.no_connection));
            }
        } catch (e: Exception) {
            CommonMethods.DebuggableLogD("Background Task", e.toString())
        }

        return data
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        val parserTask = ParserTask(this.polylineOptionsInterface, mContext)

        if (commonMethods.isOnline(mContext)) {
            parserTask.execute(result)
        } else {
            //commonMethods.showMessage(mContext, dialog, mContext.getResources().getString(R.string.no_connection));
        }

    }

    /**
     * A method to download json data from url
     */
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpsURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpsURLConnection

            urlConnection.connect()

            iStream = urlConnection.inputStream

          /*  val br = BufferedReader(InputStreamReader(iStream))

            val sb = StringBuffer()

            var line: String
            line = br.readLine()
            while (true) {
                sb.append(line)
                line = br.readLine()
            }
*/

            val sb = iStream.bufferedReader().use(BufferedReader::readText)
            data = sb.toString()

           // br.close()

        } catch (e: Exception) {
            CommonMethods.DebuggableLogD("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }
}