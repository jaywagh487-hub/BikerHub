
package com.example.bikerhub
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// ======================================================
// COLORS
// ======================================================

private val DarkBackground = Color(0xFF0B0D10)
private val CardColor = Color(0xFF171A1F)
private val Orange = Color(0xFFFF6B00)
private val LightText = Color(0xFFF5F5F5)
private val GrayText = Color(0xFF8D9299)
private val BorderColor = Color(0xFF30343A)


// ======================================================
// DASHBOARD
// ======================================================

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onExitRequest: () -> Unit
) {

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var userName by remember {
        mutableStateOf("Rider")
    }

    var userEmail by remember {
        mutableStateOf("")
    }

    // Connected Riders screen
    var showConnectedRiders by remember {
        mutableStateOf(false)
    }

    // Notifications screen
    var showNotifications by remember {
        mutableStateOf(false)
    }

    // Private chat
    var chatUserId by remember {
        mutableStateOf<String?>(null)
    }

    var chatUserName by remember {
        mutableStateOf("")
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    BackHandler {
        when {
            chatUserId != null -> chatUserId = null
            showConnectedRiders -> showConnectedRiders = false
            showNotifications -> showNotifications = false
            selectedTab != 0 -> selectedTab = 0
            else -> onExitRequest()
        }
    }

    // ==================================================
    // LOAD CURRENT USER
    // ==================================================

    LaunchedEffect(Unit) {

        val currentUser = auth.currentUser

        if (currentUser != null) {

            userEmail = currentUser.email ?: ""

            firestore
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        userName =
                            document.getString("name")
                                ?.takeIf { it.isNotBlank() }
                                ?: "Rider"
                    }
                }
        }
    }

    // ==================================================
    // OPEN NOTIFICATIONS
    // ==================================================

    if (showNotifications) {

        NotificationsScreen(
            onBack = {
                showNotifications = false
            }
        )

        return
    }

    // ==================================================
    // OPEN PRIVATE CHAT
    // ==================================================

    if (chatUserId != null) {

        ChatScreen(
            otherUserId = chatUserId!!,
            otherUserName = chatUserName,
            onBack = {
                chatUserId = null
            }
        )

        return
    }

    // ==================================================
    // OPEN CONNECTED RIDERS
    // ==================================================

    if (showConnectedRiders) {

        ConnectedRidersScreen(
            onBack = {
                showConnectedRiders = false
            },
            onMessage = { riderId, riderName ->

                chatUserId = riderId
                chatUserName = riderName
            }
        )

        return
    }

    // ==================================================
    // MAIN DASHBOARD
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            when (selectedTab) {

                // =========================================
                // HOME
                // =========================================

                0 -> {

                    HomeFeedScreen(
                        userName = userName,

                        onProfileClick = {
                            selectedTab = 4
                        },

                        onExploreRiders = {
                            selectedTab = 5
                        },

                        onExploreRides = {
                            selectedTab = 1
                        },

                        onNotifications = {
                            showNotifications = true
                        }
                    )
                }

                // =========================================
                // RIDES
                // =========================================

                1 -> {

                    RidesScreen()
                }

                // =========================================
                // CREATE
                // =========================================

                2 -> {

                    CreateScreen()
                }

                // =========================================
                // MESSAGES
                // =========================================

                3 -> {

                    MessagesScreen(
                        onOpenConnectedRiders = {

                            showConnectedRiders = true
                        }
                    )
                }

                // =========================================
                // PROFILE
                // =========================================

                4 -> {

                    ProfileScreen(
                        name = userName,
                        email = userEmail,
                        onLogout = onLogout
                    )
                }

                // =========================================
                // EXPLORE RIDERS
                // =========================================

                5 -> {

                    ExploreRidersScreen(
                        onBack = {
                            selectedTab = 0
                        }
                    )
                }
            }
        }

        // ================================================
        // BOTTOM NAVIGATION
        // ================================================

        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = {

                selectedTab = it
            }
        )
    }
}


// ======================================================
// HOME FEED
// ======================================================

