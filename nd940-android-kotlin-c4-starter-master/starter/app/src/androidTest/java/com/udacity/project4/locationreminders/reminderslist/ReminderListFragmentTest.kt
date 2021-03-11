package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.coroutines.coroutineContext

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: AutoCloseKoinTest() {

    private lateinit var remindersListViewmodel: RemindersListViewModel
    private lateinit var remindersDataSource: ReminderDataSource
    //    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
    @Before
    fun init(){
        stopKoin()//stop the original app koin
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        ApplicationProvider.getApplicationContext(),
                        get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                        ApplicationProvider.getApplicationContext(),
                        get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        remindersDataSource = get()
        //clear the data to start fresh
        runBlocking {
            remindersDataSource.deleteAllReminders()
        }
        remindersListViewmodel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersDataSource)
    }

    @Test
    fun noDataDisplayed() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
    }

    @Test
    fun clickOnFabNavigateToSaveReminderFragment(){
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(R.id.saveReminderFragment)
    }

    @Test
    fun remindersAreDisplayed() {
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.01, 1.02)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 2.01, 2.02)
        runBlocking {
            remindersDataSource.saveReminder(reminder1)
            remindersDataSource.saveReminder(reminder2)
        }
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        // reminder1 should be shown in the recycler view
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.description)).check(matches(isDisplayed()))
        onView(withText(reminder1.location)).check(matches(isDisplayed()))
        // reminder2 should be shown in the recycler view
        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder2.location)).check(matches(isDisplayed()))
    }

    @Test
    fun checkAuthenticationStateUnauthenticated(){
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext) // opening the menu
        onView(withId(R.id.logout)).perform(click()) // clicking the logout option
        // after clicking the logout button the authentication should be unauthenticated
        assertThat(
                remindersListViewmodel.authenticationState.value, `is`(AuthenticationActivity.AuthenticationState.UNAUTHENTICATED)
        )
    }


}

