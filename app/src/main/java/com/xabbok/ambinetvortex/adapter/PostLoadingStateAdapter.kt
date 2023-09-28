package com.xabbok.ambinetvortex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xabbok.ambinetvortex.databinding.LoadStateItemBinding
import kotlinx.coroutines.CancellationException

class PostLoadingStateAdapter(private val retryListener: () -> Unit) :
    LoadStateAdapter<PostLoadingViewHolder>() {
    override fun onBindViewHolder(holder: PostLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostLoadingViewHolder =
        PostLoadingViewHolder(
            LoadStateItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), retryListener
        )

}

class PostLoadingViewHolder(
    private val loadStateItemBinding: LoadStateItemBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(loadStateItemBinding.root) {
    fun bind(loadState: LoadState) {
        loadStateItemBinding.apply {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error && loadState.error !is CancellationException

            retryButton.setOnClickListener {
                retryListener()
            }
        }
    }
}