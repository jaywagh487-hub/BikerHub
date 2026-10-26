
package com.example.bikerhub
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bikerhub.ui.auth.LoginScreen
import com.example.bikerhub.ui.auth.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {
            BikerHubApp()
        }
    }

    @Composable
    private fun BikerHubApp() {

        var currentScreen by remember {
            mutableStateOf("splash")
        }

        var showExitDialog by remember {
            mutableStateOf(false)
        }

        // =====================================================
        // ANDROID BACK BUTTON
        // =====================================================

        BackHandler(
            enabled = currentScreen != "splash" && currentScreen != "dashboard"
        ) {

            when (currentScreen) {

                "home" -> {
                    showExitDialog = true
                }

                "login" -> {
                    currentScreen = "home"
                }

                "register" -> {
                    currentScreen = "home"
                }

                "dashboard" -> {
                    showExitDialog = true
                }
            }
        }

        // =====================================================
        // EXIT CONFIRMATION
        // =====================================================

        if (showExitDialog) {

            AlertDialog(

                onDismissRequest = {
                    showExitDialog = false
                },

                title = {
                    Text(
                        text = "Exit Biker Hub?",
                        fontWeight = FontWeight.Bold
                    )
                },

                text = {
                    Text(
                        text = "Are you sure you want to exit the app?"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            showExitDialog = false
                            finish()
                        }
                    ) {

                        Text(
                            text = "EXIT",
                            color = Color(0xFFFF4D00),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showExitDialog = false
                        }
                    ) {

                        Text(
                            text = "CANCEL",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

        // =====================================================
        // SCREEN NAVIGATION
        // =====================================================

        when (currentScreen) {

            // =====================================================
            // SPLASH
            // =====================================================

            "splash" -> {

                SplashScreen(

                    onSplashFinished = {

                        currentScreen =
                            if (auth.currentUser != null) {
                                "dashboard"
                            } else {
                                "home"
                            }
                    }
                )
            }

            // =====================================================
            // HOME
            // =====================================================

            "home" -> {

                HomeScreen(

                    onLogin = {
                        currentScreen = "login"
                    },

                    onRegister = {
                        currentScreen = "register"
                    }
                )
            }

            // =====================================================
            // LOGIN
            // =====================================================

            "login" -> {

                LoginScreen(

                    onLoginClick = { email, password ->

                        if (
                            email.isBlank() ||
                            password.isBlank()
                        ) {

                            showToast(
                                "Please enter email and password"
                            )

                            return@LoginScreen
                        }

                        auth
                            .signInWithEmailAndPassword(
                                email.trim(),
                                password
                            )
                            .addOnSuccessListener {

                                showToast(
                                    "Login successful!"
                                )

                                currentScreen =
                                    "dashboard"
                            }
                            .addOnFailureListener { exception ->

                                showToast(
                                    "Login failed: ${
                                        exception.message
                                    }"
                                )
                            }
                    },

                    onRegisterClick = {

                        currentScreen =
                            "register"
                    }
                )
            }

            // =====================================================
            // REGISTER
            // =====================================================

            "register" -> {

                RegisterScreen(

                    onRegisterClick = {
                            name,
                            email,
                            password,
                            confirmPassword ->

                        when {

                            name.isBlank() ||
                                    email.isBlank() ||
                                    password.isBlank() ||
                                    confirmPassword.isBlank() -> {

                                showToast(
                                    "Please fill all fields"
                                )
                            }

                            password != confirmPassword -> {

                                showToast(
                                    "Passwords do not match"
                                )
                            }

                            password.length < 6 -> {

                                showToast(
                                    "Password must be at least 6 characters"
                                )
                            }

                            else -> {

                                registerUser(
                                    name = name,
                                    email = email,
                                    password = password
                                ) { success, message ->

                                    showToast(message)

                                    if (success) {

                                        currentScreen =
                                            "login"
                                    }
                                }
                            }
                        }
                    },

                    onLoginClick = {

                        currentScreen =
                            "login"
                    }
                )
            }

            // =====================================================
            // DASHBOARD
            // =====================================================

            "dashboard" -> {

                DashboardScreen(

                    onLogout = {

                        auth.signOut()

                        currentScreen =
                            "home"

                        showToast(
                            "Logged out successfully"
                        )
                    },

                    onExitRequest = {
                        showExitDialog = true
                    }
                )
            }
        }
    }

    // =============================================================
    // REGISTER USER
    // =============================================================

    private fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {

        auth
            .createUserWithEmailAndPassword(
                email.trim(),
                password
            )
            .addOnSuccessListener { result ->

                val user = result.user

                if (user == null) {

                    onResult(
                        false,
                        "Account creation failed"
                    )

                    return@addOnSuccessListener
                }

                val userData =
                    hashMapOf<String, Any>(

                        "uid" to user.uid,

                        "name" to name.trim(),

                        "email" to email.trim(),

                        "mobile" to "",

                        "city" to "",

                        "bike" to "",

                        "instagram" to "",

                        "bio" to "",

                        "photoUrl" to "",

                        "showMobile" to false,

                        "createdAt" to
                                FieldValue.serverTimestamp()
                    )

                firestore
                    .collection("users")
                    .document(user.uid)
                    .set(userData)
                    .addOnSuccessListener {

                        onResult(
                            true,
                            "Account created successfully!"
                        )
                    }
                    .addOnFailureListener { exception ->

                        onResult(
                            false,
                            "Account created, but profile save failed: ${
                                exception.message
                            }"
                        )
                    }
            }
            .addOnFailureListener { exception ->

                onResult(
                    false,
                    "Registration failed: ${
                        exception.message
                    }"
                )
            }
    }

    // =============================================================
    // TOAST
    // =============================================================

    private fun showToast(message: String) {

        Toast
            .makeText(
                this,
                message,
                Toast.LENGTH_LONG
            )
            .show()
    }
}


