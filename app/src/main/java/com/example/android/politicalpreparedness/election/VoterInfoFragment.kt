package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse


class VoterInfoFragment : Fragment() {

    private lateinit var electionVoterInfo: Election

    //Declare ViewModel
    private val viewModel: VoterInfoViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, VoterInfoViewModelFactory(
            ElectionDatabase.getInstance(activity.application).electionDao)).get(VoterInfoViewModel::class.java)
    }

    private lateinit var binding: FragmentVoterInfoBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_voter_info, container, false
        )

        //Add ViewModel values and create ViewModel
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        arguments?.let {

            /**
            Hint: You will need to ensure proper data is provided from previous fragment.
             */

            val division = VoterInfoFragmentArgs.fromBundle(it).argDivision
            val electionId = VoterInfoFragmentArgs.fromBundle(it).argElectionId
            electionVoterInfo = VoterInfoFragmentArgs.fromBundle(it).argElection
            //electionVoterInfo = election

            // Add binding values
            binding.title.text = electionVoterInfo.name

            //Populate voter info -- hide views without provided data.
            var voterKey = division.country + ", " + division.state
            viewModel.getVoterInfo(electionId = electionId.toString(), voterKey = voterKey)

            binding.stateBallot.setOnClickListener {
                openUrl(viewModel.voterInfoResponse.value?.state?.first()?.electionAdministrationBody?.ballotInfoUrl ?: "")
            }

            binding.stateLocations.setOnClickListener {
                openUrl(viewModel.voterInfoResponse.value?.state?.first()?.electionAdministrationBody?.votingLocationFinderUrl ?: "")
            }
        }


        //Handle save button UI state & cont'd Handle save button clicks
        binding.followButton.setOnClickListener {
            clickToggleButton()
        }

        return binding.root
    }

    //Create method to load URL intents
    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun clickToggleButton() {
        viewModel.clickAction(electionVoterInfo)
    }

    //Refresh adapters when fragment loads
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer<String?> {
            it?.let {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                viewModel.resetErrorMsg()
            }
        })
    }
}