package com.xabbok.ambinetvortex.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.TerminalSeparatorType
import androidx.paging.insertSeparators
import androidx.recyclerview.widget.ConcatAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.snackbar.Snackbar
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.adapter.PostLoadStateAdapter
import com.xabbok.ambinetvortex.adapter.PostsAdapter
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.databinding.FragmentPostsBinding
import com.xabbok.ambinetvortex.dto.Divider
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.error.ScreenState
import com.xabbok.ambinetvortex.presentation.OnPostInteractionListenerImpl
import com.xabbok.ambinetvortex.presentation.viewmodels.PostViewModel
import com.xabbok.ambinetvortex.utils.withLoadStateHeaderFooterAndRefreshError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

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

            adapter.refresh()
            scrollOnNextSubmit = true

        }

        binding.retryButton.setOnClickListener {
            adapter.retry()
        }
    }

    private fun showError(state: ScreenState.Error) {
        if (state.repeatAction == null) {

            Snackbar.make(
                requireContext(),
                binding.snackBarCoordinator,
                getString(R.string.SNACKBAR_ERROR),
                Snackbar.LENGTH_LONG
            )
                .setTextMaxLines(10)
                .setAction("OK") {

                }
                .show()
        } else {
            Snackbar.make(
                requireContext(),
                binding.snackBarCoordinator,
                state.repeatText ?: getString(R.string.SNACKBAR_ERROR),
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.repeat)) {
                state.repeatAction.apply { this() }
            }
                .show()
        }

        viewModel.changeState(ScreenState.Working())
    }

    private fun showWorking(state: ScreenState.Working) {
        if (state.moveRecyclerViewPointerToTop) {
            scrollOnNextSubmit = true
            viewModel.changeState(ScreenState.Working())
        }
    }

    private fun showLoading(state: ScreenState.Loading) {

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

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScreenState.Error -> showError(state)
                is ScreenState.Loading -> showLoading(state)
                is ScreenState.Working -> showWorking(state)
            }
        }



        binding.postList.adapter = adapter.withLoadStateHeaderFooterAndRefreshError(
            footer = PostLoadStateAdapter { adapter.retry() },
            header = PostLoadStateAdapter {
                adapter.refresh()
            }
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
                    .insertSeparators(terminalSeparatorType = TerminalSeparatorType.SOURCE_COMPLETE) { before, after ->
                        if ((before as? Post)?.roundedDate() != (after as? Post)?.roundedDate()) {
                            return@insertSeparators Divider(Long.MAX_VALUE, (after as Post))
                        }

                        return@insertSeparators null
                    }
            }.collect {
                adapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.distinctUntilChanged { oldState, loadState ->
                binding.apply {
                    postListSwipeRefresh.isRefreshing =
                        loadState.mediator?.refresh is LoadState.Loading ||
                                loadState.mediator?.append is LoadState.Loading ||
                                loadState.mediator?.prepend is LoadState.Loading

                    //errorGroup.isVisible = loadState.mediator?.refresh is LoadState.Error
                    emptyText.isVisible =
                        loadState.mediator?.refresh is LoadState.NotLoading && adapter.itemCount == 0
                    //postList.isVisible = !binding.emptyText.isVisible

                    if (loadState.mediator?.refresh is LoadState.Error && oldState.mediator?.refresh !is LoadState.Error) {
                        binding.postList.smoothScrollToPosition(0)
                    }

                    if (loadState.mediator?.append is LoadState.Error && oldState.mediator?.append !is LoadState.Error) {
                        binding.postList.postDelayed(300) {
                            binding.postList.smoothScrollToPosition((binding.postList.adapter as ConcatAdapter).itemCount - 1)
                        }

                    }
                }

                false
            }
                .collect()
        }
    }
}