// =================================================================
// SPLASH SCREEN
// =================================================================

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {

    var progress by remember {
        mutableStateOf(0f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 2000
        ),
        label = "splash_progress"
    )

    LaunchedEffect(Unit) {

        progress = 1f

        delay(2000)

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF0B0D10)
            )
    ) {

        Image(
            painter = painterResource(
                id = R.drawable.splash_bg
            ),
            contentDescription = "Splash Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.60f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 24.dp,
                    vertical = 48.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(80.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "BIKER ",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "HUB",
                    color = Color(0xFFFF5A00),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "RIDE • CONNECT • BELONG",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Community for real riders",
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            LinearProgressIndicator(
                progress = {
                    animatedProgress
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(6.dp)
                    .clip(
                        RoundedCornerShape(3.dp)
                    ),
                color = Color(0xFFFF5A00),
                trackColor = Color(0xFF30343A)
            )
        }
    }
}


// =================================================================
// HOME SCREEN
// =================================================================

@Composable
fun HomeScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {

    val backgroundBrush =
        Brush.verticalGradient(

            colors = listOf(
                Color(0xFF08090C),
                Color(0xFF11141A),
                Color(0xFF08090C)
            )
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                backgroundBrush
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp)
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .height(230.dp)
                    .fillMaxWidth()
                    .background(

                        Brush.radialGradient(

                            colors = listOf(
                                Color(0x55FF4D00),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 24.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "BIKER",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = " HUB",
                    color = Color(0xFFFF4D00),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "● ONLINE",
                    color = Color(0xFF69F58C),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(35.dp)
            )

            Box(
                modifier = Modifier
                    .height(190.dp)
                    .fillMaxWidth(),
                contentAlignment =
                    Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .height(175.dp)
                        .fillMaxWidth(0.72f)
                        .background(

                            Brush.radialGradient(

                                colors = listOf(
                                    Color(0x44FF4D00),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = "🏍️",
                    fontSize = 82.sp
                )
            }

            Text(
                text = "BIKER HUB",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )

            Text(
                text = "RIDE • CONNECT • BELONG",
                color = Color(0xFFFF5A00),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF171A20),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "RIDERS",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "∞",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "RIDES",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "24/7",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "ADVENTURE",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "∞",
                            color = Color(0xFFFF5A00),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFFFF4D00)
                    )
            ) {

                Text(
                    text = "LOGIN",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF20242B)
                    )
            ) {

                Text(
                    text = "CREATE ACCOUNT",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text =
                    "FIND RIDERS  •  CREATE RIDES  •  EXPLORE",
                color = Color(0xFF666B73),
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