@Composable
fun HomeFeedScreen(
    userName: String,
    onProfileClick: () -> Unit,
    onExploreRiders: () -> Unit,
    onExploreRides: () -> Unit,
    onNotifications: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // ==============================================
        // TOP BAR
        // ==============================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 18.dp,
                    end = 10.dp,
                    top = 16.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {

                Row {

                    Text(
                        text = "BIKER",
                        color = LightText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = " HUB",
                        color = Orange,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "RIDE • CONNECT • BELONG",
                    color = GrayText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            // SEARCH
            TextButton(
                onClick = onExploreRiders
            ) {

                Text(
                    text = "⌕",
                    color = LightText,
                    fontSize = 30.sp
                )
            }

            // NOTIFICATIONS
            TextButton(
                onClick = onNotifications
            ) {

                Text(
                    text = "🔔",
                    fontSize = 18.sp
                )
            }

            // PROFILE
            TextButton(
                onClick = onProfileClick
            ) {

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252A30)),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = if (userName.isNotBlank()) {
                            userName
                                .first()
                                .uppercase()
                        } else {
                            "R"
                        },
                        color = Orange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(
            color = Color(0xFF22262C)
        )

        // ==============================================
        // WELCOME
        // ==============================================

        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 18.dp
            )
        ) {

            Text(
                text = "Hey, ${userName.ifBlank { "Rider" }} 👋",
                color = LightText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Ready for your next ride?",
                color = GrayText,
                fontSize = 13.sp
            )
        }

        // ==============================================
        // QUICK STATS
        // ==============================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            HomeStatCard(
                modifier = Modifier.weight(1f),
                value = "0",
                label = "Rides"
            )

            HomeStatCard(
                modifier = Modifier.weight(1f),
                value = "0",
                label = "Connections"
            )

            HomeStatCard(
                modifier = Modifier.weight(1f),
                value = "0",
                label = "Events"
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        HorizontalDivider(
            color = Color(0xFF22262C)
        )

        // ==============================================
        // FEED
        // ==============================================

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 20.dp
            )
        ) {

            item {

                HomeSectionTitle(
                    title = "COMMUNITY"
                )
            }

            item {

                CommunityCard(
                    icon = "🏍️",
                    title = "Explore Riders",
                    description = "Find bikers near you and connect with them.",
                    onClick = onExploreRiders
                )
            }

            item {

                CommunityCard(
                    icon = "🛣️",
                    title = "Explore Rides",
                    description = "Discover rides created by the Biker Hub community.",
                    onClick = onExploreRides
                )
            }

            item {

                CommunityCard(
                    icon = "🔥",
                    title = "Ride Together",
                    description = "Join group rides, meet fellow bikers and turn every route into an adventure.",
                    onClick = onExploreRides
                )
            }

            item {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Your biker community starts here.",
                    color = GrayText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        horizontal = 18.dp
                    )
                )
            }
        }
    }
}


// ======================================================
// HOME STAT CARD
// ======================================================

@Composable
private fun HomeStatCard(
    modifier: Modifier,
    value: String,
    label: String
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .padding(
                vertical = 14.dp,
                horizontal = 8.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            color = Orange,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = label,
            color = GrayText,
            fontSize = 10.sp
        )
    }
}


// ======================================================
// HOME SECTION TITLE
// ======================================================

@Composable
private fun HomeSectionTitle(
    title: String
) {

    Text(
        text = title,
        color = LightText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            start = 18.dp,
            top = 20.dp,
            bottom = 10.dp
        )
    )
}


// ======================================================
// COMMUNITY CARD
// ======================================================

