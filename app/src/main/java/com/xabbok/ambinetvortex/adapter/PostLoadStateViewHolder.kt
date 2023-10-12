package com.xabbok.ambinetvortex.adapter

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.xabbok.ambinetvortex.databinding.LoadStateItemBinding
import kotlinx.coroutines.CancellationException

class PostLoadStateViewHolder(
    private val binding: LoadStateItemBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener {
            retryListener.invoke()
        }
    }

    fun bind(loadState: LoadState) {
        binding.apply {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error && loadState.error !is CancellationException
            //TODO add text with error
        }
    }
}