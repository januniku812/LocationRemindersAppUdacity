package com.udacity.project4.locationreminders.reminderdescription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    // viewModel variable
    private val reminderDescriptionViewModel: ReminderDescriptionViewModel by lazy {
        ViewModelProvider(this).get(ReminderDescriptionViewModel::class.java)
    }

    // geofencing client
    private val geofencingClient = LocationServices.getGeofencingClient(this)


    // companion object
    companion object {
        private const val EXTRA_ReminderDataItemName = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItemName, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initializing the binding variable
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        // setting the reminder item to bind
        val reminderDataItemFromIntent =
            intent.extras?.getSerializable(EXTRA_ReminderDataItemName) as ReminderDataItem
        binding.reminderDataItem = reminderDataItemFromIntent

        binding.deleteButton.setOnClickListener {
            reminderDescriptionViewModel.deleteReminder(reminderDataItemFromIntent)
        }

        // observing wheter the selected reminder has been deleted
        reminderDescriptionViewModel.reminderDeleted.observe(this, Observer {
            when (it) {
                true -> removeGeofenceFromGeofencingClient(reminderDataItemFromIntent)
            }
        })

    }

    private fun removeGeofenceFromGeofencingClient(reminder: ReminderDataItem) {
        geofencingClient.removeGeofences(mutableListOf(reminder.id))
            .addOnCompleteListener {
                if(it.isSuccessful){
                    // go back to the reminders activity with the lsit
                    val intent = Intent(this, RemindersActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    // make this toast when both the geofence and reminder are removed
                    Toast.makeText(applicationContext, getString(R.string.reminder_successfully_removed), Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }
                // if it is either canceled or the task failed
                else {
                    Toast.makeText(applicationContext, getString(R.string.problem_occured_removing_geofence), Toast.LENGTH_SHORT).show()
                    // retry removing the geofence
                    removeGeofenceFromGeofencingClient(reminder)
                }
        } // once it completes
    }

}
