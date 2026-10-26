package com.example.bikerhub

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// ======================================================
// COLORS
// ======================================================

private val RidesDarkBackground = Color(0xFF0B0D10)
private val RidesCardColor = Color(0xFF171A1F)
private val RidesOrange = Color(0xFFFF6B00)
private val RidesLightText = Color(0xFFF5F5F5)
private val RidesGrayText = Color(0xFF8D9299)
private val RidesGreen = Color(0xFF35C759)


// ======================================================
// DATA MODEL
// ======================================================

data class RideData(
    val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val rideName: String = "",
    val startPoint: String = "",
    val destination: String = "",
    val date: String = "",
    val time: String = "",
    val duration: String = "",
    val maxRiders: String = "",
    val description: String = "",
    val joinedRiders: Long = 1L
)


// ======================================================
// RIDE REQUEST STATUS
// ======================================================

data class RideRequestStatus(
    val rideId: String = "",
    val status: String = ""
)


// ======================================================
// RIDES SCREEN
// ======================================================

@Composable
fun RidesScreen() {

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var rides by remember {
        mutableStateOf<List<RideData>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var selectedRide by remember {
        mutableStateOf<RideData?>(null)
    }

    // --------------------------------------------------
    // FIRESTORE REAL-TIME LISTENER
    // --------------------------------------------------

    DisposableEffect(Unit) {

        val listener: ListenerRegistration =
            db.collection("rides")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        loading = false

                        errorMessage =
                            error.message ?: "Unable to load rides"

                        return@addSnapshotListener
                    }

                    if (snapshot != null) {

                        rides = snapshot.documents.mapNotNull { document ->

                            try {

                                RideData(
                                    id = document.id,

                                    creatorId =
                                        document.getString("creatorId")
                                            ?: "",

                                    creatorName =
                                        document.getString("creatorName")
                                            ?: "Rider",

                                    rideName =
                                        document.getString("rideName")
                                            ?: "Unnamed Ride",

                                    startPoint =
                                        document.getString("startPoint")
                                            ?: "",

                                    destination =
                                        document.getString("destination")
                                            ?: "",

                                    date =
                                        document.getString("date")
                                            ?: "",

                                    time =
                                        document.getString("time")
                                            ?: "",

                                    duration =
                                        document.getString("duration")
                                            ?: "",

                                    maxRiders =
                                        document.getString("maxRiders")
                                            ?: "",

                                    description =
                                        document.getString("description")
                                            ?: "",

                                    joinedRiders =
                                        document.getLong("joinedRiders")
                                            ?: 1L
                                )

                            } catch (e: Exception) {

                                null
                            }
                        }

                        loading = false
                        errorMessage = ""
                    }
                }

        onDispose {
            listener.remove()
        }
    }

    // --------------------------------------------------
    // DETAIL SCREEN
    // --------------------------------------------------

    if (selectedRide != null) {

        RideDetailsScreenFirebase(
            ride = selectedRide!!,
            onBack = {
                selectedRide = null
            }
        )

        return
    }

    // --------------------------------------------------
    // MAIN SCREEN
    // --------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RidesDarkBackground)
            .padding(18.dp)
    ) {

        Text(
            text = "Rides",
            color = RidesLightText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Discover rides and ride with your community",
            color = RidesGrayText,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // --------------------------------------------------
        // LOADING
        // --------------------------------------------------

        if (loading) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    color = RidesOrange
                )
            }
        }

        // --------------------------------------------------
        // ERROR
        // --------------------------------------------------

        else if (errorMessage.isNotBlank()) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Unable to load rides",
                    color = RidesLightText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = errorMessage,
                    color = RidesGrayText,
                    fontSize = 12.sp
                )
            }
        }

        // --------------------------------------------------
        // EMPTY
        // --------------------------------------------------

        else if (rides.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🏍️",
                    fontSize = 42.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "No rides yet",
                    color = RidesLightText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Create a ride and it will appear here.",
                    color = RidesGrayText,
                    fontSize = 13.sp
                )
            }
        }

        // --------------------------------------------------
        // RIDE LIST
        // --------------------------------------------------

        else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(
                    items = rides,
                    key = { it.id }
                ) { ride ->

                    RideCardFirebase(
                        ride = ride,
                        onViewRide = {
                            selectedRide = ride
                        }
                    )
                }
            }
        }
    }
}


// ======================================================
// RIDE CARD
// ======================================================

