package com.xabbok.ambinetvortex.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.databinding.FragmentPostDetailsBinding
import com.xabbok.ambinetvortex.dto.AttachmentType
import com.xabbok.ambinetvortex.presentation.OnPostInteractionListenerImpl
import com.xabbok.ambinetvortex.presentation.viewmodels.PostViewModel
import com.xabbok.ambinetvortex.utils.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@AndroidEntryPoint
class FragmentPostDetails : Fragment(R.layout.fragment_post_details) {

    private val binding: FragmentPostDetailsBinding by viewBinding(FragmentPostDetailsBinding::bind)
    private val viewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onInteractionListener = OnPostInteractionListenerImpl(
            viewModel = viewModel,
            fragment = this,
            appAuth = appAuth
        )

        binding.post = arguments?.getParcelable(
            INTENT_EXTRA_POST
        )

        //добавляем верхнее меню с кнопкой назад
        /*(activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            setupActionBarWithNavControllerDefault()
        }*/

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                val p = binding.post//?.withBaseUrls()

                if (p == null) {
                    //в списке нет поста с нужным id, выходим
                    findNavController().navigateUp()
                } else {
                    //выводим данные с "нового" поста
                    binding.post = p

                    binding.avatar.load(
                        url = p.authorAvatar,
                        placeholder = R.drawable.ic_avatar_placeholder,
                        //roundedCornersRadius = 36
                    )

                    p.attachment?.takeIf { it.type == AttachmentType.IMAGE }?.let {
                        binding.attachmentImage.load(
                            url = it.url,
                            placeholder = R.drawable.ic_loading_placeholder
                        )
                    }

                    binding.more.isVisible = p.ownedByMe

                }
            }
        }

        binding.apply {
            binding.post?.let { post ->
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

                attachmentImage.setOnClickListener {
                    post.attachment?.url?.let { image ->
                        onInteractionListener.onImageViewerFullscreen(image)
                    }
                }
            }
        }

    }


}