package com.example.saahas.ui.Screens.Button.Volume

import androidx.collection.mutableIntListOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VolumeButton(viewModel: VolumeButtonViewModel) {
    val pressCount by viewModel.volumeUpButtonPressCount.observeAsState(0)
    val holdDuration by viewModel.buttonHoldDuration.observeAsState(0L)


    val phoneNumber = remember {
        mutableIntListOf(9368489937.toInt(), 93684899373.toInt())
    }


    // UI layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        // Display the press count
        Text(
            text = "Volume Up Button Press Count: $pressCount",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Display the hold duration
        Text(
            text = "Volume Down Button Hold Duration: ${holdDuration}ms",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display action feedback dynamically
        val actionFeedback = when {
            pressCount >= 2 -> "Action Triggered: Volume Up pressed $pressCount times!"
            holdDuration >= viewModel.getHoldThreshold() ->
                "Action Triggered: Volume Down held for ${holdDuration / 1000} seconds!"

            else -> {
                if (pressCount > 0) {
                    "Volume Up Button Pressed"
                } else if (holdDuration > 0) {
                    "Volume Down Button Held"
                } else {
                    ""
                }
            }
        }
        Text(
            text = actionFeedback,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

