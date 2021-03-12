package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.lang.Exception

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnPoiClickListener {
    // constructor
    public class SelectLocationFragment(){

    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    // lateinit varaibles
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private lateinit var selectedLatLong: LatLng
    private lateinit var selectedPointOfInterest: PointOfInterest
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProvider = FusedLocationProviderClient(context!!)

        supportMapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

        binding.saveLocationButton.setOnClickListener {
            onLocationSelectedAndSaved()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapView: MapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    // menu-related methods

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // location permission functions

    private fun checkIfForegroundPermissionEnabled(): Boolean {
        val locationSelfPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        return (PackageManager.PERMISSION_GRANTED == locationSelfPermission)
    }

    private fun onLocationSelectedAndSaved() {
        if(checkIfForegroundPermissionEnabled()){
            _viewModel.saveSelectedLocation(selectedLatLong, selectedPointOfInterest)
        }
        else{
            requestBackgroundPermission() // if it is not enabled and the user is trying to save something promt them to do so
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.enableMyLocation()
        map.setMapStyle()
        map.setOnMapLongClickListener(this)
        map.setOnPoiClickListener(this)
    }

    init {
        requestBackgroundPermission()
    }

    private fun requestBackgroundPermission(){
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissionsArray, REQUEST_FOREGROUND_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // if it was the background request we are getting back
        if(requestCode == REQUEST_FOREGROUND_REQUEST_CODE){
            // if it was granted then we don't have to do anything
            // if it was NOT granted then we have to prompt the user to do so
            if(!checkIfForegroundPermissionEnabled()){
                Snackbar.make( // we are using Snackbar over Toast as it allows actions alongside the message
                        requireView(),
                        getString(R.string.please_enable_location_access),
                        Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.enable)){
                            requestBackgroundPermission() // giving them the options again
                        }
                        .show()
            }
        }
    }

    // GoogleMap extension functions

    @SuppressLint("MissingPermission")
    private fun GoogleMap.enableMyLocation() {
        // Use base fragment method to check whether foreground and background location permissions are granted
        if (checkIfForegroundPermissionEnabled()) {
            this.isMyLocationEnabled = true
            try {
                val locationResult = fusedLocationProvider.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) {
                    if (it.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = it.result
                        selectedLatLong = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        // making/creating a marker at where the user is
                        selectedPointOfInterest = PointOfInterest(selectedLatLong, "selectedPointOfInterestID", "selectedPointOfInterestName")
                        this.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLong, 16f)) // zooming into their location
                        val marker = this.addMarker(
                                MarkerOptions()
                                        .position(selectedLatLong)
                                        .title(selectedPointOfInterest.name)
                        )
                        marker.showInfoWindow()
                    }
                }
            }
            catch (exception: Exception){
                Log.e("SelectLocationFragment", exception.toString())
            }

        }
        else {
            // Use BaseFragment method to request forground and background permissions
            requestBackgroundPermission()
        }
    }

    private fun GoogleMap.setMapStyle() {
        this.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                context!!,
                R.raw.map_style))
    }

    // companion object
    companion object {
        const val REQUEST_FOREGROUND_REQUEST_CODE = 1001
    }

    // map click methods

    override fun onMapLongClick(latLng: LatLng) {
        map.clear() // clearing any other places they might have selected before
        selectedLatLong = latLng // resetting the latlng
        selectedPointOfInterest = PointOfInterest(selectedLatLong, "myId", "selectedPoiName") // resetting the poi
        val marker = map.addMarker(
                MarkerOptions()
                        .position(latLng)
                        .title(selectedPointOfInterest.name)
                        .visible(true)
        )
        marker.showInfoWindow()
    }

    override fun onPoiClick(pointOfInterest: PointOfInterest) {
        map.clear() // clearing any other places they might have selected before
        selectedPointOfInterest = pointOfInterest // resetting the poi
        selectedLatLong = pointOfInterest.latLng // resetting the latlng

        val poiMarker = map.addMarker(MarkerOptions()
                .position(pointOfInterest.latLng)
                .title(pointOfInterest.name)
        )

        poiMarker.showInfoWindow()
    }
}
