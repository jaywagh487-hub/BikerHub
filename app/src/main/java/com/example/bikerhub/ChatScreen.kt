package com.example.bikerhub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue

private val ChatBackground = Color(0xFF0B0D10)
private val ChatCard = Color(0xFF171A1F)
private val ChatOrange = Color(0xFFFF6B00)
private val ChatWhite = Color(0xFFF5F5F5)
private val ChatGray = Color(0xFF8D9299)
private val ChatInput = Color(0xFF20242A)
private val ChatGreen = Color(0xFF34C759)

data class ChatMessageData(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)

@Composable
fun ChatScreen(
    otherUserId: String,
    otherUserName: String,
    onBack: () -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val currentUser = auth.currentUser

    val currentUid = currentUser?.uid

    var messageText by remember {
        mutableStateOf("")
    }

    var messages by remember {
        mutableStateOf<List<ChatMessageData>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var sending by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    val listState = rememberLazyListState()

    // ==================================================
    // CREATE DETERMINISTIC CHAT ID
    // ==================================================

    val chatId = remember(
        currentUid,
        otherUserId
    ) {

        if (
            currentUid.isNullOrBlank() ||
            otherUserId.isBlank()
        ) {
            ""
        } else {

            listOf(
                currentUid,
                otherUserId
            )
                .sorted()
                .joinToString("_")
        }
    }

    // ==================================================
    // REAL-TIME MESSAGE LISTENER
    // ==================================================

    DisposableEffect(chatId) {

        if (chatId.isBlank()) {

            loading = false

            onDispose { }

        } else {

            val listener =
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .orderBy(
                        "createdAt",
                        Query.Direction.ASCENDING
                    )
                    .addSnapshotListener { snapshot, error ->

                        if (error != null) {

                            loading = false

                            errorMessage =
                                error.message
                                    ?: "Unable to load messages."

                            return@addSnapshotListener
                        }

                        val loadedMessages =
                            snapshot
                                ?.documents
                                ?.map { document ->

                                    ChatMessageData(

                                        id =
                                            document.id,

                                        senderId =
                                            document.getString(
                                                "senderId"
                                            ).orEmpty(),

                                        receiverId =
                                            document.getString(
                                                "receiverId"
                                            ).orEmpty(),

                                        text =
                                            document.getString(
                                                "text"
                                            ).orEmpty(),

                                        createdAt =
                                            document.getTimestamp(
                                                "createdAt"
                                            )
                                                ?.toDate()
                                                ?.time
                                                ?: 0L
                                    )
                                }
                                .orEmpty()

                        messages =
                            loadedMessages

                        loading = false

                        errorMessage = ""
                    }

            onDispose {
                listener.remove()
            }
        }
    }

    // ==================================================
    // AUTO SCROLL
    // ==================================================

    LaunchedEffect(
        messages.size
    ) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )
        }
    }

    // ==================================================
    // SEND MESSAGE
    // ==================================================

    fun sendMessage() {

        val senderUid =
            currentUid ?: return

        val cleanText =
            messageText.trim()

        if (
            cleanText.isBlank() ||
            sending
        ) {
            return
        }

        if (chatId.isBlank()) {
            return
        }

        sending = true

        val chatReference =
            db.collection("chats")
                .document(chatId)

        val messageReference =
            chatReference
                .collection("messages")
                .document()

        val messageData =
            hashMapOf(
                "senderId" to senderUid,
                "receiverId" to otherUserId,
                "text" to cleanText,
                "createdAt" to FieldValue.serverTimestamp()
            )

        val chatData =
            hashMapOf(
                "participants" to listOf(
                    senderUid,
                    otherUserId
                ),
                "lastMessage" to cleanText,
                "lastMessageSenderId" to senderUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )

        chatReference
            .set(
                chatData,
                com.google.firebase.firestore.SetOptions.merge()
            )
            .continueWithTask {

                messageReference.set(
                    messageData
                )
            }
            .addOnSuccessListener {

                messageText = ""

                sending = false

                errorMessage = ""
            }
            .addOnFailureListener { exception ->

                sending = false

                errorMessage =
                    exception.message
                        ?: "Message could not be sent."
            }
    }

    // ==================================================
    // MAIN UI
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                ChatBackground
            )
    ) {

        // ==================================================
        // HEADER
        // ==================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatCard)
                .padding(
                    horizontal = 10.dp,
                    vertical = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBack
            ) {

                Text(
                    text = "←",
                    color = ChatOrange,
                    fontSize = 27.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = otherUserName,
                    color = ChatWhite,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Spacer(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ChatGreen)
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text(
                        text = "Biker",
                        color = ChatGray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // ==================================================
        // ERROR MESSAGE
        // ==================================================

        if (
            errorMessage.isNotBlank()
        ) {

            Text(
                text = errorMessage,
                color = Color(0xFFFF453A),
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    horizontal = 15.dp,
                    vertical = 7.dp
                )
            )
        }

        // ==================================================
        // MESSAGES
        // ==================================================

        if (
            loading
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "Loading chat...",
                    color = ChatGray,
                    fontSize = 13.sp
                )
            }

        } else if (
            messages.isEmpty()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "🏍️",
                    fontSize = 42.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text =
                        "Start the conversation",
                    color = ChatWhite,
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Send a message to $otherUserName",
                    color = ChatGray,
                    fontSize = 11.sp
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp
                    ),
                state = listState,
                verticalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                items(
                    items = messages,
                    key = {
                        it.id
                    }
                ) { message ->

                    ChatBubble(
                        message = message,
                        isMine =
                            message.senderId ==
                                    currentUid
                    )
                }

                item {

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                }
            }
        }

        // ==================================================
        // MESSAGE INPUT
        // ==================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatCard)
                .navigationBarsPadding()
                .padding(
                    horizontal = 10.dp,
                    vertical = 8.dp
                ),
            verticalAlignment =
                Alignment.Bottom
        ) {

            OutlinedTextField(
                value = messageText,
                onValueChange = {
                    messageText = it
                },
                modifier = Modifier.weight(1f),
                placeholder = {

                    Text(
                        text = "Type a message...",
                        color = ChatGray,
                        fontSize = 13.sp
                    )
                },
                singleLine = false,
                maxLines = 4,
                shape =
                    RoundedCornerShape(20.dp),
                keyboardOptions =
                    KeyboardOptions(
                        imeAction =
                            ImeAction.Default
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            sendMessage()
                        }
                    ),
                colors =
                    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor =
                            ChatInput,
                        unfocusedContainerColor =
                            ChatInput,
                        focusedBorderColor =
                            ChatOrange,
                        unfocusedBorderColor =
                            Color.Transparent,
                        focusedTextColor =
                            ChatWhite,
                        unfocusedTextColor =
                            ChatWhite,
                        cursorColor =
                            ChatOrange
                    )
            )

            Spacer(
                modifier = Modifier.width(7.dp)
            )

            Button(
                onClick = {
                    sendMessage()
                },
                enabled =
                    messageText.trim().isNotBlank() &&
                            !sending,
                modifier = Modifier
                    .height(52.dp),
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ChatOrange,
                        disabledContainerColor =
                            Color(0xFF3A3A3A)
                    )
            ) {

                Text(
                    text = "➤",
                    fontSize = 20.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// ======================================================
// CHAT BUBBLE
// ======================================================

@Composable
private fun ChatBubble(
    message: ChatMessageData,
    isMine: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {

        Column(
            modifier = Modifier
                .background(
                    if (isMine) {
                        ChatOrange
                    } else {
                        ChatCard
                    },
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart =
                            if (isMine) {
                                16.dp
                            } else {
                                4.dp
                            },
                        bottomEnd =
                            if (isMine) {
                                4.dp
                            } else {
                                16.dp
                            }
                    )
                )
                .padding(
                    horizontal = 13.dp,
                    vertical = 9.dp
                )
        ) {

            Text(
                text = message.text,
                color = ChatWhite,
                fontSize = 14.sp
            )

            if (message.createdAt > 0L) {

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                val timeText =
                    java.text.SimpleDateFormat(
                        "hh:mm a",
                        java.util.Locale.getDefault()
                    )
                        .format(
                            java.util.Date(
                                message.createdAt
                            )
                        )

                Text(
                    text = timeText,
                    color =
                        if (isMine) {
                            Color(0xFFFFE0CC)
                        } else {
                            ChatGray
                        },
                    fontSize = 8.sp
                )
            }
        }
    }
}