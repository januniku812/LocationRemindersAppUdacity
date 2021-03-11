package com.udacity.project4.locationreminders.reminderslist

import android.annotation.TargetApi
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {
    // lateinit variables
    private lateinit var remindersListViewModel: RemindersListViewModel
    private val remindersDataSource: FakeDataSource = FakeDataSource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


   @Before
    fun setUpViewModel(){
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersDataSource)
    }

    @After 
    fun tearDownViewModel(){
        stopKoin()
    }


    @Test
    fun check_loading() = runBlockingTest {
        remindersDataSource.deleteAllReminders()
        // creating a fake reminder to add
        val reminder = ReminderDTO("checkLoadingTestReminderTitle", "checkLoadingTestReminderDescription", "checkLoadingTestReminderLocation",123.4, 100.0)
        // inserting the reminder into the database
        remindersDataSource.saveReminder(reminder)
        // loading the reminder
        remindersListViewModel.loadReminders()
        val listOfReminderDataItems = remindersListViewModel.remindersList.value!!

        assertThat(listOfReminderDataItems,`is` (notNullValue()))
        assertThat(remindersListViewModel.showNoData.value, `is`(false))

    }

    @Test
    fun loadRemindersListIsEmpty() = runBlockingTest {
        remindersDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        val listOfReminderDataItems = remindersListViewModel.remindersList.value

        // checking the list is empty
        assertThat(listOfReminderDataItems as List<ReminderDTO>, // smart cast to avoid null exceptions and safe vasts
            `is`(emptyList())
        )
       // should show no data text
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun loadRemindersListIsNotEmpty() = runBlockingTest {
        // creating a fake reminder to add
        remindersDataSource.deleteAllReminders()
        val reminder = ReminderDTO("loadRemindersListNotEmptyReminderTitle", "loadRemindersListNotEmptyReminderDescription", "loadRemindersListNotEmptyReminderLocation",143.4, 100.0)
        remindersDataSource.saveReminder(reminder)

        val list = remindersDataSource.getReminders()
        // checking that the list is not empty/null
        assertThat(list as Result.Success, notNullValue())
        assertThat(list.data.isEmpty(), `is`(false))
        assertThat(remindersListViewModel.showNoData.value, `is`(not(true)))
    }

    @Test
    fun shouldReturnError() = runBlockingTest  {
        remindersDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        // should show a snackbar alerting the user there are no reminders
        val snackBar = remindersListViewModel.showSnackBar.value
        // should show a snackbar
        assertThat(snackBar, `is`(nullValue()))
        // should show no data text
        assertThat(remindersListViewModel.showNoData.value, `is`(true))

    }
}
