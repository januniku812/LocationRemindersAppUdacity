package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var databaseRemindersDao: RemindersDao

    @Before
    fun initDb() {
        // initializing the database variable
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        // getting the dao
        databaseRemindersDao = database.reminderDao()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertAndGetReminders() = runBlocking {
        // creating two fake reminders to add
        val reminder = ReminderDTO("reminder1Title", "reminder1Description", "Sydney",100.0, 100.0)
        val reminderTwo = ReminderDTO("reminder2Title", "reminder2Description", "Sydney", 100.0, 100.0)
        // adding it to the database
        databaseRemindersDao.saveReminder(reminder)
        databaseRemindersDao.saveReminder(reminderTwo)
        assertThat(databaseRemindersDao.getReminders().isNotEmpty(), `is`(true))
        assertThat(databaseRemindersDao.getReminders(), `is`(listOf(reminder, reminderTwo)))
    }

    @Test
    fun insertAndFindReminderById() = runBlockingTest {
        // creating a fake reminders to add
        val reminder = ReminderDTO("reminderToCheck1Title", "reminderToCheck1Description", "Texas",120.0, 100.0)
        databaseRemindersDao.saveReminder(reminder)
        // adding it to the database
        val reminderToCheck = databaseRemindersDao.getReminderById(reminder.id)
        // check the the reminderToCheck was fetched correctly by the the reminders dao
        assertThat(reminderToCheck as ReminderDTO, notNullValue()) // smart cast to avoid null exceptions
        // making sure they are the same
        assertThat(reminderToCheck.id, `is`(reminder.id))
        assertThat(reminderToCheck.title, `is`(reminder.title))
        assertThat(reminderToCheck.description, `is`(reminder.description))
        assertThat(reminderToCheck.location, `is`(reminder.location))
        assertThat(reminderToCheck.longitude, `is`(reminder.longitude))
        assertThat(reminderToCheck.latitude, `is`(reminder.latitude))
    }

    @Test
    fun insertReminderOnConflictReplace() = runBlockingTest {
        // creating identical fake reminders to add
        val reminder = ReminderDTO("onConflictTestReminderTitle",
            "onConflictTestReminderDescription",
            "Jersey",124.0,
            159.1) // should be replaced by newReminder
        val newReminder = ReminderDTO("onConflictTestReminderTitle","onConflictTestReminderDescription", "Jersey",124.0, 159.1)
        // saving them
        databaseRemindersDao.saveReminder(reminder)
        databaseRemindersDao.saveReminder(newReminder)
        // load the reminder with the original id
        val originalReminder = databaseRemindersDao.getReminderById(reminder.id)

        // Check the loaded reminder is the new reminder (that the first once has been replaced due to OnConflictStrategy.Replace)
        assertThat(originalReminder as ReminderDTO, notNullValue()) // smart cast to avoid null exceptions
        assertThat(originalReminder.title, `is`(newReminder.title))
        assertThat(originalReminder.description, `is`(newReminder.description))
        assertThat(originalReminder.location, `is`(newReminder.location))
        assertThat(originalReminder.latitude, `is`(newReminder.latitude))
        assertThat(originalReminder.longitude, `is`(newReminder.longitude))
    }

    @Test
    fun insertAndDeleteReminder() = runBlockingTest {
        // creating a fake reminder to add
        val reminder = ReminderDTO("reminderToDeleteTitle",
            "reminderToDeleteDescription",
            "Ohio",124.2,
            159.1)
        // inserting it into a database
        databaseRemindersDao.saveReminder(reminder)
        // deleting it from the database
        databaseRemindersDao.deleteReminder(reminder)
        // making sure it isn't in the database anymore
        assertThat(databaseRemindersDao.getReminderById(reminder.id), `is`(nullValue()))

    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        // creating a fake reminder to add
        val reminder = ReminderDTO("reminderToDelete2Title",
            "reminderToDelete2Description",
            "Manhattan",127.2,
            169.1)
        // inserting it into the database
        databaseRemindersDao.saveReminder(reminder)
        assertThat(databaseRemindersDao.getReminders(), `is`(notNullValue())) // should not be null before deleting all the reminders
        // deleting all the reminders up till now
        databaseRemindersDao.deleteAllReminders()
        // making sure that the database is empty
        assertThat(databaseRemindersDao.getReminders(), `is`(emptyList()))

    }

}