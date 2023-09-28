package com.xabbok.ambinetvortex.presentation

import android.view.View
import com.xabbok.ambinetvortex.dto.Post

interface OnPostInteractionListener {
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onShare(post: Post, view: View)
    fun onMore(post: Post, view: View)
    fun onVideo(post: Post, view: View)
    fun onPostDetails(post: Post)
    fun onImageViewerFullscreen(image: String)
}