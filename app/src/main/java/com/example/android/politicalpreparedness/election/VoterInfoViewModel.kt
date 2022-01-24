package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {

    //Add live data to hold voter info
    private val _voterInfoResponse = MutableLiveData<VoterInfoResponse>()
    val voterInfoResponse: LiveData<VoterInfoResponse>
        get() = _voterInfoResponse

    //Add live data for button title
    private val _buttonTitle = MutableLiveData<String>()
    val buttonTitle: LiveData<String>
        get() = _buttonTitle

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean>
        get() = _isSaved

    //Add live data to hold err msg
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    //Add var and methods to populate voter info
    fun getVoterInfo(voterKey: String, electionId: String) {

        viewModelScope.launch {
            try {
                _voterInfoResponse.value = CivicsApi.retrofitService.getVoterInfo(voterKey = voterKey, electionId = electionId)
                getElection(electionId = electionId.toInt())
            } catch (e: Exception) {
                _errorMessage.postValue("Sorry something went wrong! Please try again later!")
            }

        }
    }

    fun resetErrorMsg(){
        _errorMessage.value = null
    }

    fun clickAction(election: Election) {

        var saveAction = isSaved.value ?: false

        if (saveAction) {
            removeElection(election.id)
            //to reset values
            _isSaved.postValue(false)
            _buttonTitle.postValue( "Follow Election")
        } else {
            saveElection(election)
            //to reset values
            _isSaved.postValue(true)
            _buttonTitle.postValue( "Unfollow Election")
        }
    }

    private fun saveElection(election: Election) {
        viewModelScope.launch {
            try {
                dataSource.insertElection(election)
            } catch (e: Exception){
                _errorMessage.postValue("Sorry something went wrong! Please try again later!")
            }
        }
    }

    private fun removeElection(electionId: Int) {
        viewModelScope.launch{
            try {
                dataSource.deleteElectionById(electionId.toString())
            } catch (e: Exception){
                _errorMessage.postValue("Sorry something went wrong! Please try again later!")
            }
        }
    }

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    private fun getElection(electionId: Int) {
        viewModelScope.launch{
            try {
                var election = dataSource.getElectionById(electionId.toString())

                if (election == null) {
                    _isSaved.postValue(false)
                    _buttonTitle.postValue( "Follow Election")
                } else {
                    _isSaved.postValue(true)
                    _buttonTitle.postValue( "Unfollow Election")
                }
            } catch (e: Exception){
                _errorMessage.postValue("Sorry something went wrong! Please try again later!")
            }
        }
    }

}