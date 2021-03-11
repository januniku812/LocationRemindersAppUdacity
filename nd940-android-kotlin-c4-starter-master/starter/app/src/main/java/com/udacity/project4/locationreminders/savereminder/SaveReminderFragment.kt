package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient

    // onCreateView() method
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  {
        // initializing the binding variable
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            findNavController().navigate(R.id.selectLocationFragment)
        }

        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        binding.viewModel = _viewModel

        return binding.root
    }

    // menu-related methods
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
                // going back to the authentication activity
                startActivity(Intent(context, AuthenticationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    // onViewCreated method
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            findNavController().navigate(R.id.selectLocationFragment)
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

            // saving the details of the reminder
            val reminderItem = ReminderDataItem(
                title,
                description.toString(),
                location,
                latitude.value,
                longitude
            )

            // using a SaveReminderViewModel function to verify the reminder
            val isValidReminderBoolean = _viewModel.validateEnteredData(reminderItem)

            // if its valid then save it
            if (isValidReminderBoolean) {
                saveReminderAndAddGeofence(reminderItem)
                // making toast letting the user know it was saved
                Toast.makeText(context, R.string.reminder_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // onDestroy() method
    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    @SuppressLint("MissingPermission")
    private fun saveReminderAndAddGeofence(reminderItem: ReminderDataItem) {
        if (checkIfBackgroundPermissionGranted()) {
            // geofence builder
            val geofence = Geofence.Builder()
                .setRequestId(reminderItem.id)
                .setCircularRegion(reminderItem.latitude!!, // cannot be null
                    reminderItem.longitude!!,
                    geofenceRadius // Radius in meters
                )
                .setExpirationDuration(TimeUnit.HOURS.toMillis(geofenceExpirationDuration))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setRequestId(reminderItem.id)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
            intent.putExtra("reminderId", reminderItem.id)
            val geofencePendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                // if adding the geofence failed
                // it may fail midway, etc
                this.addOnFailureListener {
                    val message = if (it.message != null) it.message else "-100"
                    val temp = message!!.replace(": ", "")

                    val errorCode = when (Integer.parseInt(temp)) {
                        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> R.string.enable_google_location_services
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> R.string.too_many_geofences
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> R.string.too_many_geofences
                        else -> R.string.unknown_error
                    }
                    _viewModel.showSnackBarInt.value = errorCode
                }
                // once it completes
                this.addOnCompleteListener{
                    if(it.isSuccessful){
                        _viewModel.validateAndSaveReminder(reminderItem)
                        Toast.makeText(context,
                            getString(R.string.geofence_added_successfully),
                            Toast.LENGTH_SHORT).show()
                    }
                    else if(it.isCanceled){
                        // if adding the geofence was canceled
                        Toast.makeText(context,
                            getString(R.string.geofences_canceled),
                            Toast.LENGTH_SHORT).show()
                    }

                }
            }

        } else {
            if (!checkIfBackgroundPermissionGranted()) {
                val builder = AlertDialog.Builder(context!!)
                builder
                    .setTitle(getString(R.string.please_enable_location_access))
                    .setMessage(R.string.background_location_required)
                    .setPositiveButton(R.string.enable) { dialog: DialogInterface?, int: Int ->
                        requestBackgroundPermissions()
                    }
                    .setNegativeButton(R.string.close) { dialog: DialogInterface?, int: Int ->
                        dialog?.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // if it was the background request we are getting back
        if(requestCode == REQUEST_BACKGROUND_CODE){
            // if it was granted then we don't have to do anything
            // if it was NOT granted then we have to prompt the user to do so
            if(!checkIfBackgroundPermissionGranted()){
                Snackbar.make( // we are using Snackbar over Toast as it allows actions alongside the message
                    requireView(),
                    getString(R.string.please_enable_location_access),
                    Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.enable)){
                        requestBackgroundPermissions() // giving them the options again
                    }
                    .show()
            }
        }
    }

    @TargetApi(29)
    private fun requestBackgroundPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) { // sdk has to be at least 29
            val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            requestPermissions(permissionsArray, REQUEST_BACKGROUND_CODE)
        }
    }

    private fun checkIfBackgroundPermissionGranted(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) { // sdk has to be at least 29
            return PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        requireActivity(), // cannot null
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION // manifest permission
                    )
        }
        return false
    }

    // companion object
    companion object{
        // constant variables
        const val REQUEST_BACKGROUND_CODE = 1002 // code for background permission
        const val geofenceExpirationDuration: Long = 24 * 7 * 52
        const val geofenceRadius = 100f
    }
}
