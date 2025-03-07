package com.example.saahas.ui.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.ADD_FRIENDS
import com.example.saahas.LOCATION_MAP_SCREEN
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.PermissionManager
import com.example.saahas.R
import com.example.saahas.SENSOR_SCREEN
import com.example.saahas.SOS_SCREEN
import com.example.saahas.VOICE_CONTROL_SCREEN
import com.example.saahas.VOICE_RECORDING_SCREEN
import com.example.saahas.ui.Authentication.AuthViewModel
import com.example.saahas.ui.Authentication.AuthViewModelFactory
import com.example.saahas.ui.Screens.SOS.SOSViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(
    permission: PermissionManager,
    openScreen: (String) -> Unit,
    openAndPopUp: (String, String) -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as android.app.Application))
    val viewModel: SOSViewModel = viewModel(
        factory = SOSViewModelFactory(
            context,
            permission,
            authViewModel,
            VoiceRecordingDatabase.getDatabase(context).ContactDao()
        )
    )


    var showSignOutDialog by remember { mutableStateOf(false) }

    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()





    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp)
                    .height(108.dp),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.onLocationClicked(openScreen) }) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = viewModel.locationText,
                            modifier = Modifier.clickable { viewModel.onLocationClicked(openScreen) },
                            color = Color.Blue,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onAddContactClicked { openScreen(ADD_FRIENDS) } },
                        modifier = Modifier.padding(vertical = 16.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.person_add),
                            contentDescription = "Add person"
                        )
                    }
                    IconButton(onClick = { showSignOutDialog = true },
                        modifier = Modifier.padding(vertical = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .height(88.dp) // Reduced height
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .shadow(
                        elevation = 8.dp, // Adds shadow for elevation
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        clip = true // Ensures the shadow follows the rounded corners
                    ),
                containerColor = Color.White, // Sets the background color to white
                tonalElevation = 8.dp // Adds tonal elevation
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // Adds horizontal padding
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically // Centers icons vertically
                ) {
                    IconButton(
                        onClick = { openScreen(SOS_SCREEN) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { openScreen(LOCATION_MAP_SCREEN) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { openScreen(VOICE_RECORDING_SCREEN) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_mic_24),
                            contentDescription = "Voice",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { openScreen(SENSOR_SCREEN)},
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { openScreen(VOICE_CONTROL_SCREEN) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.speaker),
                            contentDescription = "Voice buzzer",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    title = { Text("Sign Out") },
                    text = { Text("Are you sure you want to sign out?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.onSignOut(openAndPopUp)
                                showSignOutDialog = false
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showSignOutDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            EmergencyCard(
                title = "Are you in Emergency ?",
                description = "Press the button below to get help reach soon to you."
            )

            Divider(Modifier.padding(horizontal = 8.dp, vertical = 8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .size(300.dp),
                colors = CardDefaults.cardColors(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {viewModel.onSOSButtonClicked()},
                        modifier = Modifier.size(200.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.sos),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Cards(
                    text = "Track Me",
                    icon = painterResource(R.drawable.track),
                    onClick = {}
                )
                Cards(
                    text = "Community",
                    icon = painterResource(R.drawable.community),
                    onClick = {}
                )
                Cards(
                    text = "Helpline",
                    icon = painterResource(R.drawable.helpline),
                    onClick = {}
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (viewModel.showPhoneInputDialog) {
        PhoneInputDialog(
            onSave = { number -> viewModel.savePhoneNumber(number) },
            onDismiss = { viewModel.dismissPhoneDialog() }
        )
    }
}


@Composable
fun PhoneInputDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Emergency Contact") },
        text = {
            Column {
                Text("Please enter a phone number to send an emergency message.")
                TextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phoneInput.isNotBlank()) {
                        onSave(phoneInput)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun Cards(
    text: String,
    icon: Painter,
    onClick: () -> Unit
) {

    Card(modifier = Modifier.size(100.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                modifier = Modifier.size(60.dp),
                painter = icon,
                contentDescription = null,
                tint = Color.Unspecified
            )

            Text(
                text = text,
                color = Color(0xFFF16D39),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

}


@Composable
fun EmergencyCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                clip = true
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF16D39)
        ),
        shape = RoundedCornerShape(24.dp) // Rounded corners for the card
    ) {
        Column(
            modifier = Modifier.padding(16.dp) // Adds padding inside the card
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = title,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                color = Color.White // Sets text color to white
            )
            Spacer(Modifier.height(8.dp)) // Adds spacing between title and description
            Text(
                modifier = Modifier.padding(4.dp),
                text = description,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                color = Color.White // Sets text color to white
            )
        }
    }
}



