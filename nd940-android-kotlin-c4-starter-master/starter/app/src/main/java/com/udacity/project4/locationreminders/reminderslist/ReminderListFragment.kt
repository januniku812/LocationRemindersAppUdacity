package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : Fragment() {
    //use Koin to retrieve the ViewModel instance
    private val _viewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // initializing the binding variable
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )

        // setting the viewModel
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)

        // setting the title that will be displayed in the action bar for this fragment
        setTitle(getString(R.string.app_name))

        binding.addReminderFAB.setOnClickListener{
            findNavController().navigate(R.id.action_navigate_to_save_reminder)
        }

        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
        }

        // observing the show no data boolean
        _viewModel.showNoData.observe(viewLifecycleOwner, Observer {
            when(it){
                true -> Toast.makeText(context, getString(R.string.no_data), Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner

        setupRecyclerView()

        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        // use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.actionNavigateToSaveReminder()
            )
        )
        ReminderListFragmentDirections.actionNavigateToSaveReminder()

    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> logout()
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    fun logout(){
        AuthUI.getInstance().signOut(requireContext())
        // going back to the authentication activity
        startActivity(Intent(context, AuthenticationActivity::class.java))

        // setting the value of the authentication state to unauthenticated
        _viewModel.setStateToUnauthenticated()
    }

}