@Composable
private fun CommunityCard(
    icon: String,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 5.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF252A30)),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }

            Spacer(
                modifier = Modifier.width(13.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    color = LightText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = description,
                    color = GrayText,
                    fontSize = 11.sp
                )
            }

            if (onClick != null) {

                TextButton(
                    onClick = onClick
                ) {

                    Text(
                        text = "→",
                        color = Orange,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}


// ======================================================
// CREATE SCREEN
// ======================================================

@Composable
fun CreateScreen() {

    var postText by remember {
        mutableStateOf("")
    }

    var showRideForm by remember {
        mutableStateOf(false)
    }

    var rideName by remember {
        mutableStateOf("")
    }

    var startPoint by remember {
        mutableStateOf("")
    }

    var destination by remember {
        mutableStateOf("")
    }

    var date by remember {
        mutableStateOf("")
    }

    var time by remember {
        mutableStateOf("")
    }

    var duration by remember {
        mutableStateOf("")
    }

    var maxRiders by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var saving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentPadding = PaddingValues(
            bottom = 25.dp
        )
    ) {

        item {

            Text(
                text = "Create",
                color = LightText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Share something with your biker community.",
                color = GrayText,
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // ==========================================
            // POST
            // ==========================================

            Text(
                text = "CREATE POST",
                color = LightText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedTextField(
                value = postText,
                onValueChange = {
                    postText = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = {
                    Text(
                        text = "Share your ride, bike or adventure..."
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText,
                    focusedPlaceholderColor = GrayText,
                    unfocusedPlaceholderColor = GrayText,
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = BorderColor,
                    cursorColor = Orange
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = {

                    // Post system will be connected later.
                    postText = ""
                    message = "Post feature ready for Firebase."
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange
                ),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = "POST",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // ==========================================
            // CREATE RIDE
            // ==========================================

            Button(
                onClick = {
                    showRideForm = !showRideForm
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = if (showRideForm) {
                        "CLOSE RIDE FORM"
                    } else {
                        "CREATE RIDE"
                    },
                    color = LightText,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showRideForm) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "New Ride",
                    color = LightText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                RideInputField(
                    title = "Ride Name",
                    value = rideName,
                    placeholder = "Jalgaon → Nashik"
                ) {
                    rideName = it
                }

                RideInputField(
                    title = "Starting Point",
                    value = startPoint,
                    placeholder = "Jalgaon"
                ) {
                    startPoint = it
                }

                RideInputField(
                    title = "Destination",
                    value = destination,
                    placeholder = "Nashik"
                ) {
                    destination = it
                }

                RideInputField(
                    title = "Date",
                    value = date,
                    placeholder = "05/10/2026"
                ) {
                    date = it
                }

                RideInputField(
                    title = "Time",
                    value = time,
                    placeholder = "06:00 AM"
                ) {
                    time = it
                }

                RideInputField(
                    title = "Duration",
                    value = duration,
                    placeholder = "5 Hours"
                ) {
                    duration = it
                }

                RideInputField(
                    title = "Maximum Riders",
                    value = maxRiders,
                    placeholder = "10"
                ) {
                    maxRiders = it
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = {
                        Text("Description")
                    },
                    placeholder = {
                        Text("Tell riders about this ride...")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText,
                        focusedLabelColor = Orange,
                        unfocusedLabelColor = GrayText,
                        focusedBorderColor = Orange,
                        unfocusedBorderColor = BorderColor,
                        cursorColor = Orange
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Button(
                    onClick = {

                        if (
                            rideName.isBlank() ||
                            startPoint.isBlank() ||
                            destination.isBlank() ||
                            date.isBlank() ||
                            time.isBlank()
                        ) {

                            message =
                                "Please fill Ride Name, Start, Destination, Date and Time."

                            return@Button
                        }

                        val currentUser =
                            FirebaseAuth
                                .getInstance()
                                .currentUser

                        if (currentUser == null) {

                            message =
                                "Please login again."

                            return@Button
                        }

                        saving = true
                        message = ""

                        val db =
                            FirebaseFirestore
                                .getInstance()

                        // Get actual profile name
                        db.collection("users")
                            .document(currentUser.uid)
                            .get()
                            .addOnSuccessListener { profile ->

                                val creatorName =
                                    profile.getString("name")
                                        ?.takeIf {
                                            it.isNotBlank()
                                        }
                                        ?: "Rider"

                                val rideData =
                                    hashMapOf<String, Any>(

                                        "creatorId" to
                                                currentUser.uid,

                                        "creatorName" to
                                                creatorName,

                                        "rideName" to
                                                rideName.trim(),

                                        "startPoint" to
                                                startPoint.trim(),

                                        "destination" to
                                                destination.trim(),

                                        "date" to
                                                date.trim(),

                                        "time" to
                                                time.trim(),

                                        "duration" to
                                                duration.trim(),

                                        "maxRiders" to
                                                maxRiders.trim(),

                                        "description" to
                                                description.trim(),

                                        "joinedRiders" to
                                                1,

                                        "createdAt" to
                                                FieldValue.serverTimestamp()
                                    )

                                db.collection("rides")
                                    .add(rideData)
                                    .addOnSuccessListener {

                                        saving = false

                                        rideName = ""
                                        startPoint = ""
                                        destination = ""
                                        date = ""
                                        time = ""
                                        duration = ""
                                        maxRiders = ""
                                        description = ""

                                        showRideForm = false

                                        message =
                                            "Ride created successfully ✓"
                                    }
                                    .addOnFailureListener {

                                        saving = false

                                        message =
                                            "Ride creation failed."
                                    }
                            }
                            .addOnFailureListener {

                                saving = false

                                message =
                                    "Could not load your profile."
                            }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Orange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Text(
                        text = if (saving) {
                            "CREATING..."
                        } else {
                            "CREATE RIDE"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (message.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = message,
                    color = if (
                        message.contains(
                            "success",
                            ignoreCase = true
                        )
                    ) {
                        Color(0xFF4CAF50)
                    } else {
                        GrayText
                    },
                    fontSize = 12.sp
                )
            }
        }
    }
}


// ======================================================
// RIDE INPUT FIELD
// ======================================================

@Composable
fun RideInputField(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        label = {
            Text(title)
        },
        placeholder = {
            Text(placeholder)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LightText,
            unfocusedTextColor = LightText,
            focusedLabelColor = Orange,
            unfocusedLabelColor = GrayText,
            focusedBorderColor = Orange,
            unfocusedBorderColor = BorderColor,
            cursorColor = Orange
        )
    )
}


// ======================================================
// RIDER DATA
// ======================================================

data class RiderProfileData(
    val uid: String = "",
    val name: String = "Rider",
    val email: String = "",
    val mobile: String = "",
    val showMobile: Boolean = false,
    val city: String = "",
    val bike: String = "",
    val instagram: String = "",
    val bio: String = ""
)


// ======================================================
// EXPLORE RIDERS
// ======================================================

@Composable
fun ExploreRidersScreen(
    onBack: () -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    val currentUid =
        auth.currentUser?.uid

    var searchText by remember {
        mutableStateOf("")
    }

    var riders by remember {
        mutableStateOf<List<RiderProfileData>>(
            emptyList()
        )
    }

    var selectedRider by remember {
        mutableStateOf<RiderProfileData?>(null)
    }

    var message by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(true)
    }

    // Connection status for each rider: pending / accepted / rejected
    var connectionStatuses by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    // ==================================================
    // RIDER PROFILE
    // ==================================================

    if (selectedRider != null) {

        RiderProfileScreen(
            rider = selectedRider!!,
            connectionStatus = connectionStatuses[selectedRider!!.uid] ?: "",
            onBack = {
                selectedRider = null
            },
            onConnect = { rider ->

                sendConnectionRequest(
                    rider = rider,
                    currentUid = currentUid,
                    firestore = firestore
                ) { result ->

                    message = result

                    if (result.contains("✓")) {
                        connectionStatuses =
                            connectionStatuses + (rider.uid to "pending")
                    }
                }
            }
        )

        return
    }

    // ==================================================
    // FIRESTORE LISTENER
    // ==================================================

    DisposableEffect(currentUid) {

        if (currentUid == null) {

            loading = false

            onDispose { }
        } else {

            val listener =
                firestore
                    .collection("users")
                    .addSnapshotListener { snapshot, error ->

                        loading = false

                        if (error != null) {

                            message =
                                "Unable to load riders."

                            riders =
                                emptyList()

                            return@addSnapshotListener
                        }

                        riders =
                            snapshot
                                ?.documents
                                ?.mapNotNull { document ->

                                    val uid =
                                        document.getString("uid")
                                            ?: document.id

                                    if (
                                        uid ==
                                        currentUid
                                    ) {
                                        return@mapNotNull null
                                    }

                                    RiderProfileData(

                                        uid = uid,

                                        name =
                                            document
                                                .getString(
                                                    "name"
                                                )
                                                ?.takeIf {
                                                    it.isNotBlank()
                                                }
                                                ?: "Rider",

                                        email =
                                            document
                                                .getString(
                                                    "email"
                                                )
                                                ?: "",

                                        mobile =
                                            document
                                                .getString(
                                                    "mobile"
                                                )
                                                ?: "",

                                        showMobile =
                                            document
                                                .getBoolean(
                                                    "showMobile"
                                                )
                                                ?: false,

                                        city =
                                            document
                                                .getString(
                                                    "city"
                                                )
                                                ?: "",

                                        bike =
                                            document
                                                .getString(
                                                    "bike"
                                                )
                                                ?: "",

                                        instagram =
                                            document
                                                .getString(
                                                    "instagram"
                                                )
                                                ?: "",

                                        bio =
                                            document
                                                .getString(
                                                    "bio"
                                                )
                                                ?: ""
                                    )
                                }
                                ?: emptyList()
                    }

            onDispose {
                listener.remove()
            }
        }
    }

    // ==================================================
    // CONNECTION STATUS LISTENER
    // ==================================================

    DisposableEffect(currentUid) {

        if (currentUid == null) {

            connectionStatuses = emptyMap()
            onDispose { }
        } else {

            val listener =
                firestore
                    .collection("connectionRequests")
                    .whereEqualTo("senderId", currentUid)
                    .addSnapshotListener { snapshot, _ ->

                        connectionStatuses =
                            snapshot
                                ?.documents
                                ?.associate { document ->
                                    val receiverId =
                                        document.getString("receiverId") ?: ""
                                    val status =
                                        document.getString("status") ?: ""
                                    receiverId to status
                                }
                                ?: emptyMap()
                    }

            onDispose {
                listener.remove()
            }
        }
    }

    val filteredRiders =
        riders.filter { rider ->

            val query =
                searchText
                    .trim()
                    .lowercase()

            query.isEmpty() ||
                    rider.name
                        .lowercase()
                        .contains(query) ||
                    rider.city
                        .lowercase()
                        .contains(query) ||
                    rider.bike
                        .lowercase()
                        .contains(query)
        }

    // ==================================================
    // UI
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "←",
                    color = Orange,
                    fontSize = 28.sp
                )
            }

            Spacer(
                modifier = Modifier.width(5.dp)
            )

            Text(
                text = "Explore Riders",
                color = LightText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    "Search name, city or bike"
                )
            },
            leadingIcon = {
                Text(
                    "⌕",
                    fontSize = 20.sp
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LightText,
                unfocusedTextColor = LightText,
                focusedPlaceholderColor = GrayText,
                unfocusedPlaceholderColor = GrayText,
                focusedBorderColor = Orange,
                unfocusedBorderColor = BorderColor,
                cursorColor = Orange
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (message.isNotEmpty()) {

            Text(
                text = message,
                color = if (
                    message.contains(
                        "✓"
                    )
                ) {
                    Color(0xFF4CAF50)
                } else {
                    GrayText
                },
                fontSize = 12.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        when {

            loading -> {

                Text(
                    text = "Loading riders...",
                    color = GrayText,
                    fontSize = 13.sp
                )
            }

            filteredRiders.isEmpty() -> {

                Text(
                    text =
                        if (searchText.isBlank()) {
                            "No other riders found yet."
                        } else {
                            "No rider found."
                        },
                    color = GrayText,
                    fontSize = 13.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 20.dp
            )
        ) {

            items(
                items = filteredRiders,
                key = {
                    it.uid
                }
            ) { rider ->

                RiderCard(
                    rider = rider,
                    connectionStatus = connectionStatuses[rider.uid] ?: "",

                    onViewProfile = {
                        selectedRider = rider
                    },

                    onConnect = {

                        sendConnectionRequest(
                            rider = rider,
                            currentUid = currentUid,
                            firestore = firestore
                        ) { result ->

                            message = result

                            if (result.contains("✓")) {
                                connectionStatuses =
                                    connectionStatuses + (rider.uid to "pending")
                            }
                        }
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
fun RiderCard(
    rider: RiderProfileData,
    connectionStatus: String = "",
    onViewProfile: () -> Unit,
    onConnect: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .padding(15.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF30343A)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        rider.name
                            .firstOrNull()
                            ?.uppercase()
                            ?: "R",
                    color = Orange,
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = rider.name,
                    color = LightText,
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                val info =
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
                        if (info.isBlank()) {
                            "Biker"
                        } else {
                            info
                        },
                    color = GrayText,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = onViewProfile,
                modifier = Modifier.weight(1f),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2A2E34)
                    ),
                shape =
                    RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = "PROFILE",
                    color = LightText,
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    if (connectionStatus.isBlank() || connectionStatus == "rejected") {
                        onConnect()
                    }
                },
                enabled = connectionStatus.isBlank() || connectionStatus == "rejected",
                modifier = Modifier.weight(1f),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (connectionStatus == "pending" || connectionStatus == "accepted") {
                            Color(0xFF2A2E34)
                        } else {
                            Orange
                        },
                        disabledContainerColor = Color(0xFF2A2E34),
                        disabledContentColor = LightText
                    ),
                shape =
                    RoundedCornerShape(10.dp)
            ) {

                Text(
                    text = when (connectionStatus) {
                        "pending" -> "✓ SENT"
                        "accepted" -> "✓ CONNECTED"
                        else -> "CONNECT"
                    },
                    color = if (connectionStatus == "pending" || connectionStatus == "accepted") LightText else Color.Black,
                    fontSize = 11.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// ======================================================
// RIDER PROFILE
// ======================================================

@Composable
fun RiderProfileScreen(
    rider: RiderProfileData,
    connectionStatus: String = "",
    onBack: () -> Unit,
    onConnect: (RiderProfileData) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentPadding =
            PaddingValues(bottom = 25.dp)
    ) {

        item {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "← BACK",
                    color = Orange,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF30343A)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        rider.name
                            .firstOrNull()
                            ?.uppercase()
                            ?: "R",
                    color = Orange,
                    fontSize = 35.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = rider.name,
                color = LightText,
                fontSize = 24.sp,
                fontWeight =
                    FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (rider.city.isNotBlank()) {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text = "📍 ${rider.city}",
                    color = GrayText,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            ProfileDetail(
                title = "Bike / Model",
                value =
                    rider.bike.ifBlank {
                        "Not added"
                    }
            )

            ProfileDetail(
                title = "City / Location",
                value =
                    rider.city.ifBlank {
                        "Not added"
                    }
            )

            ProfileDetail(
                title = "Instagram",
                value =
                    rider.instagram.ifBlank {
                        "Not added"
                    }
            )

            ProfileDetail(
                title = "Mobile Number",
                value =
                    if (
                        rider.showMobile &&
                        rider.mobile.isNotBlank()
                    ) {
                        rider.mobile
                    } else {
                        "Private"
                    }
            )

            ProfileDetail(
                title = "Bio",
                value =
                    rider.bio.ifBlank {
                        "Not added"
                    }
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Button(
                onClick = {
                    if (connectionStatus.isBlank() || connectionStatus == "rejected") {
                        onConnect(rider)
                    }
                },
                enabled = connectionStatus.isBlank() || connectionStatus == "rejected",
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (connectionStatus == "pending" || connectionStatus == "accepted") {
                            Color(0xFF2A2E34)
                        } else {
                            Orange
                        },
                        disabledContainerColor = Color(0xFF2A2E34),
                        disabledContentColor = LightText
                    ),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text = when (connectionStatus) {
                        "pending" -> "✓ REQUEST SENT"
                        "accepted" -> "✓ CONNECTED"
                        else -> "CONNECT"
                    },
                    color = if (connectionStatus == "pending" || connectionStatus == "accepted") LightText else Color.Black,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// ======================================================
// SEND CONNECTION REQUEST
// ======================================================

fun sendConnectionRequest(
    rider: RiderProfileData,
    currentUid: String?,
    firestore: FirebaseFirestore,
    onResult: (String) -> Unit
) {

    if (currentUid == null) {

        onResult(
            "Please login again."
        )

        return
    }

    if (currentUid == rider.uid) {

        onResult(
            "You cannot connect with yourself."
        )

        return
    }

    // Check pending request
    firestore
        .collection("connectionRequests")
        .whereEqualTo(
            "senderId",
            currentUid
        )
        .whereEqualTo(
            "receiverId",
            rider.uid
        )
        .whereEqualTo(
            "status",
            "pending"
        )
        .get()
        .addOnSuccessListener { snapshot ->

            if (!snapshot.isEmpty) {

                onResult(
                    "Connection request already sent."
                )

                return@addOnSuccessListener
            }

            // ==========================================
            // GET ACTUAL SENDER NAME FROM FIRESTORE
            // ==========================================

            firestore
                .collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener { senderDocument ->

                    val senderName =
                        senderDocument
                            .getString("name")
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Rider"

                    val request =
                        hashMapOf<String, Any>(

                            "senderId" to
                                    currentUid,

                            "senderName" to
                                    senderName,

                            "receiverId" to
                                    rider.uid,

                            "receiverName" to
                                    rider.name,

                            "status" to
                                    "pending",

                            "createdAt" to
                                    FieldValue.serverTimestamp()
                        )

                    firestore
                        .collection(
                            "connectionRequests"
                        )
                        .add(request)
                        .addOnSuccessListener {

                            onResult(
                                "Connection request sent ✓"
                            )
                        }
                        .addOnFailureListener {

                            onResult(
                                "Failed to send request."
                            )
                        }
                }
                .addOnFailureListener {

                    onResult(
                        "Could not load your profile."
                    )
                }
        }
        .addOnFailureListener {

            onResult(
                "Could not check request."
            )
        }
}


// ======================================================
// PROFILE
// ======================================================

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    onLogout: () -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var fullName by remember {
        mutableStateOf(name)
    }

    var userEmail by remember {
        mutableStateOf(email)
    }

    var mobile by remember {
        mutableStateOf("")
    }

    var city by remember {
        mutableStateOf("")
    }

    var bike by remember {
        mutableStateOf("")
    }

    var instagram by remember {
        mutableStateOf("")
    }

    var bio by remember {
        mutableStateOf("")
    }

    var showMobile by remember {
        mutableStateOf(false)
    }

    var isEditing by remember {
        mutableStateOf(false)
    }

    var saving by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    // ==================================================
    // LOAD PROFILE
    // ==================================================

    LaunchedEffect(Unit) {

        val currentUser =
            auth.currentUser

        if (currentUser != null) {

            userEmail =
                currentUser.email ?: email

            firestore
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        fullName =
                            document.getString(
                                "name"
                            ) ?: name

                        mobile =
                            document.getString(
                                "mobile"
                            ) ?: ""

                        city =
                            document.getString(
                                "city"
                            ) ?: ""

                        bike =
                            document.getString(
                                "bike"
                            ) ?: ""

                        instagram =
                            document.getString(
                                "instagram"
                            ) ?: ""

                        bio =
                            document.getString(
                                "bio"
                            ) ?: ""

                        showMobile =
                            document.getBoolean(
                                "showMobile"
                            ) ?: false
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

            Text(
                text = "My Profile",
                color = LightText,
                fontSize = 25.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = {

                    isEditing =
                        !isEditing

                    message = ""
                }
            ) {

                Text(
                    text =
                        if (isEditing) {
                            "CANCEL"
                        } else {
                            "EDIT"
                        },
                    color = Orange,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // ==================================================
        // PROFILE AVATAR
        // ==================================================

        Box(
            modifier = Modifier
                .size(95.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF30343A)
                )
                .align(
                    Alignment.CenterHorizontally
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    fullName
                        .firstOrNull()
                        ?.uppercase()
                        ?: "R",
                color = Orange,
                fontSize = 32.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text =
                fullName.ifBlank {
                    "Rider"
                },
            color = LightText,
            fontSize = 21.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.align(
                    Alignment.CenterHorizontally
                )
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = userEmail,
            color = GrayText,
            fontSize = 12.sp,
            modifier =
                Modifier.align(
                    Alignment.CenterHorizontally
                )
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        // ==================================================
        // PROFILE FIELDS
        // ==================================================

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding =
                PaddingValues(
                    bottom = 10.dp
                )
        ) {

            item {

                if (isEditing) {

                    ProfileEditField(
                        title = "Full Name",
                        value = fullName
                    ) {

                        fullName = it
                    }

                    ProfileEditField(
                        title = "Email",
                        value = userEmail,
                        enabled = false
                    ) {
                    }

                    ProfileEditField(
                        title = "Mobile Number",
                        value = mobile
                    ) {

                        mobile = it
                    }

                    ProfileEditField(
                        title = "City / Location",
                        value = city
                    ) {

                        city = it
                    }

                    ProfileEditField(
                        title = "Bike / Model",
                        value = bike
                    ) {

                        bike = it
                    }

                    ProfileEditField(
                        title = "Instagram ID",
                        value = instagram
                    ) {

                        instagram = it
                    }

                    ProfileEditField(
                        title = "Bio",
                        value = bio,
                        singleLine = false
                    ) {

                        bio = it
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 8.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "Show Mobile Number",
                                color =
                                    LightText,
                                fontSize = 14.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    if (showMobile) {
                                        "Other riders can see your number"
                                    } else {
                                        "Your number stays private"
                                    },
                                color =
                                    GrayText,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = showMobile,
                            onCheckedChange = {
                                showMobile = it
                            }
                        )
                    }

                } else {

                    ProfileDetail(
                        title = "Full Name",
                        value = fullName
                    )

                    ProfileDetail(
                        title = "Email",
                        value = userEmail
                    )

                    ProfileDetail(
                        title = "Mobile Number",
                        value =
                            if (
                                showMobile &&
                                mobile.isNotBlank()
                            ) {
                                mobile
                            } else {
                                "Private"
                            }
                    )

                    ProfileDetail(
                        title = "City / Location",
                        value =
                            city.ifBlank {
                                "Not added"
                            }
                    )

                    ProfileDetail(
                        title = "Bike / Model",
                        value =
                            bike.ifBlank {
                                "Not added"
                            }
                    )

                    ProfileDetail(
                        title = "Instagram ID",
                        value =
                            instagram.ifBlank {
                                "Not added"
                            }
                    )

                    ProfileDetail(
                        title = "Bio",
                        value =
                            bio.ifBlank {
                                "Not added"
                            }
                    )
                }

                if (message.isNotEmpty()) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text = message,
                        color =
                            if (
                                message.contains(
                                    "success",
                                    ignoreCase = true
                                ) ||
                                message.contains("✓")
                            ) {
                                Color(0xFF4CAF50)
                            } else {
                                GrayText
                            },
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ==================================================
        // SAVE
        // ==================================================

        if (isEditing) {

            Button(
                onClick = {

                    val currentUser =
                        auth.currentUser

                    if (currentUser == null) {

                        message =
                            "Please login again."

                        return@Button
                    }

                    saving = true
                    message = ""

                    val data =
                        hashMapOf<String, Any>(

                            "name" to
                                    fullName.trim(),

                            "email" to
                                    userEmail,

                            "mobile" to
                                    mobile.trim(),

                            "city" to
                                    city.trim(),

                            "bike" to
                                    bike.trim(),

                            "instagram" to
                                    instagram
                                        .trim()
                                        .removePrefix("@"),

                            "bio" to
                                    bio.trim(),

                            "showMobile" to
                                    showMobile
                        )

                    firestore
                        .collection("users")
                        .document(currentUser.uid)
                        .set(
                            data,
                            SetOptions.merge()
                        )
                        .addOnSuccessListener {

                            saving = false

                            isEditing = false

                            message =
                                "Profile updated successfully ✓"
                        }
                        .addOnFailureListener { error ->

                            saving = false

                            message =
                                "Save failed: ${error.message}"
                        }
                },
                modifier =
                    Modifier.fillMaxWidth(),
                enabled = !saving,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Orange
                    ),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Text(
                    text =
                        if (saving) {
                            "SAVING..."
                        } else {
                            "SAVE CHANGES"
                        },
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }

        // ==================================================
        // LOGOUT
        // ==================================================

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF2A2E34)
                ),
            shape =
                RoundedCornerShape(12.dp)
        ) {

            Text(
                text = "LOGOUT",
                color = Color.White,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// ======================================================
// PROFILE EDIT FIELD
// ======================================================

@Composable
fun ProfileEditField(
    title: String,
    value: String,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 12.dp
            ),
        singleLine = singleLine,
        minLines =
            if (singleLine) {
                1
            } else {
                3
            },
        label = {
            Text(title)
        },
        shape =
            RoundedCornerShape(12.dp),
        colors =
            OutlinedTextFieldDefaults.colors(

                focusedTextColor =
                    LightText,

                unfocusedTextColor =
                    LightText,

                disabledTextColor =
                    GrayText,

                focusedLabelColor =
                    Orange,

                unfocusedLabelColor =
                    GrayText,

                disabledLabelColor =
                    GrayText,

                focusedBorderColor =
                    Orange,

                unfocusedBorderColor =
                    BorderColor,

                disabledBorderColor =
                    Color(0xFF44484F),

                cursorColor =
                    Orange
            )
    )
}


// ======================================================
// PROFILE DETAIL
// ======================================================

@Composable
fun ProfileDetail(
    title: String,
    value: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 12.dp
            )
            .background(
                CardColor,
                RoundedCornerShape(12.dp)
            )
            .padding(15.dp)
    ) {

        Text(
            text = title,
            color = GrayText,
            fontSize = 11.sp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,
            color = LightText,
            fontSize = 15.sp
        )
    }
}


// ======================================================
// BOTTOM NAVIGATION
// ======================================================

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF111318)
            )
            .padding(
                vertical = 7.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceAround,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        BottomNavItem(
            icon = "⌂",
            label = "Home",
            selected =
                selectedTab == 0
        ) {

            onTabSelected(0)
        }

        BottomNavItem(
            icon = "⌁",
            label = "Rides",
            selected =
                selectedTab == 1
        ) {

            onTabSelected(1)
        }

        BottomNavItem(
            icon = "+",
            label = "Create",
            selected =
                selectedTab == 2
        ) {

            onTabSelected(2)
        }

        BottomNavItem(
            icon = "□",
            label = "Messages",
            selected =
                selectedTab == 3
        ) {

            onTabSelected(3)
        }

        BottomNavItem(
            icon = "●",
            label = "Profile",
            selected =
                selectedTab == 4
        ) {

            onTabSelected(4)
        }
    }
}


// ======================================================
// BOTTOM NAV ITEM
// ======================================================

@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    TextButton(
        onClick = onClick
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = icon,
                color =
                    if (selected) {
                        Orange
                    } else {
                        GrayText
                    },
                fontSize = 22.sp
            )

            Text(
                text = label,
                color =
                    if (selected) {
                        Orange
                    } else {
                        GrayText
                    },
                fontSize = 9.sp
            )
        }
    }
}
