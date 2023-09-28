package com.xabbok.ambinetvortex.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.TerminalSeparatorType
import androidx.paging.insertSeparators
import by.kirich1409.viewbindingdelegate.viewBinding
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
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var adapter: PostsAdapter
    private val binding: FragmentPostsBinding by viewBinding(FragmentPostsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe()
        setupListeners()
    }

    private fun setupListeners() {

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
                            return@insertSeparators Divider(Random.nextLong(), next)
                        }

                        if (!dividerYesterday && next.isPublishedYesterday()
                        ) {
                            dividerYesterday = true
                            return@insertSeparators Divider(Random.nextLong(), next)
                        }

                        if (!dividerOlder && next.isPublishedLater()
                        ) {
                            dividerOlder = true
                            return@insertSeparators Divider(Random.nextLong(), next)
                        }

                        return@insertSeparators null
                    }
            }.collectLatest {
                adapter.submitData(it)
            }
        }

    }
}