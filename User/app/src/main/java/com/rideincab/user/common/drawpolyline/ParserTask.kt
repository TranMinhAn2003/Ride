package com.rideincab.user.common.drawpolyline

/**
 * @package com.cloneappsolutions.cabmeuserdriver
 * @subpackage map.drawpolyline
 * @category ParserTask
 * @author SMR IT Solutions
 *
 */

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.rideincab.user.R
import com.rideincab.user.taxi.datamodels.StepsClass
import org.json.JSONObject
import java.util.*

/* ************************************************************
                      ParserTask
Its used get google places in json format
*************************************************************** */

/**
 * A class to parse the Google Places in JSON format
 */
class ParserTask(polylineOptionsInterface: PolylineOptionsInterface, internal var context: Context) : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

    internal var routes: List<List<HashMap<String, String>>>? = null
    internal var distances = ""
    internal var arrivalTime = ""
    internal var overview_polyline = ""
    var stepPoints = ArrayList<StepsClass>()
    var totalDuration: Int = 0

    var polylineOptionsInterface: PolylineOptionsInterface? = null

    init {
        this.polylineOptionsInterface = polylineOptionsInterface
    }


    // Parsing the data in non-ui thread
    override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {
        //hf=new HomeFragment();
        if (isOnline(context)) {
            val jObject: JSONObject

            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
                distances = parser.parseDistance(jObject)
                arrivalTime = parser.parseTime(jObject)
                overview_polyline = parser.parseOverviewPolyline(jObject)
                stepPoints = parser.parseStepPoints(jObject)
                totalDuration = parser.parseDuration(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return routes
    }

    override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
        val points: ArrayList<LatLng> = ArrayList()
        var lineOptions = PolylineOptions()
        if (result != null) {


            for (i in result.indices) {
                // points = ArrayList()
                lineOptions = PolylineOptions()

                val path = result[i]

                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"]!!)
                    val lng = java.lang.Double.parseDouble(point["lng"]!!)
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(ContextCompat.getColor(context, R.color.app_primary_color))
                lineOptions.geodesic(true)

            }

            // Drawing polyline in the Google Map for the i-th route
            //  mMap.addPolyline(lineOptions);
            polylineOptionsInterface!!.getPolylineOptions(lineOptions, points, distances, overview_polyline, arrivalTime, stepPoints, totalDuration)
        }

    }


    fun isOnline(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}
