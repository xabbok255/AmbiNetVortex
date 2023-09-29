package com.xabbok.ambinetvortex.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.TerminalSeparatorType
import androidx.paging.insertSeparators
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.adapter.PostLoadingStateAdapter
import com.xabbok.ambinetvortex.adapter.PostsAdapter
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.databinding.FragmentPostsBinding
import com.xabbok.ambinetvortex.dto.Divider
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.presentation.OnPostInteractionListenerImpl
import com.xabbok.ambinetvortex.presentation.viewmodels.PostViewModel
import com.xabbok.ambinetvortex.utils.withLoadStateHeaderFooterAndRefreshError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var adapter: PostsAdapter
    private val binding: FragmentPostsBinding by viewBinding(FragmentPostsBinding::bind)

    private val badgeDrawable: BadgeDrawable by lazy {
        val b = BadgeDrawable.create(requireActivity())
        b.badgeGravity = BadgeDrawable.TOP_END
        b.isVisible = false
        b
    }

    private var scrollOnNextSubmit: Boolean = false
        get() {
            if (field) {
                field = false
                return true
            }

            return false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe()
        setupListeners()
    }

    private fun setupListeners() {
        binding.postListSwipeRefresh.setOnRefreshListener {
            //binding.postListSwipeRefresh.isRefreshing = false
            //viewModel.loadPosts()
            binding.unreadMessagesButton.visibility = View.INVISIBLE
            badgeDrawable.isVisible = false

            scrollOnNextSubmit = true
            adapter.refresh()
        }
    }

    @kotlin.OptIn(FlowPreview::class)
    @OptIn(ExperimentalBadgeUtils::class)
    private fun subscribe() {
        adapter = PostsAdapter(
            OnPostInteractionListenerImpl(
                viewModel = viewModel,
                fragment = this,
                appAuth = appAuth
            )
        )

        binding.postList.adapter = adapter.withLoadStateHeaderFooterAndRefreshError(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() }
        )

        //скроллинг при добавлении нового элемента
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            adapter.loadStateFlow.debounce(300).collectLatest {
                if (it.refresh is LoadState.NotLoading
                    && it.append is LoadState.NotLoading
                ) {
                    if (scrollOnNextSubmit) {
                        binding.postList.postDelayed(100) {
                            binding.postList.smoothScrollToPosition(0)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            var dividerToday: Boolean
            var dividerYesterday: Boolean
            var dividerOlder: Boolean

            viewModel.data.map { pagingData ->
                pagingData
                    .also {
                        dividerToday = false
                        dividerYesterday = false
                        dividerOlder = false
                    }
                    .insertSeparators(terminalSeparatorType = TerminalSeparatorType.SOURCE_COMPLETE) { _, n ->
                        val next: Post =
                            (if (n == null) null else (n as? Post)) ?: return@insertSeparators null

                        if (!dividerToday && next.isPublishedToday()
                        ) {
                            dividerToday = true
                            return@insertSeparators Divider(Long.MAX_VALUE, next)
                        }

                        if (!dividerYesterday && next.isPublishedYesterday()
                        ) {
                            dividerYesterday = true
                            return@insertSeparators Divider(Long.MAX_VALUE, next)
                        }

                        if (!dividerOlder && next.isPublishedLater()
                        ) {
                            dividerOlder = true
                            return@insertSeparators Divider(Long.MAX_VALUE, next)
                        }

                        return@insertSeparators null
                    }
            }.collectLatest {
                adapter.submitData(it)
            }
        }

    }
}