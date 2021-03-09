package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.regex.Pattern.matches

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var remindersListViewmodel: RemindersListViewModel
    private val remindersDataSource: FakeDataSource = FakeDataSource()
//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    @Test
    fun checkAuthenticationStateUnauthenticated(){
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext) // opening the menu
        onView(withText(R.string.logout)).perform(click()) // clicking the logout option
        // after clicking the logout button the authentication should be unauthenticated
        MatcherAssert.assertThat(
            AuthenticationActivity.AuthenticationState.UNAUTHENTICATED,
            `is`(remindersListViewmodel.authenticationState.value)
        )
    }

    init {
        // initially the authentication state should be authenticated
        @Test
        fun checkAuthenticationStateAuthenticated(){
            onView(withId(R.id.logout)).check(isNotChecked() as ViewAssertion) // it should not be selected and the state should authenticated
            MatcherAssert.assertThat(
                    AuthenticationActivity.AuthenticationState.AUTHENTICATED,
                    `is`(remindersListViewmodel.authenticationState.value)
            )
        }

    }

    @Test
    fun clickOnFabNavigateToSaveReminderFragment(){
        onView(withId(R.id.addReminderFAB)).perform(click())
        // verify that it navigated to the save reminder fragment when fab clicked
        verify(mock(NavController::class.java)).navigate(ReminderListFragmentDirections.actionNavigateToSaveReminder())
    }

}