@Composable
fun RideCardFirebase(
    ride: RideData,
    onViewRide: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                RidesCardColor,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {

        Text(
            text = ride.rideName,
            color = RidesLightText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "📍 ${ride.startPoint} → ${ride.destination}",
            color = RidesGrayText,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "📅 ${ride.date}",
            color = RidesGrayText,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "🕐 ${ride.time}",
            color = RidesGrayText,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "👥 ${ride.joinedRiders} Riders",
            color = RidesGrayText,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        if (ride.creatorName.isNotBlank()) {

            Text(
                text = "Created by ${ride.creatorName}",
                color = RidesGrayText,
                fontSize = 11.sp
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onViewRide,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = RidesOrange
            ),
            shape = RoundedCornerShape(10.dp)
        ) {

            Text(
                text = "VIEW RIDE",
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// ======================================================
// RIDE DETAILS
// ======================================================

@Composable
fun RideDetailsScreenFirebase(
    ride: RideData,
    onBack: () -> Unit
) {

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var requestStatus by remember {
        mutableStateOf("none")
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var sendingRequest by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    var showRideRequests by remember {
        mutableStateOf(false)
    }

    // --------------------------------------------------
    // RIDE REQUEST MANAGEMENT
    // --------------------------------------------------

    if (showRideRequests) {
        RideRequestsScreen(
            rideId = ride.id,
            rideName = ride.rideName,
            onBack = {
                showRideRequests = false
            }
        )
        return
    }

    // --------------------------------------------------
    // CHECK EXISTING REQUEST
    // --------------------------------------------------

    DisposableEffect(ride.id) {

        val currentUser = auth.currentUser

        if (currentUser == null) {

            loading = false

            onDispose { }

        } else {

            val listener =
                db.collection("rideRequests")
                    .whereEqualTo(
                        "rideId",
                        ride.id
                    )
                    .whereEqualTo(
                        "riderId",
                        currentUser.uid
                    )
                    .addSnapshotListener { snapshot, error ->

                        loading = false

                        if (error != null) {

                            return@addSnapshotListener
                        }

                        val request =
                            snapshot?.documents?.firstOrNull()

                        requestStatus =
                            request?.getString("status")
                                ?: "none"
                    }

            onDispose {
                listener.remove()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RidesDarkBackground)
            .padding(18.dp)
    ) {

        // --------------------------------------------------
        // HEADER
        // --------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← Back",
                    color = RidesOrange,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            item {

                Text(
                    text = "Ride Details",
                    color = RidesLightText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            RidesCardColor,
                            RoundedCornerShape(18.dp)
                        )
                        .padding(20.dp)
                ) {

                    // --------------------------------------------------
                    // RIDE NAME
                    // --------------------------------------------------

                    Text(
                        text = ride.rideName,
                        color = RidesLightText,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // --------------------------------------------------
                    // ROUTE
                    // --------------------------------------------------

                    RideDetailRow(
                        title = "Starting Point",
                        value = ride.startPoint
                    )

                    RideDetailRow(
                        title = "Destination",
                        value = ride.destination
                    )

                    RideDetailRow(
                        title = "Date",
                        value = ride.date
                    )

                    RideDetailRow(
                        title = "Time",
                        value = ride.time
                    )

                    RideDetailRow(
                        title = "Duration",
                        value = ride.duration
                    )

                    RideDetailRow(
                        title = "Maximum Riders",
                        value = ride.maxRiders
                    )

                    RideDetailRow(
                        title = "Joined Riders",
                        value = ride.joinedRiders.toString()
                    )

                    RideDetailRow(
                        title = "Created By",
                        value = ride.creatorName
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // --------------------------------------------------
                    // DESCRIPTION
                    // --------------------------------------------------

                    if (ride.description.isNotBlank()) {

                        Text(
                            text = "Description",
                            color = RidesLightText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = ride.description,
                            color = RidesGrayText,
                            fontSize = 13.sp
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )
                    }

                    // --------------------------------------------------
                    // REQUEST STATUS
                    // --------------------------------------------------

                    if (requestStatus == "pending") {

                        Text(
                            text = "✓ Join request sent",
                            color = RidesOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }

                    if (requestStatus == "accepted") {

                        Text(
                            text = "✓ You are accepted for this ride",
                            color = RidesGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }

                    if (requestStatus == "rejected") {

                        Text(
                            text = "Join request was rejected",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }

                    // --------------------------------------------------
                    // JOIN BUTTON
                    // --------------------------------------------------

                    if (
                        ride.creatorId ==
                        auth.currentUser?.uid
                    ) {

                        Text(
                            text = "You created this ride",
                            color = RidesGrayText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                showRideRequests = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RidesOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "MANAGE JOIN REQUESTS",
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {

                        Button(
                            onClick = {

                                sendRideJoinRequest(
                                    ride = ride,
                                    onSending = {
                                        sendingRequest = true
                                        message = ""
                                    },
                                    onSuccess = {
                                        sendingRequest = false
                                        requestStatus = "pending"
                                        message =
                                            "Join request sent successfully"
                                    },
                                    onError = { error ->

                                        sendingRequest = false
                                        message = error
                                    }
                                )
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                            enabled =
                                !sendingRequest &&
                                        requestStatus != "pending" &&
                                        requestStatus != "accepted",

                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (
                                        requestStatus == "accepted"
                                    ) {
                                        RidesGreen
                                    } else {
                                        RidesOrange
                                    }
                            ),

                            shape = RoundedCornerShape(12.dp)
                        ) {

                            Text(
                                text = when {

                                    sendingRequest ->
                                        "SENDING..."

                                    requestStatus == "pending" ->
                                        "REQUEST SENT"

                                    requestStatus == "accepted" ->
                                        "ACCEPTED"

                                    else ->
                                        "JOIN RIDE"
                                },

                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // --------------------------------------------------
                    // MESSAGE
                    // --------------------------------------------------

                    if (message.isNotBlank()) {

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text = message,
                            color = RidesGrayText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(25.dp)
                )

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RidesCardColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = "BACK TO RIDES",
                        color = RidesLightText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}


// ======================================================
// DETAIL ROW
// ======================================================

@Composable
fun RideDetailRow(
    title: String,
    value: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 13.dp)
    ) {

        Text(
            text = title,
            color = RidesGrayText,
            fontSize = 11.sp
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = if (value.isBlank()) "-" else value,
            color = RidesLightText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// ======================================================
// SEND JOIN REQUEST
// ======================================================

private fun sendRideJoinRequest(
    ride: RideData,
    onSending: () -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    if (currentUser == null) {

        onError("Please login first")
        return
    }

    onSending()

    // --------------------------------------------------
    // CHECK DUPLICATE REQUEST
    // --------------------------------------------------

    db.collection("rideRequests")
        .whereEqualTo(
            "rideId",
            ride.id
        )
        .whereEqualTo(
            "riderId",
            currentUser.uid
        )
        .get()
        .addOnSuccessListener { snapshot ->

            if (!snapshot.isEmpty) {

                val existing =
                    snapshot.documents.firstOrNull()

                val existingStatus =
                    existing?.getString("status")
                        ?: "pending"

                when (existingStatus) {

                    "accepted" -> {
                        onError(
                            "You are already accepted for this ride"
                        )
                    }

                    "pending" -> {
                        onError(
                            "Join request already sent"
                        )
                    }

                    else -> {

                        onError(
                            "A request already exists for this ride"
                        )
                    }
                }

                return@addOnSuccessListener
            }

            // --------------------------------------------------
            // GET USER PROFILE NAME
            // --------------------------------------------------

            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { userDocument ->

                    val riderName =
                        userDocument.getString("name")
                            ?.takeIf { it.isNotBlank() }
                            ?: currentUser.displayName
                            ?: "Rider"

                    val requestData =
                        hashMapOf(

                            "rideId" to ride.id,

                            "rideName" to ride.rideName,

                            "creatorId" to ride.creatorId,

                            "creatorName" to ride.creatorName,

                            "riderId" to currentUser.uid,

                            "riderName" to riderName,

                            "riderEmail" to (
                                    currentUser.email ?: ""
                                    ),

                            "status" to "pending",

                            "createdAt" to
                                    FieldValue
                                        .serverTimestamp()
                        )

                    db.collection("rideRequests")
                        .add(requestData)
                        .addOnSuccessListener {

                            onSuccess()
                        }
                        .addOnFailureListener { error ->

                            onError(
                                error.message
                                    ?: "Failed to send request"
                            )
                        }
                }
                .addOnFailureListener { error ->

                    onError(
                        error.message
                            ?: "Unable to load profile"
                    )
                }
        }
        .addOnFailureListener { error ->

            onError(
                error.message
                    ?: "Unable to check existing request"
            )
        }
}