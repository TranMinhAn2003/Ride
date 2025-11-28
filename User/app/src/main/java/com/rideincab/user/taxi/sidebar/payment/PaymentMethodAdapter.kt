package com.rideincab.user.taxi.sidebar.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rideincab.user.R
import com.rideincab.user.common.configs.SessionManager
import com.rideincab.user.common.network.AppController
import com.rideincab.user.common.utils.CommonKeys
import com.rideincab.user.taxi.datamodels.PaymentMethodsModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import javax.inject.Inject


class PaymentMethodAdapter(private var context:Context, private var paymentlist: ArrayList<PaymentMethodsModel.PaymentMethods>, private var listener:ItemClickListener):RecyclerView.Adapter<PaymentMethodAdapter.PaymentViewHolder>() {
    private var paymentmethodlist=ArrayList<PaymentMethodsModel>()
    @Inject
    lateinit var  sessionManager: SessionManager

    init {
        AppController.appComponent.inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_payment_method,parent,false)

        return PaymentViewHolder(view)
    }

    override fun getItemCount(): Int {

            return paymentlist.size

    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val paymentmode= paymentlist.get(position)

        holder.tv_paymentname.text=paymentmode.paymenMethodvalue
        Picasso.get().load(paymentmode.paymenMethodIcon).into(holder.iv_paymentIcon)
        if(paymentmode.isDefaultPaymentMethod && !CommonKeys.isSetPaymentMethod)
        {
            if(paymentmode.paymenMethodKey.contains(context.resources.getString(R.string.stripe),ignoreCase = true) && !paymentmode.paymenMethodKey.equals(context.resources.getString(R.string.stripe),ignoreCase = true))
            {
                holder.iv_default.visibility=View.GONE
                Picasso.get().load(paymentmode.paymenMethodIcon).into(holder.iv_paymentIcon,object : Callback{
                    override fun onSuccess() {
                        holder.iv_paymentIcon.setColorFilter(
                            ContextCompat.getColor(context,
                            R.color.app_primary_color));
                    }

                    override fun onError(e: Exception?) {
                        TODO("Not yet implemented")
                    }
                })

                CommonKeys.isSetPaymentMethod=false
            }else
            {

                if(CommonKeys.isWallet==0) {
                    holder.iv_default.visibility=View.VISIBLE
                    sessionManager.paymentMethodkey=paymentmode.paymenMethodKey
                    sessionManager.paymentMethod=paymentmode.paymenMethodvalue
                    sessionManager.paymentMethodImage=paymentmode.paymenMethodIcon
                    CommonKeys.isSetPaymentMethod=true
                }else{
                    holder.iv_default.visibility=View.GONE
                    CommonKeys.isSetPaymentMethod=false
                }
            }

        }else if(sessionManager.paymentMethodkey.equals(paymentmode.paymenMethodKey,ignoreCase = true) && CommonKeys.isWallet==0)
        {
                holder.iv_default.visibility = View.VISIBLE

        }else if(sessionManager.walletPaymentMethodkey.equals(paymentmode.paymenMethodKey,ignoreCase = true) && CommonKeys.isWallet==1)
        {
            holder.iv_default.visibility = View.VISIBLE
        }
         else
            holder.iv_default.visibility=View.GONE
        holder.rowview.setOnClickListener(View.OnClickListener {
          if(paymentmode.paymenMethodKey.contains(context.resources.getString(R.string.stripe),ignoreCase = true) && !paymentmode.paymenMethodKey.equals(context.resources.getString(R.string.stripe),ignoreCase = true))
          {
             listener.onItemClick()
              return@OnClickListener
          }
            sessionManager.walletPaymentMethod=paymentmode.paymenMethodvalue
            sessionManager.paymentMethod=paymentmode.paymenMethodvalue
            sessionManager.walletPaymentMethodkey=paymentmode.paymenMethodKey
            sessionManager.paymentMethodkey=paymentmode.paymenMethodKey
            sessionManager.paymentMethodImage=paymentmode.paymenMethodIcon
            CommonKeys.isSetPaymentMethod=true
            notifyDataSetChanged()

        })
    }
    class PaymentViewHolder(val rowview:View):RecyclerView.ViewHolder(rowview)
    {
        internal var tv_paymentname: TextView
        internal var iv_paymentIcon:ImageView
        internal var iv_default:ImageView
        internal var ll_payment:RelativeLayout
        init {
            iv_paymentIcon=rowview.findViewById(R.id.iv_paymentIcon)
            tv_paymentname=rowview.findViewById(R.id.tv_paymentName)
            iv_default=rowview.findViewById(R.id.iv_isSelected)
            ll_payment=rowview.findViewById(R.id.rtl_paymentMode)
        }
    }

    interface ItemClickListener{
        fun onItemClick()
    }
}