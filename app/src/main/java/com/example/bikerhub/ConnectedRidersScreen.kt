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
import com.google.firebase.firestore.FirebaseFirestore

private val ConnectedBackground = Color(0xFF0B0D10)
private val ConnectedCard = Color(0xFF171A1F)
private val ConnectedOrange = Color(0xFFFF6B00)
private val ConnectedWhite = Color(0xFFF5F5F5)
private val ConnectedGray = Color(0xFF8D9299)

data class ConnectedRiderData(
    val uid: String = "",
    val name: String = "Rider",
    val email: String = "",
    val city: String = "",
    val bike: String = "",
    val instagram: String = "",
    val bio: String = "",
    val mobile: String = "",
    val showMobile: Boolean = false
)

@Composable
fun ConnectedRidersScreen(
    onBack: () -> Unit,
    onMessage: (String, String) -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val currentUid =
        auth.currentUser?.uid

    var riders by remember {
        mutableStateOf<List<ConnectedRiderData>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    // ==================================================
    // LOAD CONNECTIONS
    // ==================================================

    DisposableEffect(currentUid) {

        if (currentUid == null) {

            loading = false

            onDispose { }

        } else {

            val listener =
                db.collection("connections")
                    .whereEqualTo(
                        "userId",
                        currentUid
                    )
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {

                            loading = false

                            errorMessage =
                                error.message
                                    ?: "Unable to load connections."

                            return@addSnapshotListener
                        }

                        val connectionIds =
                            snapshot
                                ?.documents
                                ?.mapNotNull {
                                    it.getString(
                                        "connectedUserId"
                                    )
                                }
                                ?.distinct()
                                .orEmpty()

                        if (connectionIds.isEmpty()) {

                            riders = emptyList()

                            loading = false

                            return@addSnapshotListener
                        }

                        val loadedRiders =
                            mutableListOf<ConnectedRiderData>()

                        var completed = 0

                        connectionIds.forEach { riderId ->

                            db.collection("users")
                                .document(riderId)
                                .get()
                                .addOnSuccessListener { document ->

                                    if (document.exists()) {

                                        loadedRiders.add(

                                            ConnectedRiderData(

                                                uid = riderId,

                                                name =
                                                    document
                                                        .getString("name")
                                                        ?.takeIf {
                                                            it.isNotBlank()
                                                        }
                                                        ?: "Rider",

                                                email =
                                                    document
                                                        .getString("email")
                                                        .orEmpty(),

                                                city =
                                                    document
                                                        .getString("city")
                                                        .orEmpty(),

                                                bike =
                                                    document
                                                        .getString("bike")
                                                        .orEmpty(),

                                                instagram =
                                                    document
                                                        .getString("instagram")
                                                        .orEmpty(),

                                                bio =
                                                    document
                                                        .getString("bio")
                                                        .orEmpty(),

                                                mobile =
                                                    document
                                                        .getString("mobile")
                                                        .orEmpty(),

                                                showMobile =
                                                    document
                                                        .getBoolean(
                                                            "showMobile"
                                                        )
                                                        ?: false
                                            )
                                        )
                                    }

                                    completed++

                                    if (
                                        completed ==
                                        connectionIds.size
                                    ) {

                                        riders =
                                            loadedRiders
                                                .sortedBy {
                                                    it.name.lowercase()
                                                }

                                        loading = false
                                    }
                                }
                                .addOnFailureListener {

                                    completed++

                                    if (
                                        completed ==
                                        connectionIds.size
                                    ) {

                                        riders =
                                            loadedRiders
                                                .sortedBy {
                                                    it.name.lowercase()
                                                }

                                        loading = false
                                    }
                                }
                        }
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
            .background(
                ConnectedBackground
            )
            .padding(18.dp)
    ) {

        // ==================================================
        // HEADER
        // ==================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← BACK",
                    color = ConnectedOrange,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Column {

                Text(
                    text = "Connected Riders",
                    color = ConnectedWhite,
                    fontSize = 24.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "${riders.size} connection" +
                                if (riders.size == 1) {
                                    ""
                                } else {
                                    "s"
                                },
                    color = ConnectedGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        // ==================================================
        // ERROR
        // ==================================================

        if (errorMessage.isNotBlank()) {

            Text(
                text = errorMessage,
                color = Color(0xFFFF453A),
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
                text = "Loading connected riders...",
                color = ConnectedGray,
                fontSize = 13.sp
            )
        }

        // ==================================================
        // EMPTY
        // ==================================================

        if (
            !loading &&
            riders.isEmpty()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "👥",
                    fontSize = 42.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "No connected riders yet",
                    color = ConnectedWhite,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text =
                        "Connect with riders from\n" +
                                "Explore Riders.",
                    color = ConnectedGray,
                    fontSize = 12.sp
                )
            }
        }

        // ==================================================
        // RIDER LIST
        // ==================================================

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            items(
                items = riders,
                key = {
                    it.uid
                }
            ) { rider ->

                ConnectedRiderCard(
                    rider = rider,

                    onMessage = {

                        onMessage(
                            rider.uid,
                            rider.name
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }
    }
}


// ======================================================
// RIDER CARD
// ======================================================

@Composable
private fun ConnectedRiderCard(
    rider: ConnectedRiderData,
    onMessage: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                ConnectedCard,
                RoundedCornerShape(16.dp)
            )
            .padding(15.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // ==================================================
            // AVATAR
            // ==================================================

            Column(
                modifier = Modifier
                    .size(55.dp)
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
                        rider.name
                            .firstOrNull()
                            ?.uppercase()
                            ?: "R",
                    color = ConnectedOrange,
                    fontSize = 21.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            // ==================================================
            // RIDER INFO
            // ==================================================

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = rider.name,
                    color = ConnectedWhite,
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                val locationBike =
                    listOf(
                        rider.city,
                        rider.bike
                    )
                        .filter {
                            it.isNotBlank()
                        }
                        .joinToString(
                            " • "
                        )

                Text(
                    text =
                        locationBike
                            .ifBlank {
                                "Biker"
                            },
                    color = ConnectedGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // ==================================================
        // EXTRA INFO
        // ==================================================

        if (
            rider.instagram.isNotBlank()
        ) {

            Text(
                text =
                    "Instagram: @${rider.instagram.removePrefix("@")}",
                color = ConnectedGray,
                fontSize = 11.sp
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )
        }

        if (
            rider.showMobile &&
            rider.mobile.isNotBlank()
        ) {

            Text(
                text =
                    "Mobile: ${rider.mobile}",
                color = ConnectedGray,
                fontSize = 11.sp
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )
        }

        if (
            rider.bio.isNotBlank()
        ) {

            Text(
                text = rider.bio,
                color = ConnectedGray,
                fontSize = 11.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        // ==================================================
        // MESSAGE BUTTON
        // ==================================================

        Button(
            onClick = onMessage,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    ConnectedOrange
            ),
            shape =
                RoundedCornerShape(10.dp)
        ) {

            Text(
                text = "💬  MESSAGE",
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}