package com.example.android.politicalpreparedness.election

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.databinding.BindingAdapter
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import java.util.*


@BindingAdapter("checkVisible")
fun bindTextViewVisible(textView: TextView, voterInfoResponse: VoterInfoResponse?) {

    if (voterInfoResponse != null) {
        textView.visibility = View.VISIBLE
    } else {
        textView.visibility = View.INVISIBLE
    }

}


@BindingAdapter("address")
fun bindTextViewToAddress(textView: TextView, voterInfoResponse: VoterInfoResponse?) {

    var state = voterInfoResponse?.state?.first()

    state?.let {
        textView.text = state.electionAdministrationBody.correspondenceAddress?.toFormattedString()
    }

}