package com.rideincab.user.taxi.adapters

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.taxi.datamodels.trip.TripListModelArrayList
import com.rideincab.user.taxi.interfaces.PaginationAdapterCallback
import java.util.*
import javax.inject.Inject

class PastTripsPaginationAdapter(private val context: Context, private val mCallback: PaginationAdapterCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @Inject
    lateinit var sessionManager: SessionManager

    private var CompleteTrips: ArrayList<TripListModelArrayList>? = null
        set
    private var isLoadingAdded = false
    private var retryPageLoad = false
    lateinit var onItemRatingClickListener: onItemRatingClickListner


    private var errorMsg: String? = null

    val isEmpty: Boolean
        get() = itemCount == 0

    fun setOnItemRatingClickListner(onItemRatingClickListner: onItemRatingClickListner) {
        onItemRatingClickListener = onItemRatingClickListner
    }

    interface onItemRatingClickListner {
        fun setRatingClick(position: Int, TripDetailsModel: TripListModelArrayList)
    }

    init {
        CompleteTrips = ArrayList()
        AppController.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem = inflater.inflate(R.layout.app_card_layout, parent, false)
                viewHolder = PastTripsViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading = inflater.inflate(R.layout.app_item_progress, parent, false)
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val TripDetailsModel = CompleteTrips!![position] // Past Detail

        when (getItemViewType(position)) {
            ITEM -> {
                val pastTripsViewHolder = holder as PastTripsViewHolder

                pastTripsViewHolder.tvCountry.text = context.resources.getString(R.string.trip_id) + TripDetailsModel.tripId.toString()
                pastTripsViewHolder.carName.text = TripDetailsModel.carType
                if (TripDetailsModel.status == "Rating") {
                    pastTripsViewHolder.status.text = context.getString(R.string.Rating)
                } else if (TripDetailsModel.status == "Cancelled") {
                    pastTripsViewHolder.status.text = context.getString(R.string.Cancelled)
                    pastTripsViewHolder.status.setTextColor(Color.RED)
                } else if (TripDetailsModel.status == "Completed") {
                    pastTripsViewHolder.status.text = context.getString(R.string.completed)
                    pastTripsViewHolder.status.setTextColor(Color.GREEN)
                } else if (TripDetailsModel.status == "Payment") {
                    pastTripsViewHolder.status.text = context.getString(R.string.payment)
                } else if (TripDetailsModel.status == "Begin trip") {
                    pastTripsViewHolder.status.text = context.getString(R.string.begin_trip)
                } else if (TripDetailsModel.status == "End trip") {
                    pastTripsViewHolder.status.text = context.getString(R.string.end_trip)
                } else if (TripDetailsModel.status == "Scheduled") {
                    pastTripsViewHolder.status.text = context.getString(R.string.scheduled)
                    pastTripsViewHolder.status.setTextColor(Color.BLUE)
                } else {
                    pastTripsViewHolder.status.text = TripDetailsModel.carType
                }

                pastTripsViewHolder.amountCard.text = sessionManager.currencySymbol + TripDetailsModel.totalFare
                if (!TextUtils.isEmpty(TripDetailsModel.mapImage)) {
                    pastTripsViewHolder.imageLayout.visibility = View.VISIBLE
                    Picasso.get().load(TripDetailsModel.mapImage).placeholder(R.drawable.trip_map_placeholder)
                        .into(pastTripsViewHolder.imageView)
                    pastTripsViewHolder.rltData.visibility = View.GONE
                } else {
                    pastTripsViewHolder.rltData.visibility = View.VISIBLE
                    pastTripsViewHolder.imageLayout.visibility = View.GONE
                    pastTripsViewHolder.tv_pickLocation.text = TripDetailsModel.pickup
                    pastTripsViewHolder.tv_dropLocation.text = TripDetailsModel.drop
                }

                /*if (!TextUtils.isEmpty(TripDetailsModel.pickup)&&!TextUtils.isEmpty(TripDetailsModel.drop)){
                    pastTripsViewHolder.rltData.visibility = View.VISIBLE
                    pastTripsViewHolder.imageLayout.visibility = View.GONE
                    pastTripsViewHolder.tv_pickLocation.text = TripDetailsModel.pickup
                    pastTripsViewHolder.tv_dropLocation.text = TripDetailsModel.drop
                }else{
                    pastTripsViewHolder.imageLayout.visibility = View.VISIBLE
                    Picasso.get().load(TripDetailsModel.mapImage).placeholder(R.drawable.trip_map_placeholder)
                        .into(pastTripsViewHolder.imageView)
                    pastTripsViewHolder.rltData.visibility = View.GONE
                }*/

                if (TripDetailsModel.isPool!! && TripDetailsModel.seats != 0) {
                    pastTripsViewHolder.seatcount.visibility = View.VISIBLE
                    pastTripsViewHolder.seatcount.setText(context.getString(R.string.seat_count) + " " + TripDetailsModel.seats.toString())
                } else {
                    pastTripsViewHolder.seatcount.visibility = View.GONE
                }
                pastTripsViewHolder.rltData.setOnClickListener { onItemRatingClickListener.setRatingClick(position, TripDetailsModel) }
                pastTripsViewHolder.imageView.setOnClickListener {
                    onItemRatingClickListener.setRatingClick(position, TripDetailsModel)
                }
            }

            LOADING -> {
                val loadingVH = holder as LoadingViewHolder

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE

                    loadingVH.mErrorTxt.text = if (errorMsg != null)
                        errorMsg
                    else
                        context.getString(R.string.error_msg_unknown)

                } else {
                    loadingVH.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (CompleteTrips == null) 0 else CompleteTrips!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == CompleteTrips!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }


    /*
        Helpers - Pagination
    */

    fun add(TripDetailsModel: TripListModelArrayList) {
        CompleteTrips!!.add(TripDetailsModel)
        notifyItemInserted(CompleteTrips!!.size - 1)
    }

    fun removeAll() {
        CompleteTrips!!.clear()
        notifyDataSetChanged()
    }

    fun addAll(TripDetailsModels: ArrayList<TripListModelArrayList>) {
        for (TripDetailsModel in TripDetailsModels) {
            add(TripDetailsModel)
        }
    }

    fun remove(TripDetailsModel: TripListModelArrayList?) {
        val position = CompleteTrips!!.indexOf(TripDetailsModel)
        if (position > -1) {
            CompleteTrips!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    fun clearAll() {
        isLoadingAdded = false
        CompleteTrips!!.clear()
        notifyDataSetChanged()
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(TripListModelArrayList())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = CompleteTrips!!.size - 1
        val TripDetailsModel = getItem(position)

        if (TripDetailsModel != null) {
            CompleteTrips!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun getItem(position: Int): TripListModelArrayList? {
        return CompleteTrips!![position]
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(CompleteTrips!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }

    protected inner class PastTripsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvCountry: TextView
        val carName: TextView
        val status: TextView
        val amountCard: TextView
        val imageView: ImageView
        val btnRate: Button
        internal val tv_dropLocation: TextView
        internal val tv_pickLocation: TextView
        internal val mapview: RelativeLayout
        internal val imageLayout: RelativeLayout
        internal val rltData: RelativeLayout
        val seatcount: TextView

        init {
            tvCountry = itemView.findViewById<View>(R.id.datetime) as TextView
            carName = itemView.findViewById<View>(R.id.carname) as TextView
            status = itemView.findViewById<View>(R.id.status) as TextView
            amountCard = itemView.findViewById<View>(R.id.amountcard) as TextView
            imageView = itemView.findViewById<View>(R.id.imageView) as ImageView
            btnRate = itemView.findViewById<View>(R.id.btnrate) as Button
            rltData = itemView.findViewById<View>(R.id.rltdata) as RelativeLayout
            tv_pickLocation = itemView.findViewById(R.id.tv_pick_location) as TextView
            mapview = itemView.findViewById(R.id.static_map) as RelativeLayout
            tv_dropLocation = itemView.findViewById(R.id.tv_drop_location) as TextView
            seatcount = itemView.findViewById(R.id.seatcount) as TextView
            imageLayout = itemView.findViewById(R.id.image_layout) as RelativeLayout
        }
    }


    protected inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val mProgressBar: ProgressBar
        val mRetryBtn: ImageButton
        val mErrorTxt: TextView
        val mErrorLayout: LinearLayout


        init {

            mProgressBar = itemView.findViewById(R.id.loadmore_progress)
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry)
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt)
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout)

            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }


        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {
                    showRetry(false, null)
                    mCallback.retryPageLoad()
                }
            }
        }
    }

    companion object {
        // View Types
        private val ITEM = 0
        private val LOADING = 1
    }
}