package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var remindersList = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        var reminderToReturn: ReminderDTO? = null
        for(reminder in remindersList){
            if(reminder.id == id){
                reminderToReturn = reminder
                break
            }
        }
        if(reminderToReturn != null){
            return Result.Success(reminderToReturn)
        }
        else{
            return Result.Error("reminder not found")
        }
    }

    override suspend fun deleteReminder(reminder: ReminderDTO) {
        remindersList.remove(reminder)
    }

    override suspend fun deleteAllReminders() {
        remindersList.removeAll(remindersList)
    }

}