package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    @Before
    fun initDb(){
        // initializing remindersDatabase
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        // initializing remindersRepository
        remindersRepository = RemindersLocalRepository(remindersDatabase.reminderDao(), TestCoroutineDispatcher())
    }

    @After
    fun closeDb() = remindersDatabase.close()

    @Test
    fun getRemindersNotNull() = runBlockingTest {
        remindersRepository.deleteAllReminders()
        // creating a fake reminder to add to the repository
        val reminder = ReminderDTO("shouldSaveReminderTestTitle","shouldSaveReminderTestDescription", "shouldSaveReminderTestLocation", 100.0,100.0)
        remindersRepository.saveReminder(reminder)
        assertThat(remindersRepository.getReminders(), `is`(notNullValue()))
    }

    @Test
    fun deleteAllRemindersShouldReturnNull() = runBlockingTest {
        remindersRepository.deleteAllReminders()
        val result = remindersRepository.getReminders()
        result as Result.Success
        assertThat(result.data, `is`(emptyList()))
    }

    @Test
    fun shouldSaveReminder() = runBlockingTest {
        remindersRepository.deleteAllReminders()
        // creating a fake reminder to add to the database
        val reminderToAdd = ReminderDTO("shouldSaveReminderTestTitle","shouldSaveReminderTestDescription", "shouldSaveReminderTestLocation", 100.0,100.0)
        // adding/inserting it into the database
        remindersRepository.saveReminder(reminderToAdd) // reminderToAdd should be the only reminder in it now
        assertThat(remindersRepository.getReminders(), Matchers.`is`(Matchers.notNullValue()))
        // checking the individual details of the reminder pulled from the database
        val reminderPulledFromDatabase = remindersRepository.getReminder(reminderToAdd.id)
        reminderPulledFromDatabase as Result.Success // smart cast to get the reminder data
        assertThat(reminderPulledFromDatabase.data.title, `is`(reminderToAdd.title))
        assertThat(reminderPulledFromDatabase.data.description, `is`(reminderToAdd.description))
        assertThat(reminderPulledFromDatabase.data.location, `is`(reminderToAdd.location))
        assertThat(reminderPulledFromDatabase.data.latitude, `is`(reminderToAdd.latitude))
        assertThat(reminderPulledFromDatabase.data.longitude, `is`(reminderToAdd.longitude))
    }

    @Test
    fun checkReminderDeleted() = runBlockingTest {
        remindersRepository.deleteAllReminders()
        // creating a fake reminder to add to the database and later delete
        val reminderToAddAndThenDelete = ReminderDTO("checkReminderDeletedTestTitle","checkReminderDeletedTestDescription", "checkReminderDeletedLocation", 100.0,100.0)
        // adding/inserting it into the database
        remindersRepository.saveReminder(reminderToAddAndThenDelete)
        // deleting it
        remindersRepository.deleteReminder(reminderToAddAndThenDelete)
        // checking the individual details of the reminder pulled from the database
        val reminderPulledFromDatabase = remindersRepository.getReminder(reminderToAddAndThenDelete.id)
        // since it is no longer in the database it should be an result error
        assertThat(reminderPulledFromDatabase is Result.Error, `is`(true))
        assertThat(reminderPulledFromDatabase is Result.Success, `is`(false))
    }

    @Test
    fun getReminderByInvalidIdShouldReturnError() = runBlockingTest {
        remindersRepository.deleteAllReminders()
        // creating a fake reminder to add to the database and later delete
        val reminder = ReminderDTO("getReminderByInvalidIdShouldReturnErrorTitle","getReminderByInvalidIdShouldReturnErrorDescription", "getReminderByInvalidIdShouldReturnErrorLocation", 100.0,100.0)
        // adding it into the database
        remindersRepository.saveReminder(reminder)
        val faultyId = "324919241"
        // checking the individual details of the reminder pulled from the database
        val result = remindersRepository.getReminder(faultyId)
        // since there is no reminder in the database with that id it should be an result error
        assertThat(result is Result.Error, `is`(true))
    }

    @Test
    fun getReminderByValidId() = runBlockingTest {
        // creating a fake reminder to add to the database
        val reminder = ReminderDTO("getReminderByValidIdTitle","getReminderByValidIdDescription", "getReminderByValidIdLocation", 100.0,100.0)
        // adding it into the database
        remindersRepository.saveReminder(reminder)
        // pulling it from the database
        val reminderPulledFromDatabase = remindersRepository.getReminder(reminder.id)
        assertThat(reminderPulledFromDatabase is Result.Success, `is`(true)) // should be a success as the reminder exists in the database
        // checking the individual details of the reminder pulled from the database
        reminderPulledFromDatabase as Result.Success
        assertThat(reminderPulledFromDatabase.data.title, `is`(reminder.title))
        assertThat(reminderPulledFromDatabase.data.description, `is`(reminder.description))
        assertThat(reminderPulledFromDatabase.data.location, `is`(reminder.location))
        assertThat(reminderPulledFromDatabase.data.latitude, `is`(reminder.latitude))
        assertThat(reminderPulledFromDatabase.data.longitude, `is`(reminder.longitude)
        )
    }


}
