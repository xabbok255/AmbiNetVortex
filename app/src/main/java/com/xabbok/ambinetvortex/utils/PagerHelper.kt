package com.xabbok.ambinetvortex.utils

import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

fun <T : Any, VH : RecyclerView.ViewHolder> PagingDataAdapter<T, VH>.withLoadStateHeaderFooterAndRefreshError(
    header: LoadStateAdapter<*>,
    footer: LoadStateAdapter<*>
): ConcatAdapter {
    addLoadStateListener { loadStates ->
        header.loadState = (loadStates.refresh as? LoadState.Error) ?: loadStates.prepend
        footer.loadState = loadStates.append
    }
    return ConcatAdapter(header, this, footer)
}