package com.example.bikerhub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private val RequestBackground = Color(0xFF0B0D10)
private val RequestCard = Color(0xFF171A1F)
private val RequestOrange = Color(0xFFFF6B00)
private val RequestWhite = Color(0xFFF5F5F5)
private val RequestGray = Color(0xFF8D9299)
private val RequestGreen = Color(0xFF35C759)
private val RequestRed = Color(0xFFFF453A)

data class RideJoinRequest(
    val id: String = "",
    val rideId: String = "",
    val rideName: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val riderId: String = "",
    val riderName: String = "Rider",
    val riderEmail: String = "",
    val status: String = "pending"
)

@Composable
fun RideRequestsScreen(
    rideId: String,
    rideName: String,
    onBack: () -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val currentUid = auth.currentUser?.uid

    var requests by remember {
        mutableStateOf<List<RideJoinRequest>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var message by remember {
        mutableStateOf("")
    }

    // ==================================================
    // LOAD REQUESTS
    // ==================================================

    DisposableEffect(rideId, currentUid) {

        if (currentUid == null) {

            loading = false

            onDispose { }

        } else {

            val listener =
                db.collection("rideRequests")
                    .whereEqualTo(
                        "rideId",
                        rideId
                    )
                    .whereEqualTo(
                        "creatorId",
                        currentUid
                    )
                    .addSnapshotListener { snapshot, error ->

                        loading = false

                        if (error != null) {

                            message =
                                error.message
                                    ?: "Unable to load requests."

                            requests = emptyList()

                            return@addSnapshotListener
                        }

                        requests =
                            snapshot
                                ?.documents
                                ?.mapNotNull { document ->

                                    val status =
                                        document
                                            .getString("status")
                                            ?: "pending"

                                    if (status != "pending") {
                                        null
                                    } else {

                                        RideJoinRequest(

                                            id = document.id,

                                            rideId =
                                                document
                                                    .getString("rideId")
                                                    .orEmpty(),

                                            rideName =
                                                document
                                                    .getString("rideName")
                                                    ?: rideName,

                                            creatorId =
                                                document
                                                    .getString("creatorId")
                                                    .orEmpty(),

                                            creatorName =
                                                document
                                                    .getString("creatorName")
                                                    ?: "Rider",

                                            riderId =
                                                document
                                                    .getString("riderId")
                                                    .orEmpty(),

                                            riderName =
                                                document
                                                    .getString("riderName")
                                                    ?: "Rider",

                                            riderEmail =
                                                document
                                                    .getString("riderEmail")
                                                    .orEmpty(),

                                            status = status
                                        )
                                    }
                                }
                                .orEmpty()
                    }

            onDispose {
                listener.remove()
            }
        }
    }

    // ==================================================
    // UI
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RequestBackground)
            .padding(18.dp)
    ) {

        // ==================================================
        // HEADER
        // ==================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← BACK",
                    color = RequestOrange,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Column {

                Text(
                    text = "Ride Requests",
                    color = RequestWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = rideName,
                    color = RequestGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        // ==================================================
        // MESSAGE
        // ==================================================

        if (message.isNotBlank()) {

            Text(
                text = message,
                color = RequestGray,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        // ==================================================
        // LOADING
        // ==================================================

        if (loading) {

            Text(
                text = "Loading requests...",
                color = RequestGray,
                fontSize = 13.sp
            )
        }

        // ==================================================
        // EMPTY
        // ==================================================

        if (!loading && requests.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 35.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "📭",
                    fontSize = 42.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "No pending requests",
                    color = RequestWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text =
                        "When riders request to join this ride,\n" +
                                "their requests will appear here.",
                    color = RequestGray,
                    fontSize = 12.sp
                )
            }
        }

        // ==================================================
        // REQUEST LIST
        // ==================================================

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            items(
                items = requests,
                key = {
                    it.id
                }
            ) { request ->

                RideRequestCard(
                    request = request,

                    onAccept = {

                        acceptRideRequest(
                            request = request,
                            db = db,
                            onSuccess = {
                                message =
                                    "Request accepted ✓"
                            },
                            onError = {
                                message = it
                            }
                        )
                    },

                    onReject = {

                        rejectRideRequest(
                            request = request,
                            db = db,
                            onSuccess = {
                                message =
                                    "Request rejected."
                            },
                            onError = {
                                message = it
                            }
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }
        }
    }
}


// ======================================================
// REQUEST CARD
// ======================================================

@Composable
private fun RideRequestCard(
    request: RideJoinRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                RequestCard,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // Avatar
            Column(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF2B3036)
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text =
                        request.riderName
                            .firstOrNull()
                            ?.uppercase()
                            ?: "R",
                    color = RequestOrange,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = request.riderName,
                    color = RequestWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                if (request.riderEmail.isNotBlank()) {

                    Text(
                        text = request.riderEmail,
                        color = RequestGray,
                        fontSize = 11.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "wants to join this ride",
                    color = RequestGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            // REJECT
            Button(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF2A2E34)
                ),
                shape =
                    RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = "REJECT",
                    color = RequestWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ACCEPT
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        RequestGreen
                ),
                shape =
                    RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = "ACCEPT",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ======================================================
// ACCEPT RIDE REQUEST
// ======================================================

private fun acceptRideRequest(
    request: RideJoinRequest,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    if (request.id.isBlank()) {

        onError(
            "Invalid request."
        )

        return
    }

    // ==================================================
    // UPDATE REQUEST STATUS
    // ==================================================

    db.collection("rideRequests")
        .document(request.id)
        .update(
            mapOf(
                "status" to "accepted",
                "respondedAt" to
                        FieldValue.serverTimestamp()
            )
        )
        .addOnSuccessListener {

            // ==================================================
            // ADD MEMBER
            // ==================================================

            val memberData =
                hashMapOf<String, Any>(

                    "userId" to request.riderId,

                    "name" to request.riderName,

                    "email" to request.riderEmail,

                    "joinedAt" to
                            FieldValue.serverTimestamp()
                )

            db.collection("rides")
                .document(request.rideId)
                .collection("members")
                .document(request.riderId)
                .set(memberData)
                .addOnSuccessListener {

                    // ==================================================
                    // UPDATE RIDER COUNT
                    // ==================================================

                    db.collection("rides")
                        .document(request.rideId)
                        .update(
                            "joinedRiders",
                            FieldValue.increment(1)
                        )
                        .addOnSuccessListener {

                            onSuccess()
                        }
                        .addOnFailureListener { error ->

                            onError(
                                error.message
                                    ?: "Rider count update failed."
                            )
                        }
                }
                .addOnFailureListener { error ->

                    onError(
                        error.message
                            ?: "Could not add rider."
                    )
                }
        }
        .addOnFailureListener { error ->

            onError(
                error.message
                    ?: "Could not accept request."
            )
        }
}


// ======================================================
// REJECT RIDE REQUEST
// ======================================================

private fun rejectRideRequest(
    request: RideJoinRequest,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    if (request.id.isBlank()) {

        onError(
            "Invalid request."
        )

        return
    }

    db.collection("rideRequests")
        .document(request.id)
        .update(
            mapOf(
                "status" to "rejected",
                "respondedAt" to
                        FieldValue.serverTimestamp()
            )
        )
        .addOnSuccessListener {

            onSuccess()
        }
        .addOnFailureListener { error ->

            onError(
                error.message
                    ?: "Could not reject request."
            )
        }
}