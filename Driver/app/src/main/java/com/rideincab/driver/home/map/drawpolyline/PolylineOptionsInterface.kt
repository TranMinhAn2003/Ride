package com.rideincab.driver.home.map.drawpolyline

/**
 * @package com.cloneappsolutions.cabmedriver
 * @subpackage map.drawpolyline
 * @category PolylineOptionsInterface
 * @author SMR IT Solutions
 *
 */

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.rideincab.driver.home.datamodel.StepsClass
import java.util.*

interface PolylineOptionsInterface {
    fun getPolylineOptions(output: PolylineOptions, points: ArrayList<LatLng>, distance: String, overviewPolyline: String, stepPoints: ArrayList<StepsClass>, totalDuration : Int)
}
