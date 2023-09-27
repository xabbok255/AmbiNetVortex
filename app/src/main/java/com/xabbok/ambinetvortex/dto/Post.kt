package com.xabbok.ambinetvortex.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.xabbok.ambinetvortex.BuildConfig
import com.xabbok.ambinetvortex.utils.currentDate
import com.xabbok.ambinetvortex.utils.epochToDate
import com.xabbok.ambinetvortex.utils.hoursBetween

const val NON_EXISTING_POST_ID = 0L

sealed interface FeedItem {
    val id: Long

    fun withBaseUrls(): FeedItem
}

@Parcelize
data class Post(
    override val id: Long = NON_EXISTING_POST_ID,
    val authorId: Long = 0L,
    val author: String = "",
    var content: String = "",
    val published: Long = 0L,
    var likedByMe: Boolean = false,
    var likes: Int = 0,
    var shares: Int = 0,
    var video: String = "",
    val authorAvatar: String = "",
    var attachment: Attachment? = null,
    var ownedByMe: Boolean = false,

    ) : FeedItem, Parcelable {

    fun isNewPost(): Post? {
        return if (this.id == NON_EXISTING_POST_ID) this else null
    }

    override fun withBaseUrls(): Post {
        return this.copy(
            authorAvatar = "${BuildConfig.BASE_URL_AVATARS}${authorAvatar}",
            attachment = attachment?.copy(url = "${BuildConfig.BASE_URL_IMAGES}${attachment?.url}")
        )
    }

    fun isPublishedToday() : Boolean {
        return hoursBetween(
            epochToDate(published),
            currentDate()
        ) < 24
    }

    fun isPublishedYesterday() : Boolean {
        return hoursBetween(
            epochToDate(published), currentDate()
        ) in 24..48
    }

    fun isPublishedLater() : Boolean {
        return hoursBetween(
            epochToDate(published), currentDate()
        ) > 48
    }
}

fun List<FeedItem>.listWithBaseUrls(): List<FeedItem> = this.map {
    it.withBaseUrls()
}

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem {
    override fun withBaseUrls(): Ad {
        return this.copy(
            image = "${BuildConfig.BASE_URL_IMAGES}${image}",
        )
    }
}

data class Divider(
    override val id: Long,
    val nextPost: Post
) : FeedItem {
    override fun withBaseUrls(): FeedItem {
        return this
    }
}