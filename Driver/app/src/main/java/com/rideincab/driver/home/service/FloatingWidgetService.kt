package com.rideincab.driver.home.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ImageView
import com.rideincab.driver.R
import com.rideincab.driver.common.configs.SessionManager
import com.rideincab.driver.common.network.AppController
import com.rideincab.driver.home.MainActivity
import javax.inject.Inject

class FloatingWidgetService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mFloatingWidgetView: View? = null
    private var collapsedView: View? = null
    private var expandedView: View? = null
    private var remove_image_view: ImageView? = null
    private val szWindow = Point()
    private var removeFloatingWidgetView: View? = null

    @JvmField
    @Inject
    var sessionManager: SessionManager? = null
    private val mContext: Context? = null
    private val TAG = "FloatingWidgetService"

    // flag for GPS status
    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false

    // flag for GPS status
    var canGetLocation = false
    var location: Location? = null // location
    var latitude = 0.0 // latitude
    var longitude = 0.0 // longitude

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null
    private var x_init_cord = 0
    private var y_init_cord = 0
    private var x_init_margin = 0
    private var y_init_margin = 0

    //Variable to check if the Floating widget view is on left side or in right side
    // initially we are displaying Floating widget view to Left side so set it to true
    private var isLeft = true
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        try {
            //init WindowManager
            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManagerDefaultDisplay

            //Init LayoutInflater
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addRemoveView(inflater)
            addFloatingWidgetView(inflater)
            //        implementClickListeners();
            implementTouchListenerToFloatingWidgetView()
            AppController.getAppComponent().inject(this)
        } catch (e: Exception) {
            Log.i(TAG, "onCreate: Error=" + e.localizedMessage)
        }
    }

    /*  Add Remove View to Window Manager  */
    private fun addRemoveView(inflater: LayoutInflater) {
        try {
            //Inflate the removing view layout we created
            removeFloatingWidgetView =
                inflater.inflate(R.layout.remove_floating_widget_layout, null)

            //Add the view to the window.
            val paramRemove: WindowManager.LayoutParams
            paramRemove = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }

            //Specify the view position
            paramRemove.gravity = Gravity.TOP or Gravity.LEFT

            //Initially the Removing widget view is not visible, so set visibility to GONE
            removeFloatingWidgetView!!.setVisibility(View.GONE)
            remove_image_view =
                removeFloatingWidgetView!!.findViewById<View>(R.id.remove_img) as ImageView

            //Add the view to the window
            mWindowManager!!.addView(removeFloatingWidgetView, paramRemove)
        } catch (e: Exception) {
            Log.i(TAG, "addRemoveView: Error=" + e.localizedMessage)
        }
    }

    /*  Add Floating Widget View to Window Manager  */
    private fun addFloatingWidgetView(inflater: LayoutInflater) {
        try {
            //Inflate the floating view layout we created
            mFloatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null)

            //Add the view to the window.
            val params: WindowManager.LayoutParams
            params = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }


            //Specify the view position
            params.gravity = Gravity.TOP or Gravity.LEFT

            //Initially view will be added to top-left corner, you change x-y coordinates according to your need
            params.x = 0
            params.y = 100

            //Add the view to the window
            mWindowManager!!.addView(mFloatingWidgetView, params)

            //find id of collapsed view layout
            collapsedView = mFloatingWidgetView!!.findViewById(R.id.collapse_view)

            //find id of the expanded view layout
            expandedView = mFloatingWidgetView!!.findViewById(R.id.expanded_container)
        } catch (e: Exception) {
            Log.i(TAG, "addFlotingWidgetView: Error=" + e.localizedMessage)
        }
    }

    private val windowManagerDefaultDisplay: Unit
        private get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) mWindowManager!!.defaultDisplay.getSize(
                szWindow
            ) else {
                val w = mWindowManager!!.defaultDisplay.width
                val h = mWindowManager!!.defaultDisplay.height
                szWindow[w] = h
            }
        }

    /*  Implement Touch Listener to Floating Widget Root View  */
    private fun implementTouchListenerToFloatingWidgetView() {
        try {
            //Drag and move floating view using user's touch action.
            mFloatingWidgetView!!.findViewById<View>(R.id.root_container)
                .setOnTouchListener(object : OnTouchListener {
                    var time_start: Long = 0
                    var time_end: Long = 0
                    var isLongClick = false //variable to judge if user click long press
                    var inBounded =
                        false //variable to judge if floating view is bounded to remove view
                    var remove_img_width = 0
                    var remove_img_height = 0
                    var handler_longClick = Handler()
                    var runnable_longClick = Runnable { //On Floating Widget Long Click

                        //Set isLongClick as true
                        isLongClick = true

                        //Set remove widget view visibility to VISIBLE
                        removeFloatingWidgetView!!.visibility = View.VISIBLE

                        /*  try {
                                      Intent intent=new Intent(getBaseContext(),MainActivity.class);
                                      PendingIntent pendingIntent=getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                                      pendingIntent.send(getApplicationContext(),0,intent);
                                  } catch (PendingIntent.CanceledException e) {
                                      e.printStackTrace();
                                  }*/onFloatingWidgetLongClick()
                    }

                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        return try {
                            //Get Floating widget view params
                            val layoutParams =
                                mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

                            //get the touch location coordinates
                            val x_cord = event.rawX.toInt()
                            val y_cord = event.rawY.toInt()
                            val x_cord_Destination: Int
                            var y_cord_Destination: Int
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    time_start = System.currentTimeMillis()
                                    handler_longClick.postDelayed(runnable_longClick, 600)
                                    remove_img_width = remove_image_view!!.layoutParams.width
                                    remove_img_height = remove_image_view!!.layoutParams.height
                                    x_init_cord = x_cord
                                    y_init_cord = y_cord

                                    //remember the initial position.
                                    x_init_margin = layoutParams.x
                                    y_init_margin = layoutParams.y
                                    return true
                                }

                                MotionEvent.ACTION_UP -> {
                                    isLongClick = false
                                    removeFloatingWidgetView!!.visibility = View.GONE
                                    remove_image_view!!.layoutParams.height = remove_img_height
                                    remove_image_view!!.layoutParams.width = remove_img_width
                                    handler_longClick.removeCallbacks(runnable_longClick)

                                    //If user drag and drop the floating widget view into remove view then stop the service
                                    if (inBounded) {
                                        sessionManager!!.bubbleClosedViaUser = true
                                        inBounded = false
                                        stopSelf()
                                    }


                                    //Get the difference between initial coordinate and current coordinate
                                    val x_diff = x_cord - x_init_cord
                                    val y_diff = y_cord - y_init_cord

                                    //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                                    //So that is click event.
                                    if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                                        time_end = System.currentTimeMillis()

                                        //Also check the difference between start time and end time should be less than 300ms
                                        if (time_end - time_start < 300) onFloatingWidgetClick()
                                    }
                                    y_cord_Destination = y_init_margin + y_diff
                                    val barHeight: Int = statusBarHeight
                                    if (y_cord_Destination < 0) {
                                        y_cord_Destination = 0
                                    } else if (y_cord_Destination + (mFloatingWidgetView!!.height + barHeight) > szWindow.y) {
                                        y_cord_Destination =
                                            szWindow.y - (mFloatingWidgetView!!.height + barHeight)
                                    }
                                    layoutParams.y = y_cord_Destination
                                    inBounded = false

                                    //reset position if user drags the floating view
                                    resetPosition(x_cord)
                                    return true
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    val x_diff_move = x_cord - x_init_cord
                                    val y_diff_move = y_cord - y_init_cord
                                    x_cord_Destination = x_init_margin + x_diff_move
                                    y_cord_Destination = y_init_margin + y_diff_move

                                    //If user long click the floating view, update remove view
                                    if (isLongClick) {
                                        val x_bound_left =
                                            szWindow.x / 2 - (remove_img_width * 1.5).toInt()
                                        val x_bound_right =
                                            szWindow.x / 2 + (remove_img_width * 1.5).toInt()
                                        val y_bound_top =
                                            szWindow.y - (remove_img_height * 1.5).toInt()

                                        //If Floating view comes under Remove View update Window Manager
                                        if (x_cord >= x_bound_left && x_cord <= x_bound_right && y_cord >= y_bound_top) {
                                            inBounded = true
                                            val x_cord_remove =
                                                ((szWindow.x - remove_img_height * 1.5) / 2).toInt()
                                            val y_cord_remove: Int =
                                                (szWindow.y - (remove_img_width * 1.5 + statusBarHeight)).toInt()
                                            if (remove_image_view!!.layoutParams.height == remove_img_height) {
                                                remove_image_view!!.layoutParams.height =
                                                    (remove_img_height * 1.5).toInt()
                                                remove_image_view!!.layoutParams.width =
                                                    (remove_img_width * 1.5).toInt()
                                                val param_remove =
                                                    removeFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
                                                param_remove.x = x_cord_remove
                                                param_remove.y = y_cord_remove
                                                mWindowManager!!.updateViewLayout(
                                                    removeFloatingWidgetView,
                                                    param_remove
                                                )
                                            }
                                            layoutParams.x = x_cord_remove + Math.abs(
                                                removeFloatingWidgetView!!.width - mFloatingWidgetView!!.width
                                            ) / 2
                                            layoutParams.y = y_cord_remove + Math.abs(
                                                removeFloatingWidgetView!!.height - mFloatingWidgetView!!.height
                                            ) / 2

                                            //Update the layout with new X & Y coordinate
                                            mWindowManager!!.updateViewLayout(
                                                mFloatingWidgetView,
                                                layoutParams
                                            )
                                        } else {
                                            //If Floating window gets out of the Remove view update Remove view again
                                            inBounded = false
                                            remove_image_view!!.layoutParams.height =
                                                remove_img_height
                                            remove_image_view!!.layoutParams.width =
                                                remove_img_width
                                            //                                onFloatingWidgetClick();
                                        }
                                    }
                                    layoutParams.x = x_cord_Destination
                                    layoutParams.y = y_cord_Destination

                                    //Update the layout with new X & Y coordinate
                                    mWindowManager!!.updateViewLayout(
                                        mFloatingWidgetView,
                                        layoutParams
                                    )
                                    return true
                                }
                            }
                            false
                        } catch (e: Exception) {
                            Log.i(TAG, "onTouch: Errror=" + e.localizedMessage)
                            false
                        }
                    }
                })
        } catch (e: Exception) {
            Log.i(TAG, "implementTouchListenerToFloatingWidgetView: Error=" + e.localizedMessage)
        }
    }

    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */
    private fun onFloatingWidgetLongClick() {
        try {
            if (mWindowManager != null) {
                try {
                    //Get remove Floating view params
                    val removeParams =
                        removeFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams

                    //get x and y coordinates of remove view
                    val x_cord = (szWindow.x - removeFloatingWidgetView!!.width) / 2
                    val y_cord = szWindow.y - (removeFloatingWidgetView!!.height + statusBarHeight)
                    removeParams.x = x_cord
                    removeParams.y = y_cord

                    //Update Remove view params
                    mWindowManager!!.updateViewLayout(removeFloatingWidgetView, removeParams)
                } catch (e: Exception) {
                    Log.i(TAG, "onFloatingWidgetLongClick: Error=" + e.localizedMessage)
                }
            }
        } catch (e: Exception) {
            Log.i(TAG, "onFloatingWidgetLongClick: Error=" + e.localizedMessage)
        }
    }

    /*  Reset position of Floating Widget view on dragging  */
    private fun resetPosition(x_cord_now: Int) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true
            moveToLeft(x_cord_now)
        } else {
            isLeft = false
            moveToRight(x_cord_now)
        }
    }

    /*  Method to move the Floating widget view to Left  */
    private fun moveToLeft(current_x_cord: Int) {
        try {
            val x = szWindow.x - current_x_cord
            if (mWindowManager != null) {
                object : CountDownTimer(500, 5) {
                    //get params of Floating Widget view
                    var mParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
                    override fun onTick(t: Long) {
                        try {
                            val step = (500 - t) / 5
                            mParams.x = 0 - (current_x_cord * current_x_cord * step).toInt()

                            //If you want bounce effect uncomment below line and comment above line
                            // mParams.x = 0 - (int) (double) bounceValue(step, x);

                            //Update window manager for Floating Widget
                            mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
                        } catch (e: Exception) {
                            Log.i(TAG, "MLonTick: Error=" + e.localizedMessage)
                        }
                    }

                    override fun onFinish() {
                        try {
                            mParams.x = 0

                            //Update window manager for Floating Widget
                            mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
                        } catch (e: Exception) {
                            Log.i(TAG, "MLonFinish: Error=" + e.localizedMessage)
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            Log.i(TAG, "moveToLeft: Error=" + e.localizedMessage)
        }
    }

    /*  Method to move the Floating widget view to Right  */
    private fun moveToRight(current_x_cord: Int) {
        try {
            if (mWindowManager != null) {
                object : CountDownTimer(500, 5) {
                    //get params of Floating Widget view
                    var mParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
                    override fun onTick(t: Long) {
                        try {
                            val step = (500 - t) / 5
                            mParams.x =
                                (szWindow.x + current_x_cord * current_x_cord * step - mFloatingWidgetView!!.width).toInt()

                            //If you want bounce effect uncomment below line and comment above line
                            //  mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - mFloatingWidgetView.getWidth();

                            //Update window manager for Floating Widget
                            mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
                        } catch (e: Exception) {
                            Log.i(TAG, " MRonTick: Error=" + e.localizedMessage)
                        }
                    }

                    override fun onFinish() {
                        try {
                            mParams.x = szWindow.x - mFloatingWidgetView!!.width

                            //Update window manager for Floating Widget
                            mWindowManager!!.updateViewLayout(mFloatingWidgetView, mParams)
                        } catch (e: Exception) {
                            Log.i(TAG, "MRonFininsh: Error=" + e.localizedMessage)
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            Log.i(TAG, "moveToRight: Error=" + e.localizedMessage)
        }
    }

    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */
    private fun bounceValue(step: Long, scale: Long): Double {
        return scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step)
    }

    private val isViewCollapsed: Boolean
        /*  Detect if the floating view is collapsed or expanded */private get() = mFloatingWidgetView == null || mFloatingWidgetView!!.findViewById<View>(
            R.id.collapse_view
        ).visibility == View.VISIBLE
    private val statusBarHeight: Int
        /*  return status bar height on basis of device display metrics  */private get() = Math.ceil(
            (25 * applicationContext.resources.displayMetrics.density).toDouble()
        ).toInt()

    /*  Update Floating Widget view coordinates on Configuration change  */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            if (mWindowManager != null) {
                windowManagerDefaultDisplay
                val layoutParams = mFloatingWidgetView!!.layoutParams as WindowManager.LayoutParams
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (layoutParams.y + (mFloatingWidgetView!!.height + statusBarHeight) > szWindow.y) {
                        layoutParams.y =
                            szWindow.y - (mFloatingWidgetView!!.height + statusBarHeight)
                        mWindowManager!!.updateViewLayout(mFloatingWidgetView, layoutParams)
                    }
                    if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                        resetPosition(szWindow.x)
                    }
                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (layoutParams.x > szWindow.x) {
                        resetPosition(szWindow.x)
                    }
                }
            }
        } catch (e: Exception) {
            Log.i(TAG, "onConfigurationChanged: Error=" + e.localizedMessage)
        }
    }

    /*  on Floating widget click show expanded view  */
    private fun onFloatingWidgetClick() {
        try {
            if (isViewCollapsed) {
                //When user clicks on the image view of the collapsed layout,
                //visibility of the collapsed layout will be changed to "View.GONE"
                //and expanded view will become visible.
                collapsedView!!.visibility = View.VISIBLE
                expandedView!!.visibility = View.GONE
                val it =
                    Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)

                //close the service and remove view from the view hierarchy
//                stopSelf();
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            /*  on destroy remove both view from window manager */
            if (mFloatingWidgetView != null) mWindowManager!!.removeView(mFloatingWidgetView)
            if (removeFloatingWidgetView != null) mWindowManager!!.removeView(
                removeFloatingWidgetView
            )
        } catch (e: Exception) {
            Log.i(TAG, "onDestroy: Error=" + e.localizedMessage)
        }
    } /* //Variable to check if the Floating widget view is on left side or in right side
    // initially we are displaying Floating widget view to Left side so set it to true
    private boolean isLeft = true;
    private String TAG="FloatingWidgetService";

    public FloatingWidgetService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        getWindowManagerDefaultDisplay();

        //Init LayoutInflater
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        addRemoveView(inflater);
        addFloatingWidgetView(inflater);
        implementClickListeners();
        implementTouchListenerToFloatingWidgetView();

        try {
           */

    /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startMyOwnForeground();
            else
                startForeground(10, new Notification());*/
    /*
        } catch (Exception e) {
            Log.i("TAG", "onStartCommand: ${e.localizedMessage}"+e.getLocalizedMessage());
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(20, notification);
    }


    */
    /*  Add Remove View to Window Manager  */ /*
    private View addRemoveView(LayoutInflater inflater) {
        //Inflate the removing view layout we created
        removeFloatingWidgetView = inflater.inflate(R.layout.remove_floating_widget_layout, null);

        //Add the view to the window.
        WindowManager.LayoutParams paramRemove;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            paramRemove = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        //Specify the view position
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially the Removing widget view is not visible, so set visibility to GONE
        removeFloatingWidgetView.setVisibility(View.GONE);
        remove_image_view = (ImageView) removeFloatingWidgetView.findViewById(R.id.remove_img);

        //Add the view to the window
        mWindowManager.addView(removeFloatingWidgetView, paramRemove);
        return remove_image_view;
    }

    */
    /*  Add Floating Widget View to Window Manager  */ /*
    private void addFloatingWidgetView(LayoutInflater inflater) {
        //Inflate the floating view layout we created
        mFloatingWidgetView = inflater.inflate(R.layout.floating_widget_layout, null);

        //Add the view to the window.
        WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }


        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager.addView(mFloatingWidgetView, params);

        //find id of collapsed view layout
        collapsedView = mFloatingWidgetView.findViewById(R.id.collapse_view);

        //find id of the expanded view layout
        expandedView = mFloatingWidgetView.findViewById(R.id.expanded_container);
    }

    private void getWindowManagerDefaultDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
            mWindowManager.getDefaultDisplay().getSize(szWindow);
        else {
            int w = mWindowManager.getDefaultDisplay().getWidth();
            int h = mWindowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        //check the network permission
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    */
    /*  Implement Touch Listener to Floating Widget Root View  */ /*
    private void implementTouchListenerToFloatingWidgetView() {
        //Drag and move floating view using user's touch action.
        mFloatingWidgetView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {

            long time_start = 0, time_end = 0;

            boolean isLongClick = false;//variable to judge if user click long press
            boolean inBounded = false;//variable to judge if floating view is bounded to remove view
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {
                @Override
                public void run() {
                    //On Floating Widget Long Click

                    //Set isLongClick as true
                    isLongClick = true;



                    //Set remove widget view visibility to VISIBLE
                    removeFloatingWidgetView.setVisibility(View.VISIBLE);

                  */
    /*  try {
                        Intent intent=new Intent(getBaseContext(),MainActivity.class);
                        PendingIntent pendingIntent=getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                        pendingIntent.send(getApplicationContext(),0,intent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }*/
    /*

                    onFloatingWidgetLongClick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Get Floating widget view params
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

                //get the touch location coordinates
                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();

                int x_cord_Destination, y_cord_Destination;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();

                        handler_longClick.postDelayed(runnable_longClick, 600);

                        remove_img_width = remove_image_view.getLayoutParams().width;
                        remove_img_height = remove_image_view.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        //remember the initial position.
                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        return true;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeFloatingWidgetView.setVisibility(View.GONE);
                        remove_image_view.getLayoutParams().height = remove_img_height;
                        remove_image_view.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        //If user drag and drop the floating widget view into remove view then stop the service
                        if (inBounded) {
                            stopSelf();
                            inBounded = false;
                            break;
                        }


                        //Get the difference between initial coordinate and current coordinate
                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        //The check for x_diff <5 && y_diff< 5 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();

                            //Also check the difference between start time and end time should be less than 300ms
                            if ((time_end - time_start) < 300)
                                onFloatingWidgetClick();

                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int barHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (mFloatingWidgetView.getHeight() + barHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (mFloatingWidgetView.getHeight() + barHeight);
                        }

                        layoutParams.y = y_cord_Destination;

                        inBounded = false;

                        //reset position if user drags the floating view
                        resetPosition(x_cord);

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        //If user long click the floating view, update remove view
                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                            //If Floating view comes under Remove View update Window Manager
                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight()));

                                if (remove_image_view.getLayoutParams().height == remove_img_height) {
                                    remove_image_view.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                    remove_image_view.getLayoutParams().width = (int) (remove_img_width * 1.5);

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    mWindowManager.updateViewLayout(removeFloatingWidgetView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(removeFloatingWidgetView.getWidth() - mFloatingWidgetView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(removeFloatingWidgetView.getHeight() - mFloatingWidgetView.getHeight())) / 2;

                                //Update the layout with new X & Y coordinate
                                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                                break;
                            } else {
                                //If Floating window gets out of the Remove view update Remove view again
                                inBounded = false;
                                remove_image_view.getLayoutParams().height = remove_img_height;
                                remove_image_view.getLayoutParams().width = remove_img_width;
                                onFloatingWidgetClick();
                            }

                        }


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
                        return true;
                }
                return false;
            }
        });
    }

    private void implementClickListeners() {
        mFloatingWidgetView.findViewById(R.id.close_floating_view).setOnClickListener(this);
        mFloatingWidgetView.findViewById(R.id.close_expanded_view).setOnClickListener(this);
        mFloatingWidgetView.findViewById(R.id.open_activity_button).setOnClickListener(this);
//        mFloatingWidgetView.findViewById(R.id.collapsed_iv).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_floating_view:
                //close the service and remove the from from the window
                stopSelf();
                break;
            case R.id.close_expanded_view:
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;
            case R.id.open_activity_button:
                //open the activity and stop service

                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {

//                            toast(FloatingWidgetService.this,"Clicked ONE!!");


                            */
    /*Intent intent = new Intent("android.intent.category.LAUNCHER");
                            intent.setClassName(getPackageName(), "rigo.rigotaxi.rigodrivers.ui.main.activity.MainActivity");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);*/
    /*

     */
    /*Intent intentw = new Intent(FloatingWidgetService.this, MainActivity.class);
                            intentw.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
                            // the activity from a service
                            intentw.setAction(Intent.ACTION_MAIN);
                            intentw.addCategory(Intent.CATEGORY_LAUNCHER);
                            startActivity(intentw);*/
    /*

     */
    /*Intent intent = new Intent("android.intent.action.MAIN");
                            intent.addCategory("android.intent.category.LAUNCHER");

                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.setComponent(new ComponentName(getApplicationContext().getPackageName(),
                                    "MainActivity.class"));
                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    getApplicationContext(), 0, intent, 0);
                            pendingIntent.send(getApplicationContext(), 0, intent);*/
    /*
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "There was a problem loading the application: ",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }, 1000);


                //close the service and remove view from the view hierarchy
//                stopSelf();
                break;
            case R.id.collapsed_iv:
                //open the activity and stop service

                //close the service and remove view from the view hierarchy
//                stopSelf();
                break;
        }
    }

    */
    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */ /*
    private void onFloatingWidgetLongClick() {
        //Get remove Floating view params
        WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();

        //get x and y coordinates of remove view
        int x_cord = (szWindow.x - removeFloatingWidgetView.getWidth()) / 2;
        int y_cord = szWindow.y - (removeFloatingWidgetView.getHeight() + getStatusBarHeight());


        removeParams.x = x_cord;
        removeParams.y = y_cord;

        //Update Remove view params
        mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
    }

    */
    /*  Reset position of Floating Widget view on dragging  */ /*
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }

    }


    */
    /*  Method to move the Floating widget view to Left  */ /*
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                try{
                long step = (500 - t) / 5;

                mParams.x = 0 - (int) (current_x_cord * current_x_cord * step);

//                If you want bounce effect uncomment below line and comment above line
//                 mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
                }catch (Exception e){
                    Log.i(TAG, "onTick: Errror="+e.getLocalizedMessage());
                }
            }

            public void onFinish() {
                try{
                mParams.x = 0;

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }catch (Exception e){
                Log.i(TAG, "onTick: Errror="+e.getLocalizedMessage());
            }
            }
        }.start();
    }

    */
    /*  Method to move the Floating widget view to Right  */ /*
    private void moveToRight(final int current_x_cord) {

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

            public void onTick(long t) {
                try{
                long step = (500 - t) / 5;

                mParams.x = (int) (szWindow.x + (current_x_cord * current_x_cord * step) - mFloatingWidgetView.getWidth());

                //If you want bounce effect uncomment below line and comment above line
//                  mParams.x = szWindow.x + (int) (double) bounceValue(step, current_x_cord) - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }catch (Exception e){
                Log.i(TAG, "onTick: Errror="+e.getLocalizedMessage());
            }
            }

            public void onFinish() {
                try{
                mParams.x = szWindow.x - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(mFloatingWidgetView, mParams);
            }catch (Exception e){
                Log.i(TAG, "onTick: Errror="+e.getLocalizedMessage());
            }
            }
        }.start();
    }

    */
    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */ /*
    private double bounceValue(long step, long scale) {
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }


    */
    /*  Detect if the floating view is collapsed or expanded */ /*
    private boolean isViewCollapsed() {
        return mFloatingWidgetView == null || mFloatingWidgetView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }


    */
    /*  return status bar height on basis of device display metrics  */ /*
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }


    */
    /*  Update Floating Widget view coordinates on Configuration change  */ /*
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindowManagerDefaultDisplay();

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mFloatingWidgetView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (layoutParams.y + (mFloatingWidgetView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (mFloatingWidgetView.getHeight() + getStatusBarHeight());
                mWindowManager.updateViewLayout(mFloatingWidgetView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    */
    /*  on Floating widget click show expanded view  */ /*
    private void onFloatingWidgetClick() {
        if (isViewCollapsed()) {
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.
            collapsedView.setVisibility(View.VISIBLE);
//            expandedView.setVisibility(View.VISIBLE);


            try {

                Log.i("FLOATINGWS", "onFloatingWidgetClick: Clicked");

                */
    /*Intent vintent = new Intent(FloatingWidgetService.this, MyBroadcastReceiver.class);
                sendBroadcast(vintent); */
    /*
                vibratePhone(this);
                Intent it = new Intent(this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("FLOATINGWS", "onFloatingWidgetClick: error="+e.getLocalizedMessage());
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        */
    /*  on destroy remove both view from window manager */ /*

        if (mFloatingWidgetView != null)
            mWindowManager.removeView(mFloatingWidgetView);

        if (removeFloatingWidgetView != null)
            mWindowManager.removeView(removeFloatingWidgetView);

    }
*/
    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1 // 1 minute
                ).toLong()
    }
}