package com.example.saahas.ui.Authentication

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.tertiaryContainerLight
import com.example.saahas.R
import com.example.saahas.SIGN_IN
import com.example.saahas.SIGN_UP
import com.example.saahas.SOS_SCREEN
import com.example.ui.theme.displayFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignIn(
    modifier: Modifier = Modifier,
    openAndPopUp: (String, String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as android.app.Application))
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val authResult by viewModel.authResult.observeAsState()

    // Handle auth result
    LaunchedEffect(authResult) {
        when (authResult) {
            "success" -> openAndPopUp(SOS_SCREEN, SIGN_IN) // Already handled in ViewModel, but kept for clarity
            "loading" -> { /* Show loading state if needed */ }
            else -> if (authResult?.startsWith("error") == true) {
                // Show error (e.g., Toast or Snackbar)
            }
        }
    }

    Scaffold {
        Column(
            modifier = modifier
                .padding(it)
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Text(
                modifier = Modifier.fillMaxWidth().wrapContentSize().padding(top = 48.dp),
                text = "Welcome to Saahas your safety hub",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = displayFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Unspecified
                ),
                value = email,
                onValueChange = { email = it },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(text = "Enter your email") }
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = "Enter your password") },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            painter = painterResource(
                                if (passwordVisibility) R.drawable.visibility_off else R.drawable.visibility_24
                            ),
                            contentDescription = null
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("SaahasAuth", "Sign In button clicked")
                    viewModel.login(email, password, openAndPopUp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(tertiaryContainerLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(start = 18.dp)
                )
                Text(
                    text = "Or Login with",
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                )
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(end = 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Handle Google Sign-In */ },
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(2.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp).padding(end = 8.dp),
                        painter = painterResource(R.drawable.google),
                        contentDescription = "Google_logo",
                        tint = Color.Unspecified
                    )
                    Text(text = "Sign in with Google", color = Color.Black, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                TextButton(onClick = { openAndPopUp(SIGN_UP, SIGN_IN) }) {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}