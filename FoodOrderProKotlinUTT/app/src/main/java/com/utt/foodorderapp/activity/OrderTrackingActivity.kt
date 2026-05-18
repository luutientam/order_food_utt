package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ActivityOrderTrackingBinding
import com.utt.foodorderapp.model.Order

/**
 * Theo dõi vị trí shipper realtime cho một đơn hàng cụ thể.
 * Mở từ OrderHistoryActivity bằng nút "Theo dõi đơn".
 */
class OrderTrackingActivity : BaseActivity(), OnMapReadyCallback {

    private var binding: ActivityOrderTrackingBinding? = null
    private var googleMap: GoogleMap? = null
    private var customerMarker: Marker? = null
    private var shipperMarker: Marker? = null
    private var orderId: Long = 0L
    private var orderRef: DatabaseReference? = null
    private var listener: ValueEventListener? = null
    private var hasZoomed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0L)
        if (orderId == 0L) {
            finish()
            return
        }

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.order_tracking)
        binding!!.toolbar.imgBack.setOnClickListener { finish() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_tracking) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isMyLocationButtonEnabled = false
        attachOrderListener()
    }

    private fun attachOrderListener() {
        orderRef = ControllerApplication[this].bookingDatabaseReference.child(orderId.toString())
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java) ?: return
                renderOrder(order)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        orderRef!!.addValueEventListener(listener!!)
    }

    private fun renderOrder(order: Order) {
        val map = googleMap ?: return

        binding!!.tvStatusText.text = statusLabel(order)
        binding!!.tvAddressText.text = getString(R.string.label_delivery_address) + ": " + (order.address ?: "")

        val hasCustomerLoc = order.deliveryLat != 0.0 || order.deliveryLng != 0.0
        val hasShipperLoc = order.shipperLat != 0.0 || order.shipperLng != 0.0

        if (!hasCustomerLoc && !hasShipperLoc) {
            binding!!.tvShipperText.text = getString(R.string.msg_no_tracking)
            return
        }

        if (hasCustomerLoc) {
            val customerPos = LatLng(order.deliveryLat, order.deliveryLng)
            if (customerMarker == null) {
                customerMarker = map.addMarker(MarkerOptions()
                        .position(customerPos)
                        .title(getString(R.string.label_delivery_address))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
            } else {
                customerMarker!!.position = customerPos
            }
        }

        if (hasShipperLoc) {
            val shipperPos = LatLng(order.shipperLat, order.shipperLng)
            if (shipperMarker == null) {
                shipperMarker = map.addMarker(MarkerOptions()
                        .position(shipperPos)
                        .title(getString(R.string.label_shipper_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
            } else {
                shipperMarker!!.position = shipperPos
            }
            binding!!.tvShipperText.text = getString(R.string.label_shipper_position) + ": " +
                    "%.5f, %.5f".format(order.shipperLat, order.shipperLng)
        } else {
            binding!!.tvShipperText.text = getString(R.string.shipper_location_unknown)
        }

        if (!hasZoomed) {
            zoomToFit(hasCustomerLoc, hasShipperLoc, order)
            hasZoomed = true
        }
    }

    private fun zoomToFit(hasCustomer: Boolean, hasShipper: Boolean, order: Order) {
        val map = googleMap ?: return
        when {
            hasCustomer && hasShipper -> {
                val bounds = LatLngBounds.Builder()
                        .include(LatLng(order.deliveryLat, order.deliveryLng))
                        .include(LatLng(order.shipperLat, order.shipperLng))
                        .build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
            }
            hasCustomer -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(order.deliveryLat, order.deliveryLng), 15f))
            hasShipper -> map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(order.shipperLat, order.shipperLng), 15f))
        }
    }

    private fun statusLabel(order: Order): String {
        return when (order.getStatusValue()) {
            Order.STATUS_NEW -> getString(R.string.status_new)
            Order.STATUS_PREPARING -> getString(R.string.status_preparing)
            Order.STATUS_DELIVERING -> getString(R.string.status_process)
            Order.STATUS_SUCCESS -> getString(R.string.status_success)
            Order.STATUS_CANCEL -> getString(R.string.status_cancel)
            Order.STATUS_FAIL -> getString(R.string.status_fail)
            else -> ""
        }
    }

    override fun onDestroy() {
        listener?.let { orderRef?.removeEventListener(it) }
        listener = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ORDER_ID = "EXTRA_ORDER_ID"
    }
}
