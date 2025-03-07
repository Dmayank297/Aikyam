package com.example.saahas.ui.Screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.Models.Contact
import com.example.saahas.Models.Room.VoiceRecordingDatabase
import com.example.saahas.PermissionManager
import com.example.saahas.ui.AddFriends.AddFriendsViewModelFactory
import com.example.ui.theme.displayFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriends(
    permissionManager: PermissionManager,
    openScreen: (String) -> Unit,
    popUpScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AddFriendsViewModel = viewModel(
        factory = AddFriendsViewModelFactory(
            context,
            permissionManager,
            VoiceRecordingDatabase.getDatabase(context).ContactDao()
        )
    )

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleContactPicked(result.data)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = {
                    Text(
                        "Add Friends",
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onBackClicked(popUpScreen) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to SOS"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddContactClicked(contactPickerLauncher::launch) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .shadow(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Contact"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.contactList.isEmpty()) {
                Text(
                    "No contacts added yet!",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                viewModel.contactList.forEach { contact ->
                    ContactCard(contact = contact, viewModel = viewModel)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ContactCard(contact: Contact, viewModel: AddFriendsViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    var editDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(contact.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            clip = true
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF16D39)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Contact",
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.name, fontWeight = FontWeight.Bold)
                Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        editDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        viewModel.deleteContact(contact)
                    }
                )
            }
        }
    }

    if (editDialog) {
        AlertDialog(
            onDismissRequest = { editDialog = false },
            title = { Text("Edit Contact Name") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateContact(contact, newName)
                    editDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { editDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}