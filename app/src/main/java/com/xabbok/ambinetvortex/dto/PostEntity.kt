package com.xabbok.ambinetvortex.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xabbok.ambinetvortex.utils.AttachmentConverter
import com.xabbok.ambinetvortex.utils.DateConverter
import java.util.Date

@Entity(tableName = "PostEntity")
@TypeConverters(AttachmentConverter::class, DateConverter::class)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String = "",
    val authorId: Long = 0L,
    val authorAvatar: String?,
    val content: String = "",
    val published: Date,
    var likedByMe: Boolean = false,
    var likes: Int = 0,
    var shares: Int = 0,
    //@Embedded
    var attachment: Attachment? = null,
    var visible: Boolean = true
) {


    companion object {
        fun fromDto(dto: Post): PostEntity {
            dto.apply {
                return PostEntity(
                    id = id,
                    author = author,
                    authorId = authorId,
                    authorAvatar = authorAvatar,
                    content = content,
                    published = published,
                    likedByMe = likedByMe,
                    likes = likes,
                    shares = shares,
                    attachment = attachment
                )
            }
        }

        fun toDto(entity: PostEntity) : Post {
            entity.apply {
                return Post(
                    id = id,
                    author = author,
                    authorId = authorId,
                    content = content,
                    published = published,
                    likedByMe = likedByMe,
                    likes = likes,
                    shares = shares,
                    authorAvatar = authorAvatar ?: "",
                    attachment = attachment,
                )
            }
        }
    }
}

fun List<PostEntity>.toDto() : List<Post> = map { PostEntity.toDto(it) }

fun List<Post>.toEntity() : List<PostEntity> = map(PostEntity::fromDto)