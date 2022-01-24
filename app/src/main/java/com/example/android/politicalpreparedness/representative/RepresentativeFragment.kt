package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.model.Representative
import java.lang.Exception
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
        //Add Constant for Location request
        const val REQUEST_LOCATION_PERMISSION = 1123

    }

    //Declare ViewModel
    private val viewModel: RepresentativeViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, RepresentativeViewModelFactory()).get(RepresentativeViewModel::class.java)
    }


    private lateinit var binding: FragmentRepresentativeBinding

    private var viewModelAdapter: RepresentativeListAdapter? = null

    //private lateinit var address: Address

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_representative, container, false
        )

        // Establish bindings
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        // Define and assign Representative adapter
        viewModelAdapter = RepresentativeListAdapter()

        //Populate Representative adapter
        // Populate recycler adapters
        // Sets the adapter of the RecyclerView
        binding.representativesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.representativesRv.adapter = viewModelAdapter


        //Establish button listeners for field and location search
        binding.buttonSearch.setOnClickListener {

            hideKeyboard()
            addressToBeUpdated()
            if (viewModel.address.value != null) {
                //check new address entered
                viewModel.getRepresentativesByAddress()
            } else {
                Toast.makeText(context, R.string.address_error_msg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonLocation.setOnClickListener {
            getLocation()
            if (viewModel.address.value != null) {
                viewModel.getRepresentativesByAddress()
            }
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.states,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                binding.state.adapter = adapter
            }
        }

        return binding.root
    }

    private fun addressToBeUpdated() {
        var line1: String = binding.addressLine1.text.toString()
        var line2: String = binding.addressLine2.text.toString()
        var city: String = binding.city.text.toString()
        var zip: String = binding.zip.text.toString()
        var state: String = binding.state.selectedItem.toString()

        viewModel.setAddressFromInput(Address(line1, line2, city, state, zip))
    }

    //Refresh adapters when fragment loads
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.representatives.observe(viewLifecycleOwner, Observer<List<Representative>> { representatives ->
            representatives?.apply {
                viewModelAdapter?.submitList(representatives)
                viewModelAdapter?.notifyDataSetChanged()

            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer<String?> {
            it?.let {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                viewModel.resetErrorMsg()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Handle location permission result to get location on permission granted
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (isPermissionGranted()) {
                getLocation()
                viewModel.getRepresentativesByAddress()
            } else {
                Toast.makeText(context, "PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            //Request Location permissions
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        // Check if permission is already granted and return (true = granted, false = denied/other)
        return context?.let {
            ContextCompat.checkSelfPermission(it,
                Manifest.permission.ACCESS_FINE_LOCATION)
        } == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        //Get location from LocationServices

        try {
            var locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            criteria.isAltitudeRequired = false
            criteria.isBearingRequired = false
            criteria.isCostAllowed = true
            val strLocationProvider = locationManager.getBestProvider(criteria, true)

            if (checkLocationPermissions()) {
                strLocationProvider?.let {
                    val location: Location? = locationManager.getLastKnownLocation(strLocationProvider)
                    location?.let {
                        //address = geoCodeLocation(location)
                        viewModel.setAddressFromInput(geoCodeLocation(location))
                    } ?: run {
                        Toast.makeText(context, R.string.location_error_msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, R.string.location_error_msg, Toast.LENGTH_SHORT).show()
        }
    }

    //The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }


}