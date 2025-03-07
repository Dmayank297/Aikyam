package com.example.simple

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saahas.ui.Screens.OnBoarding.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    val currentPage by viewModel.currentPage.observeAsState(0) // Observe LiveData
    val currentItem = viewModel.onboardingItems[currentPage]

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Content (Icon, Title, Message, Dots)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic Image from resource ID
                Image(
                    painter = painterResource(currentItem.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp) // Adjust size as needed
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Title
                Text(
                    text = currentItem.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Dynamic Description
                Text(
                    text = currentItem.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                // Progress Indicator (Dots)
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    viewModel.onboardingItems.forEachIndexed { index, _ ->
                        Dot(isActive = index == currentPage)
                        if (index < viewModel.onboardingItems.size - 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }

            // Buttons (Skip and Next) at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onSkip() },
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text(
                        text = "Skip",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.nextPage()) {
                            // Page incremented, UI will recompose
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text(
                        text = "Next",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Dot(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                color = if (isActive) Color.Black else Color.LightGray,
                shape = CircleShape
            )
    )
}

@Preview
@Composable
private fun Preview() {
    OnboardingScreen(onSkip = {}, onFinish = {})
}