package com.example.bikerhub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private val NotificationsBackground = Color(0xFF0B0D10)
private val NotificationsCard = Color(0xFF171A1F)
private val NotificationsOrange = Color(0xFFFF6B00)
private val NotificationsWhite = Color(0xFFF5F5F5)
private val NotificationsGray = Color(0xFF8D9299)

private data class AppNotification(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val status: String
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUid = auth.currentUser?.uid

    var notifications by remember {
        mutableStateOf<List<AppNotification>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    DisposableEffect(currentUid) {
        if (currentUid == null) {
            loading = false
            onDispose { }
        } else {
            var connectionNotifications = emptyList<AppNotification>()
            var rideNotifications = emptyList<AppNotification>()

            fun refresh() {
                notifications =
                    (connectionNotifications + rideNotifications)
                        .distinctBy { "${it.type}_${it.id}" }
                loading = false
            }

            val connectionListener: ListenerRegistration =
                db.collection("connectionRequests")
                    .whereEqualTo("receiverId", currentUid)
                    .addSnapshotListener { snapshot, _ ->
                        connectionNotifications = snapshot?.documents?.map { document ->
                            val senderName =
                                document.getString("senderName") ?: "A rider"
                            val status =
                                document.getString("status") ?: "pending"

                            val message = when (status) {
                                "accepted" -> "$senderName accepted your connection request."
                                "rejected" -> "$senderName declined your connection request."
                                else -> "$senderName sent you a connection request."
                            }

                            AppNotification(
                                id = document.id,
                                type = "connection",
                                title = when (status) {
                                    "accepted" -> "Connection accepted"
                                    "rejected" -> "Connection request declined"
                                    else -> "New connection request"
                                },
                                message = message,
                                status = status
                            )
                        }.orEmpty()
                        refresh()
                    }

            val rideListener: ListenerRegistration =
                db.collection("rideRequests")
                    .whereEqualTo("riderId", currentUid)
                    .addSnapshotListener { snapshot, _ ->
                        rideNotifications = snapshot?.documents?.map { document ->
                            val rideName =
                                document.getString("rideName")
                                    ?: "your ride request"
                            val status =
                                document.getString("status") ?: "pending"

                            val creatorName =
                                document.getString("creatorName")
                                    ?: "Ride creator"

                            val message = when (status) {
                                "accepted" ->
                                    "$creatorName accepted your request to join $rideName."
                                "rejected" ->
                                    "$creatorName declined your request to join $rideName."
                                else ->
                                    "Your request to join $rideName is pending."
                            }

                            AppNotification(
                                id = document.id,
                                type = "ride",
                                title = when (status) {
                                    "accepted" -> "Ride request accepted"
                                    "rejected" -> "Ride request declined"
                                    else -> "Ride request pending"
                                },
                                message = message,
                                status = status
                            )
                        }.orEmpty()
                        refresh()
                    }

            onDispose {
                connectionListener.remove()
                rideListener.remove()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NotificationsBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "←",
                    color = NotificationsOrange,
                    fontSize = 28.sp
                )
            }

            Text(
                text = "Notifications",
                color = NotificationsWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Color(0xFF22262C))

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NotificationsOrange,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            currentUid == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please login again.",
                        color = NotificationsGray
                    )
                }
            }

            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔔",
                            fontSize = 36.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "No notifications yet",
                            color = NotificationsWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "New connection and ride updates will appear here.",
                            color = NotificationsGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = notifications,
                        key = { "${it.type}_${it.id}" }
                    ) { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification
) {
    val icon = when (notification.type) {
        "connection" -> "👥"
        "ride" -> "🏍️"
        else -> "🔔"
    }

    val statusIcon = when (notification.status) {
        "accepted" -> "✓"
        "rejected" -> "✕"
        else -> "•"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                NotificationsCard,
                RoundedCornerShape(15.dp)
            )
            .padding(15.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    Color(0xFF252A30),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 21.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.title,
                    color = NotificationsWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = statusIcon,
                    color = when (notification.status) {
                        "accepted" -> Color(0xFF4CAF50)
                        "rejected" -> Color(0xFFF44336)
                        else -> NotificationsOrange
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(5.dp))

            Text(
                text = notification.message,
                color = NotificationsGray,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
