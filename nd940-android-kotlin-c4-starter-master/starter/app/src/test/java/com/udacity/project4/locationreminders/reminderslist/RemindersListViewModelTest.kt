package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    // lateinit variables
    private lateinit var remindersListViewModel: RemindersListViewModel
    private val remindersDataSource: FakeDataSource = FakeDataSource()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @Before
    fun setUpViewModel(){
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersDataSource)
    }

    @After 
    fun tearDownViewModel(){
        stopKoin()
    }

    init {
        // initially the authentication state should be authenticated
        @Test
        fun checkAuthenticationStateAuthenticated(){
            assertThat(AuthenticationActivity.AuthenticationState.AUTHENTICATED, `is`(remindersListViewModel.authenticationState.value))
        }
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
        assertThat(listOfReminderDataItems.isEmpty(), `is`(true))
       // should show no data text
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun loadRemindersListIsNotEmpty() = runBlockingTest {
        // creating a fake reminder to add
        remindersDataSource.deleteAllReminders()
        val reminder = ReminderDTO("loadRemindersListNotEmptyReminderTitle", "loadRemindersListNotEmptyReminderDescription", "loadRemindersListNotEmptyReminderLocation",143.4, 100.0)
        remindersDataSource.saveReminder(reminder)

        val list = remindersListViewModel.remindersList.value
        // checking that the list is not empty/null
        assertThat(list as List<ReminderDTO>, notNullValue())
        assertThat(list.isEmpty(), `is`(false))
        assertThat(list.isNotEmpty(), `is`(true))
        assertThat( remindersListViewModel.showNoData.value, `is`(false))
    }

    @Test
    fun shouldReturnError() = runBlockingTest {
        remindersDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        // should show a snackbar alerting the user there are no reminders
        val snackBar = remindersListViewModel.showSnackBar.value
        // should show a snackbar
        assertThat(snackBar, `is`(notNullValue()))
        // should show no data text
        assertThat(remindersListViewModel.showNoData.value, `is`(true))

    }
}