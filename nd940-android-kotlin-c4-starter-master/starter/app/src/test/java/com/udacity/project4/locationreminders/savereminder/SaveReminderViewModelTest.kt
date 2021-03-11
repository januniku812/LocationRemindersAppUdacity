package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {
    // lateinit variables
    private lateinit var remindersDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() = runBlockingTest {
        remindersDataSource = FakeDataSource()
        val application: MyApp = ApplicationProvider.getApplicationContext()
        saveReminderViewModel = SaveReminderViewModel(application, remindersDataSource)
    }

    @After
    fun tearDownViewModel() = runBlockingTest {
        stopKoin()
    }

    @Test
    fun validateReminders() = runBlockingTest {
        // creating a fake valid reminder to use to test the validate function
        val notFaultyReminder = ReminderDataItem("validateReminderTestTitle", "validateReminderTestDescription", "validateReminderTestReminderLocation",123.4, 100.0)
        // checking that the reminder is valid
        assertThat(saveReminderViewModel.validateEnteredData(notFaultyReminder), `is`(true))
        // creating a fake faulty reminder to use to test the validate function
        val faultyReminder = ReminderDataItem("", "", "",123.4, 100.0)
        // checking that the faulty reminder is invalid
        assertThat(saveReminderViewModel.validateEnteredData(faultyReminder), `is`(false))
    }

    @Test
    fun validateAndSaveReminders() = runBlockingTest {
        remindersDataSource.deleteAllReminders()
        // creating a fake valid reminder to use to test the validate function that should be saved
        val notFaultyReminder = ReminderDataItem("validateReminderTest2Title", "validateReminderTest2Description", "validateReminderTest2ReminderLocation",123.4, 100.0)
        // creating a fake faulty reminder to use to test the validate function that should NOT be saved
        val faultyReminder = ReminderDataItem("", "", "",123.4, 100.0)
        saveReminderViewModel.validateAndSaveReminder(notFaultyReminder) // should save this one
        saveReminderViewModel.validateAndSaveReminder(faultyReminder)
        // checking that the view model didn't enter/save the faulty reminder
        val error = remindersDataSource.getReminder(faultyReminder.id)
        error as Result.Error
        assertThat(error.statusCode, `is`(nullValue()))
        // checking that the view model did eneter/save the not faulty reminder
        assertThat(remindersDataSource.getReminder(notFaultyReminder.id), `is`(notNullValue()))
    }

    @Test
    fun navigateToSelectLocationFragment() = runBlockingTest {
        // creating a fake latlng and poi to test the save selected location method in the view model
        val fakeLatLng = LatLng(100.0, 110.8)
        val fakePointOfInterest = PointOfInterest(fakeLatLng, "fakePoiId", "fakePoiName")
        saveReminderViewModel.saveSelectedLocation(fakeLatLng, fakePointOfInterest)
        val expectedNavigationValue =  NavigationCommand.BackTo(R.id.action_saveReminderFragment_to_selectLocationFragment)
        // checking that the navigation command value
        assertEquals(saveReminderViewModel.navigationCommand.value, expectedNavigationValue)
        // checking all the variables in the save reminder are as expected
        assertThat(saveReminderViewModel.latitude.value, `is`(fakeLatLng.latitude))
        assertThat(saveReminderViewModel.longitude.value, `is`(fakeLatLng.longitude))
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(fakePointOfInterest))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(fakePointOfInterest.name))
    }

    @Test
    fun shouldSaveReminder() = runBlockingTest {
        remindersDataSource.deleteAllReminders()
        // creating a fake reminder to add to the database
        val reminderToAdd = ReminderDTO("shouldSaveReminderTestTitle","shouldSaveReminderTestDescription", "shouldSaveReminderTestLocation", 100.0,100.0)
        // adding/inserting it into the database
        remindersDataSource.saveReminder(reminderToAdd) // reminderToAdd should be the only reminder in it now
        assertThat(remindersDataSource.getReminders(), `is`(notNullValue()))
        // checking the individual details of the reminder pulled from the database
        val reminderPulledFromDatabase = remindersDataSource.getReminder(reminderToAdd.id)
        reminderPulledFromDatabase as Result.Success
        assertThat(reminderPulledFromDatabase.data.title, `is`(reminderToAdd.title))
        assertThat(reminderPulledFromDatabase.data.description, `is`(reminderToAdd.description))
        assertThat(reminderPulledFromDatabase.data.location, `is`(reminderToAdd.location))
        assertThat(reminderPulledFromDatabase.data.latitude, `is`(reminderToAdd.latitude))
        assertThat(reminderPulledFromDatabase.data.longitude, `is`(reminderToAdd.longitude))
    }

}
