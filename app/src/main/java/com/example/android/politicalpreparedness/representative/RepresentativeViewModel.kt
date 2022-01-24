package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.*
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    //Establish live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    //Add live data to hold err msg
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    //Create function to fetch representatives from API from a provided address
    fun getRepresentativesByAddress() {

        viewModelScope.launch {
            try {
                _address.value?.let {
                    val (offices, officials) = CivicsApi.retrofitService.getRepresentatives(address = it?.toFormattedString())
                    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
                } ?: run {
                    _errorMessage.postValue("Something is wrong with adress! Please try again later!")
                }

            } catch (e: Exception) {
                _errorMessage.postValue("Sorry something went wrong! Please try again later!")
            }

        }
    }

    /**
     *  The following code will prove helpful in constructing a representative from the API.
     *  This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */

    //Create function get address from geo location
    //Create function to get address from individual fields
    fun setAddressFromInput(address: Address) {
        _address.value = address
    }

    fun resetErrorMsg() {
        _errorMessage.value = null
    }
}

class RepresentativeViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepresentativeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepresentativeViewModel() as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }

}