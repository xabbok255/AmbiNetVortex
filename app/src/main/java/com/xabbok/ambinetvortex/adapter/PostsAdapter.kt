package com.xabbok.ambinetvortex.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.databinding.CardDividerBinding
import com.xabbok.ambinetvortex.databinding.CardPostBinding
import com.xabbok.ambinetvortex.databinding.CardPostPlaceholderBinding
import com.xabbok.ambinetvortex.dto.AttachmentType
import com.xabbok.ambinetvortex.dto.Divider
import com.xabbok.ambinetvortex.dto.FeedItem
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.presentation.OnPostInteractionListener
import com.xabbok.ambinetvortex.utils.formatDateWithYear
import com.xabbok.ambinetvortex.utils.formatDateWithoutYear
import com.xabbok.ambinetvortex.utils.formatNumber
import com.xabbok.ambinetvortex.utils.load


class PostsAdapter(
    private val onInteractionListener: OnPostInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostItemCallback()) {
    override fun getItemViewType(position: Int): Int {
        Log.e("getItem", getItem(position).toString())
        when (getItem(position)) {
            //is Ad -> R.layout.card_ad
            is Post -> {
                return R.layout.card_post
            }

            is Divider -> {
                return R.layout.card_divider
            }

            null -> {
                return R.layout.card_post_placeholder
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        when (val item = getItem(position)) {
            //is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(
                item,
                payloads.filterIsInstance(PostPayload::class.java).singleOrNull()
            )

            is Divider -> (holder as? DividerViewHolder)?.bind(item)
            null -> (holder as? PostPlaceHolderViewHolder)?.bind()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                PostViewHolder(
                    CardPostBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), onInteractionListener
                )
            }

            /*R.layout.card_ad -> {
                AdViewHolder(
                    CardAdBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }*/

            R.layout.card_divider -> {
                DividerViewHolder(
                    CardDividerBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            R.layout.card_post_placeholder -> {
                PostPlaceHolderViewHolder(
                    CardPostPlaceholderBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            else ->
                error("unknown view type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (val item = getItem(position)) {
//            is Ad -> (holder as? AdViewHolder)?.bind(item)
//            is Post -> (holder as? PostViewHolder)?.bind(item)
//            is Divider -> (holder as? DividerViewHolder)?.bind(item)
//            null -> error("unknown item type")
//        }
        onBindViewHolder(holder, position, emptyList())
    }
}

class DividerViewHolder(private val binding: CardDividerBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(divider: Divider) {
        val dateStr: String = divider.nextPost.let { post ->
            if (post.isPublishedToday()) {
                return@let binding.root.resources.getString(R.string.today)
            } else if (post.isPublishedYesterday()) {
                return@let binding.root.resources.getString(R.string.yesterday)
            } else if (post.isPublishedCurrentYear()){
                //вывести дату без года
                return@let formatDateWithoutYear(post.published)
            } else {
                //вывести дату с годом
                return@let formatDateWithYear(post.published)
            }
        }
        binding.titleText.text = dateStr
    }
}

class PostPlaceHolderViewHolder(private val binding: CardPostPlaceholderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind() {

    }
}

/*class AdViewHolder(private val binding: CardAdBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.image.load(ad.image, R.drawable.ic_loading_placeholder)
    }
}*/

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnPostInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post, payload: PostPayload?) {
        val postPayload = payload ?: PostPayload(likedByMe = post.likedByMe, content = post.content)

        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            postPayload.content?.let { content.text = it }
            postPayload.likedByMe?.let {
                heartIcon.isChecked = it

                if (it) {
                    ObjectAnimator.ofPropertyValuesHolder(
                        heartIcon,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F)
                    ).start()
                } else {
                    ObjectAnimator.ofPropertyValuesHolder(
                        heartIcon,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2F, 1.0F),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2F, 1.0F)
                    ).start()
                }
            }
            heartIcon.text = formatNumber(post.likes)
            heartIcon.isToggleCheckedStateOnClick = false
            //heartIcon.setImageResource(if (post.likedByMe) R.drawable.ic_baseline_favorite_red_24 else R.drawable.ic_baseline_favorite_border_24)
            //likes.text = formatNumber(post.likes)
            shareButton.text = formatNumber(post.shares)
            postVideoGroup.visibility =
                if ((post.attachment?.type == AttachmentType.VIDEO) && (!post.attachment?.url.isNullOrEmpty())) VISIBLE else GONE
            more.isVisible = post.ownedByMe
            avatar.load(
                url = post.authorAvatar,
                placeholder = R.drawable.ic_avatar_placeholder,
                //roundedCornersRadius = 36
            )

            post.attachment?.takeIf { it.type == AttachmentType.IMAGE }?.let {
                attachmentImage.visibility = VISIBLE
                attachmentImage.load(
                    url = it.url,
                    placeholder = R.drawable.ic_loading_placeholder
                )
            } ?: let {
                attachmentImage.visibility = GONE
            }
        }

        binding.apply {
            heartIcon.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            shareButton.setOnClickListener {
                onInteractionListener.onShare(post, it)
            }

            more.setOnClickListener {
                onInteractionListener.onMore(post, it)
            }

            videoPreview.setOnClickListener {
                onInteractionListener.onVideo(post, it)
            }

            root.setOnClickListener {
                onInteractionListener.onPostDetails(post)
            }

            attachmentImage.setOnClickListener {
                post.attachment?.url?.let { image ->
                    onInteractionListener.onImageViewerFullscreen(image)
                }
            }
        }
    }
}

data class PostPayload(
    val likedByMe: Boolean? = null,
    val content: String? = null
)

class PostItemCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any {
        return PostPayload(
            likedByMe = (newItem as? Post)?.likedByMe.takeIf { it != (oldItem as? Post)?.likedByMe },
            content = (newItem as? Post)?.content.takeIf { it != (oldItem as? Post)?.content }
        )
    }

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class)
            return false

        //временная проверка содержимого рекламы по имени изображения, а не по id
        /*(oldItem as? Ad)?.let {
            return (oldItem as Ad).image == (newItem as Ad).image
        }*/


        //временная проверка разделителя по id следующего поста, а не по своему id
        (oldItem as? Divider)?.let {
            return (oldItem as Divider).nextPost.id == (newItem as Divider).nextPost.id
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}