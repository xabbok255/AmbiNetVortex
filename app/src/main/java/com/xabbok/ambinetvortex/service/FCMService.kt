package com.xabbok.ambinetvortex.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.dto.PushMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val gson = Gson()
    private val channelId = "remote"

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        /*message.data[action]?.let {
            try {
                when (Action.valueOf(it)) {
                    Action.LIKE -> handleLike(
                        gson.fromJson(
                            message.data[content],
                            Like::class.java
                        )
                    )
                }
            } catch (e: java.lang.Exception) {
                e.message?.apply {
                    Log.e("FirebaseMessages", this)
                }
            }
        }*/

        message.data[content]?.let {
            runCatching {
                handlePushMessage(gson.fromJson(it, PushMessage::class.java))
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.postAuthor
                )
            )
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100000), notification)
    }

    @SuppressLint("MissingPermission")
    private fun handlePushMessage(data: PushMessage) {
        val currentId: Long? = appAuth.data.value?.id
        val recipientId: Long? = data.recipientId

        val notificationText: String? =
            when {
                recipientId == null -> {
                    //Массовая рассылка
                    "${data.content} (Массовая)"
                }
                (recipientId != currentId) -> {
                    //анонимная аутентификация или другая аутентификация, отправляем токен
                    appAuth.sendPushToken()
                    null
                }
                else -> {
                    //recipientId == currentId, показываем уведомление
                    data.content
                }
            }

        notificationText?.let {
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                    getString(R.string.app_name)
                )
                .setContentText(it)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(it)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100000), notification)
        }
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }
}

enum class Action {
    LIKE
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
    val text: String
)