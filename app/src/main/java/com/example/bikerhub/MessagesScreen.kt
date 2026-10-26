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

private val MessagesBackground = Color(0xFF0B0D10)
private val MessagesCard = Color(0xFF171A1F)
private val MessagesOrange = Color(0xFFFF6B00)
private val MessagesWhite = Color(0xFFF5F5F5)
private val MessagesGray = Color(0xFF8D9299)
private val MessagesGreen = Color(0xFF35C759)
private val MessagesRed = Color(0xFFFF453A)

data class MessageConnectionRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "Rider",
    val receiverId: String = "",
    val status: String = "pending"
)

data class MessageRideRequest(
    val id: String = "",
    val rideId: String = "",
    val rideName: String = "Ride",
    val creatorId: String = "",
    val creatorName: String = "Rider",
    val riderId: String = "",
    val riderName: String = "Rider",
    val status: String = "pending"
)

@Composable
fun MessagesScreen(
    onOpenConnectedRiders: () -> Unit = {}
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val currentUid = auth.currentUser?.uid

    var connectionRequests by remember {
        mutableStateOf<List<MessageConnectionRequest>>(emptyList())
    }

    var rideRequests by remember {
        mutableStateOf<List<MessageRideRequest>>(emptyList())
    }

    var message by remember {
        mutableStateOf("")
    }

    // ==================================================
    // CONNECTION REQUESTS
    // ==================================================

    DisposableEffect(currentUid) {

        if (currentUid == null) {

            onDispose { }

        } else {

            val listener =
                db.collection("connectionRequests")
                    .whereEqualTo(
                        "receiverId",
                        currentUid
                    )
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {
                            return@addSnapshotListener
                        }

                        connectionRequests =
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

                                        MessageConnectionRequest(

                                            id = document.id,

                                            senderId =
                                                document
                                                    .getString("senderId")
                                                    .orEmpty(),

                                            senderName =
                                                document
                                                    .getString("senderName")
                                                    ?: "Rider",

                                            receiverId =
                                                document
                                                    .getString("receiverId")
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
    // RIDE REQUESTS
    // ==================================================

    DisposableEffect(currentUid) {

        if (currentUid == null) {

            onDispose { }

        } else {

            val listener =
                db.collection("rideRequests")
                    .whereEqualTo(
                        "creatorId",
                        currentUid
                    )
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {
                            return@addSnapshotListener
                        }

                        rideRequests =
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

                                        MessageRideRequest(

                                            id = document.id,

                                            rideId =
                                                document
                                                    .getString("rideId")
                                                    .orEmpty(),

                                            rideName =
                                                document
                                                    .getString("rideName")
                                                    ?: "Ride",

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
    // MAIN UI
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MessagesBackground)
            .padding(18.dp)
    ) {

        // ==================================================
        // HEADER
        // ==================================================

        Text(
            text = "Messages",
            color = MessagesWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "Connect with your biker community",
            color = MessagesGray,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // ==================================================
        // CONNECTED RIDERS
        // ==================================================

        Button(
            onClick = onOpenConnectedRiders,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MessagesOrange
            ),
            shape = RoundedCornerShape(12.dp)
        ) {

            Text(
                text = "👥  CONNECTED RIDERS",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        if (message.isNotBlank()) {

            Text(
                text = message,
                color = MessagesGray,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            // ==================================================
            // CONNECTION REQUESTS TITLE
            // ==================================================

            if (connectionRequests.isNotEmpty()) {

                item {

                    Text(
                        text = "CONNECTION REQUESTS",
                        color = MessagesWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                items(
                    items = connectionRequests,
                    key = {
                        it.id
                    }
                ) { request ->

                    ConnectionRequestCard(
                        request = request,

                        onAccept = {

                            acceptConnectionRequest(
                                request = request,
                                db = db,
                                onSuccess = {
                                    message =
                                        "Connection accepted ✓"
                                },
                                onError = {
                                    message = it
                                }
                            )
                        },

                        onReject = {

                            rejectConnectionRequest(
                                request = request,
                                db = db,
                                onSuccess = {
                                    message =
                                        "Connection request rejected."
                                },
                                onError = {
                                    message = it
                                }
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            // ==================================================
            // RIDE REQUESTS TITLE
            // ==================================================

            if (rideRequests.isNotEmpty()) {

                item {

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "RIDE JOIN REQUESTS",
                        color = MessagesWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                items(
                    items = rideRequests,
                    key = {
                        it.id
                    }
                ) { request ->

                    RideRequestMessageCard(
                        request = request,

                        onAccept = {

                            acceptRideRequest(
                                request = request,
                                db = db,
                                onSuccess = {
                                    message =
                                        "Ride request accepted ✓"
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
                                        "Ride request rejected."
                                },
                                onError = {
                                    message = it
                                }
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            // ==================================================
            // EMPTY STATE
            // ==================================================

            if (
                connectionRequests.isEmpty() &&
                rideRequests.isEmpty()
            ) {

                item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 45.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "💬",
                            fontSize = 42.sp
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(
                            text = "No new requests",
                            color = MessagesWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                "Your connection and ride requests\n" +
                                        "will appear here.",
                            color = MessagesGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}


// ======================================================
// CONNECTION REQUEST CARD
// ======================================================

@Composable
private fun ConnectionRequestCard(
    request: MessageConnectionRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MessagesCard,
                RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Avatar(
                name = request.senderName
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = request.senderName,
                    color = MessagesWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "wants to connect with you",
                    color = MessagesGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        MessagesGreen
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
// RIDE REQUEST CARD
// ======================================================

@Composable
private fun RideRequestMessageCard(
    request: MessageRideRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MessagesCard,
                RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Avatar(
                name = request.riderName
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = request.riderName,
                    color = MessagesWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "wants to join ${request.rideName}",
                    color = MessagesGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        MessagesGreen
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
// AVATAR
// ======================================================

@Composable
private fun Avatar(
    name: String
) {

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
                name
                    .firstOrNull()
                    ?.uppercase()
                    ?: "R",
            color = MessagesOrange,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ======================================================
// ACCEPT CONNECTION
// ======================================================

private fun acceptConnectionRequest(
    request: MessageConnectionRequest,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    val currentUid =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid

    if (currentUid == null) {

        onError("Please login again.")

        return
    }

    if (request.senderId.isBlank()) {

        onError("Invalid rider.")

        return
    }

    db.collection("connectionRequests")
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
            // CREATE CONNECTION FOR RECEIVER
            // ==================================================

            val receiverConnection =
                hashMapOf(

                    "userId" to currentUid,

                    "connectedUserId" to
                            request.senderId,

                    "createdAt" to
                            FieldValue.serverTimestamp()
                )

            // ==================================================
            // CREATE CONNECTION FOR SENDER
            // ==================================================

            val senderConnection =
                hashMapOf(

                    "userId" to request.senderId,

                    "connectedUserId" to
                            currentUid,

                    "createdAt" to
                            FieldValue.serverTimestamp()
                )

            val batch =
                db.batch()

            val receiverDoc =
                db.collection("connections")
                    .document()

            val senderDoc =
                db.collection("connections")
                    .document()

            batch.set(
                receiverDoc,
                receiverConnection
            )

            batch.set(
                senderDoc,
                senderConnection
            )

            batch.commit()
                .addOnSuccessListener {

                    onSuccess()
                }
                .addOnFailureListener { error ->

                    onError(
                        error.message
                            ?: "Could not create connection."
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
// REJECT CONNECTION
// ======================================================

private fun rejectConnectionRequest(
    request: MessageConnectionRequest,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    db.collection("connectionRequests")
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


// ======================================================
// ACCEPT RIDE REQUEST
// ======================================================

private fun acceptRideRequest(
    request: MessageRideRequest,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    if (request.id.isBlank()) {

        onError("Invalid ride request.")

        return
    }

    if (request.riderId.isBlank()) {

        onError("Invalid rider.")

        return
    }

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
            // ADD RIDER TO RIDE MEMBERS
            // ==================================================

            val memberData =
                hashMapOf<String, Any>(

                    "userId" to request.riderId,

                    "name" to request.riderName,

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
                    // INCREMENT RIDER COUNT
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
                                    ?: "Could not update rider count."
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
                    ?: "Could not accept ride request."
            )
        }
}


// ======================================================
// REJECT RIDE REQUEST
// ======================================================

private fun rejectRideRequest(
    request: MessageRideRequest,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

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
                    ?: "Could not reject ride request."
            )
        }
}