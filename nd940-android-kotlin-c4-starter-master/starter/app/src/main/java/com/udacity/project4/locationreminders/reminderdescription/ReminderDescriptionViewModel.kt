package com.udacity.project4.locationreminders.reminderdescription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class ReminderDescriptionViewModel(val reminderDataSource: ReminderDataSource): ViewModel() {
    private val _authenticationState = MutableLiveData<AuthenticationActivity.AuthenticationState>()
    val authenticationState: LiveData<AuthenticationActivity.AuthenticationState>
        get() = _authenticationState

    private val _reminderDeleted = MutableLiveData<Boolean>(false) // by default it is false
    val reminderDeleted: LiveData<Boolean>
        get() = _reminderDeleted

    fun deleteReminder(reminderItem: ReminderDataItem) {
        viewModelScope.launch {
            reminderDataSource.deleteReminder(
                ReminderDTO(
                id = reminderItem.id,
                title = reminderItem.title,
                description = reminderItem.description,
                location = reminderItem.location,
                longitude = reminderItem.longitude,
                latitude = reminderItem.latitude
            )
            )
            _reminderDeleted.value = true
        }
    }
}