package com.utt.foodorderapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.utt.foodorderapp.R

class MapPickerActivity : BaseActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)
        initToolbar()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        findViewById<View>(R.id.tv_confirm).setOnClickListener { onClickConfirm() }
        renderSelectedLocation()
    }

    private fun initToolbar() {
        findViewById<View>(R.id.img_back).visibility = View.VISIBLE
        findViewById<View>(R.id.img_cart).visibility = View.GONE
        findViewById<android.widget.TextView>(R.id.tv_title).text = getString(R.string.pick_location_on_map)
        findViewById<View>(R.id.img_back).setOnClickListener { finish() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val defaultLocation = LatLng(21.0278, 105.8342)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
        map.setOnMapLongClickListener { latLng ->
            selectedLocation = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title(getString(R.string.selected_location)))
            renderSelectedLocation()
        }
    }

    private fun renderSelectedLocation() {
        val location = selectedLocation
        if (location == null) {
            findViewById<android.widget.TextView>(R.id.tv_selected_location).text = getString(R.string.map_pick_hint)
            return
        }
        findViewById<android.widget.TextView>(R.id.tv_selected_location).text =
                "${getString(R.string.selected_location)}: ${location.latitude}, ${location.longitude}"
    }

    private fun onClickConfirm() {
        val location = selectedLocation ?: return
        val data = Intent()
        data.putExtra(EXTRA_LAT, location.latitude)
        data.putExtra(EXTRA_LNG, location.longitude)
        setResult(RESULT_OK, data)
        finish()
    }

    companion object {
        const val EXTRA_LAT = "EXTRA_LAT"
        const val EXTRA_LNG = "EXTRA_LNG"
    }
}
