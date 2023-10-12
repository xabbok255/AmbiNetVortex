package com.xabbok.ambinetvortex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.xabbok.ambinetvortex.databinding.LoadStateItemBinding

class PostLoadStateAdapter(private val retryListener: () -> Unit) :
    LoadStateAdapter<PostLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: PostLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PostLoadStateViewHolder {
        return PostLoadStateViewHolder(
            LoadStateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retryListener
        )
    }

}