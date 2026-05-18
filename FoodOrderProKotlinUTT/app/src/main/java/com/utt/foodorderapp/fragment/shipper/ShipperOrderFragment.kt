package com.utt.foodorderapp.fragment.shipper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Looper
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.ShipperMainActivity
import com.utt.foodorderapp.adapter.ShipperOrderAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.FragmentShipperOrderBinding
import com.utt.foodorderapp.fragment.BaseFragment
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.prefs.DataStoreManager

class ShipperOrderFragment : BaseFragment() {

    private var binding: FragmentShipperOrderBinding? = null
    private var listOrder: MutableList<Order> = ArrayList()
    private var adapter: ShipperOrderAdapter? = null
    private var listener: ChildEventListener? = null
    private var isPickupMode = true
    private var locationClient: FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val last = result.lastLocation ?: return
            publishLocationToDeliveringOrders(last.latitude, last.longitude)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentShipperOrderBinding.inflate(inflater, container, false)
        isPickupMode = arguments?.getBoolean(ARG_PICKUP_MODE, true) == true
        adapter = ShipperOrderAdapter(isPickupMode, listOrder, object : ShipperOrderAdapter.IShipperOrderListener {
            override fun onPrimaryAction(order: Order) {
                if (isPickupMode) {
                    claimOrder(order)
                } else {
                    finishOrder(order, Order.STATUS_SUCCESS)
                }
            }

            override fun onSecondaryAction(order: Order) {
                if (isPickupMode) {
                    rejectOrder(order)
                } else {
                    reportIssueAndFail(order)
                }
            }

            override fun onContactCustomer(order: Order) {
                contactCustomer(order)
            }
        })
        binding!!.rcvOrder.layoutManager = LinearLayoutManager(activity)
        binding!!.rcvOrder.adapter = adapter
        subscribeOrders(isPickupMode)
        if (!isPickupMode) {
            startLocationUpdates()
        }
        return binding!!.root
    }

    override fun initToolbar() {
        val isPickupMode = arguments?.getBoolean(ARG_PICKUP_MODE, true) == true
        val title = if (isPickupMode) getString(R.string.assign_shipper) else getString(R.string.my_delivery_orders)
        (activity as? ShipperMainActivity)?.setToolBar(title)
    }

    private fun subscribeOrders(isPickupMode: Boolean) {
        val currentActivity = activity ?: return
        val user = DataStoreManager.user ?: return
        val userId = user.uid ?: return
        listener?.let { ControllerApplication[currentActivity].bookingDatabaseReference.removeEventListener(it) }
        listOrder.clear()
        listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val order = snapshot.getValue(Order::class.java) ?: return
                if (shouldDisplayOrder(order, isPickupMode, userId)) {
                    listOrder.add(0, order)
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val order = snapshot.getValue(Order::class.java) ?: return
                var index = -1
                for (i in listOrder.indices) {
                    if (listOrder[i].id == order.id) {
                        index = i
                        break
                    }
                }
                val shouldDisplay = shouldDisplayOrder(order, isPickupMode, userId)
                if (index >= 0 && shouldDisplay) {
                    listOrder[index] = order
                } else if (index >= 0) {
                    listOrder.removeAt(index)
                } else if (shouldDisplay) {
                    listOrder.add(0, order)
                }
                adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java) ?: return
                listOrder.removeAll { it.id == order.id }
                adapter?.notifyDataSetChanged()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ControllerApplication[currentActivity].bookingDatabaseReference.addChildEventListener(listener!!)
    }

    private fun claimOrder(order: Order) {
        val currentActivity = activity ?: return
        val user = DataStoreManager.user ?: return
        if (!order.canShipperTake(user.uid)) {
            showToastMessage(currentActivity, getString(R.string.msg_order_cannot_update_status))
            return
        }
        val map: MutableMap<String, Any> = HashMap()
        map["shipperId"] = user.uid ?: ""
        map["shipperEmail"] = user.email ?: ""
        map["status"] = Order.STATUS_DELIVERING
        map["completed"] = false
        map["shipperLat"] = 0.0
        map["shipperLng"] = 0.0
        ControllerApplication[currentActivity].bookingDatabaseReference.child(order.id.toString()).updateChildren(map)
    }

    private fun rejectOrder(order: Order) {
        val currentActivity = activity ?: return
        val userId = DataStoreManager.user?.uid
        if (!order.isAssignedToShipper(userId)) {
            showToastMessage(currentActivity, getString(R.string.msg_order_cannot_update_status))
            return
        }
        val map: MutableMap<String, Any?> = HashMap()
        map["shipperId"] = null
        map["shipperEmail"] = null
        map["status"] = Order.STATUS_PREPARING
        map["completed"] = false
        ControllerApplication[currentActivity].bookingDatabaseReference.child(order.id.toString()).updateChildren(map)
    }

    private fun finishOrder(order: Order, status: Int) {
        val currentActivity = activity ?: return
        val userId = DataStoreManager.user?.uid
        if (!order.isAssignedToShipper(userId) || order.getStatusValue() != Order.STATUS_DELIVERING) {
            showToastMessage(currentActivity, getString(R.string.msg_order_cannot_update_status))
            return
        }
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        map["completed"] = status == Order.STATUS_SUCCESS
        ControllerApplication[currentActivity].bookingDatabaseReference.child(order.id.toString()).updateChildren(map)
    }

    private fun reportIssueAndFail(order: Order) {
        val currentActivity = activity ?: return
        val input = EditText(currentActivity)
        input.hint = "Nhập nội dung sự cố giao hàng"
        AlertDialog.Builder(currentActivity)
                .setTitle(getString(R.string.status_fail))
                .setView(input)
                .setPositiveButton(getString(R.string.action_ok)) { _, _ ->
                    val issue = input.text.toString().trim()
                    if (issue.isEmpty()) {
                        showToastMessage(currentActivity, "Vui lòng nhập nội dung sự cố")
                        return@setPositiveButton
                    }
                    finishOrderWithIssue(order, Order.STATUS_FAIL, issue)
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun finishOrderWithIssue(order: Order, status: Int, issueNote: String) {
        val currentActivity = activity ?: return
        val userId = DataStoreManager.user?.uid
        if (!order.isAssignedToShipper(userId) || order.getStatusValue() != Order.STATUS_DELIVERING) {
            showToastMessage(currentActivity, getString(R.string.msg_order_cannot_update_status))
            return
        }
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        map["completed"] = status == Order.STATUS_SUCCESS
        map["issueNote"] = issueNote
        ControllerApplication[currentActivity].bookingDatabaseReference.child(order.id.toString()).updateChildren(map)
    }

    private fun contactCustomer(order: Order) {
        val currentActivity = activity ?: return
        val phone = order.phone?.trim().orEmpty()
        if (phone.isEmpty()) {
            showToastMessage(currentActivity, getString(R.string.msg_get_date_error))
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    }

    private fun shouldDisplayOrder(order: Order, isPickupMode: Boolean, userId: String): Boolean {
        if (isPickupMode) {
            return order.getStatusValue() == Order.STATUS_PREPARING && order.shipperId.isNullOrEmpty()
        }
        return order.isAssignedToShipper(userId) && order.getStatusValue() == Order.STATUS_DELIVERING
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val ctx = activity ?: return
        if (!hasLocationPermission()) return
        if (locationClient == null) {
            locationClient = LocationServices.getFusedLocationProviderClient(ctx)
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
                .setMinUpdateDistanceMeters(50f)
                .build()
        locationClient?.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        locationClient?.removeLocationUpdates(locationCallback)
    }

    private fun hasLocationPermission(): Boolean {
        val currentActivity = activity ?: return false
        val fineGranted = ContextCompat.checkSelfPermission(
                currentActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
                currentActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private fun publishLocationToDeliveringOrders(lat: Double, lng: Double) {
        val currentActivity = activity ?: return
        if (isPickupMode) return
        val userId = DataStoreManager.user?.uid ?: return
        val deliveringOrders = listOrder.filter { it.isAssignedToShipper(userId) && it.getStatusValue() == Order.STATUS_DELIVERING }
        if (deliveringOrders.isEmpty()) return
        val payload: MutableMap<String, Any> = HashMap()
        payload["shipperLat"] = lat
        payload["shipperLng"] = lng
        for (order in deliveringOrders) {
            ControllerApplication[currentActivity].bookingDatabaseReference
                    .child(order.id.toString())
                    .updateChildren(payload)
        }
    }

    override fun onDestroyView() {
        stopLocationUpdates()
        val currentActivity = activity
        if (currentActivity != null && listener != null) {
            ControllerApplication[currentActivity].bookingDatabaseReference.removeEventListener(listener!!)
        }
        listener = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_PICKUP_MODE = "ARG_PICKUP_MODE"
        private const val LOCATION_UPDATE_INTERVAL_MS = 15000L

        fun newInstance(isPickupMode: Boolean): ShipperOrderFragment {
            val fragment = ShipperOrderFragment()
            val args = Bundle()
            args.putBoolean(ARG_PICKUP_MODE, isPickupMode)
            fragment.arguments = args
            return fragment
        }
    }
}
