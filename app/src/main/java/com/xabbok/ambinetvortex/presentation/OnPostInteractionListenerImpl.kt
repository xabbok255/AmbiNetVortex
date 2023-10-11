package com.xabbok.ambinetvortex.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.dto.AttachmentType
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.presentation.fragments.INTENT_EXTRA_IMAGE_URI
import com.xabbok.ambinetvortex.presentation.fragments.INTENT_EXTRA_POST
import com.xabbok.ambinetvortex.presentation.viewmodels.PostViewModel
import kotlinx.coroutines.launch


class OnPostInteractionListenerImpl(
    private val viewModel: PostViewModel,
    private val fragment: Fragment,
    private val appAuth: AppAuth
) :
    OnPostInteractionListener {

    override fun onLike(post: Post) {
        fragment.lifecycleScope.launch {
            if (!appAuth.isAuth()) {
                Toast.makeText(
                    fragment.requireContext(),
                    fragment.requireContext().getString(R.string.login_to_like_message),
                    Toast.LENGTH_LONG
                ).show()
                fragment.findNavController().navigate(
                    R.id.action_global_authFragment
                )
            } else {
                if (post.ownedByMe) {
                    Toast.makeText(
                        fragment.requireContext(),
                        fragment.requireContext()
                            .getString(R.string.you_cant_like_own_posts_message),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    viewModel.likeById(post)
                    return@launch
                }
            }
        }
    }

    override fun onEdit(post: Post) {
        fragment.findNavController().navigate(
            R.id.action_global_editPostFragment,
            bundleOf(Pair(INTENT_EXTRA_POST, post))
        )
    }

    override fun onShare(post: Post, view: View) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, post.content)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(intent, "Поделиться контентом")
        view.context.startActivity(shareIntent)
    }

    override fun onMore(post: Post, view: View) {
        /*val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.moremenu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.removePost -> {
                    viewModel.removeById(post.id)
                    true
                }
                R.id.editPost -> {
                    onEdit(post)
                    true
                }
                else -> {
                    false
                }
            }

        }
        popupMenu.show()*/
        //TODO
    }

    override fun onVideo(post: Post, view: View) {
        if (post.attachment?.type != AttachmentType.VIDEO)
            return

        post.attachment?.url?.let { video ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video))
                view.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(
                    "postVideo",
                    "Activity for Intent with url ${video.ifBlank { "NULL" }} not found"
                )
            }
        }


    }

    override fun onPostDetails(post: Post) {
        fragment.findNavController().navigate(
            R.id.feedFragmentToPostDetailsFragment,
            bundleOf(Pair(INTENT_EXTRA_POST, post))
        )

    }

    override fun onImageViewerFullscreen(image: String) {
        fragment.findNavController().navigate(
            R.id.action_global_imageViewerFragment,
            bundleOf(Pair(INTENT_EXTRA_IMAGE_URI, image))
        )
    }


}