package com.example.foodmart.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodmart.ui.theme.FoodmartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start timer immediately when activity is created
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3 seconds

        setContent {
            FoodmartTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val bounce = remember { Animatable(0f) }

    // Animations
    LaunchedEffect(key1 = "animations") {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )

        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(key1 = "rotation") {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    LaunchedEffect(key1 = "bounce") {
        bounce.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Colorful gradient
    val gradientBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFF6B35), // Orange
            Color(0xFFFF8E53), // Light orange
            Color(0xFFFFB347), // Peach
            Color(0xFFFFD23F), // Yellow
            Color(0xFF06D6A0), // Mint green
        ),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        // Floating food emojis
        FloatingFoodItems()

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B35),
                                        Color(0xFF06D6A0)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGroceryStore,
                            contentDescription = "FoodMart Logo",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App title
            Card(
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛒 FoodMart",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "🍎 Fresh 🚀 Fast 🛍️ Convenient",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF06D6A0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Loading section
            Card(
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(horizontal = 48.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🍕🍔🍟",
                        fontSize = 20.sp,
                        modifier = Modifier.rotate(rotation.value)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    CircularProgressIndicator(
                        color = Color(0xFFFF6B35),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Loading...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                }
            }
        }

        // Bottom message
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(alpha.value)
                .scale(1f + bounce.value * 0.05f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "🌟 Welcome to your favorite food store! 🌟",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun FloatingFoodItems() {
    val items = listOf("🍎", "🥕", "🍞", "🥛", "🧀", "🍇", "🥬", "🍅")
    val positions = remember {
        items.mapIndexed { index, _ ->
            Pair(
                (0..100).random(),
                (index * 120 + 50) % 800
            )
        }
    }

    items.forEachIndexed { index, emoji ->
        val animatable = remember { Animatable(0f) }

        LaunchedEffect(key1 = index) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000 + index * 200),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier
                .offset(
                    x = (positions[index].first).dp,
                    y = (positions[index].second + animatable.value * 20).dp
                )
                .alpha(0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FoodmartTheme {
        SplashScreen()
    }
}
