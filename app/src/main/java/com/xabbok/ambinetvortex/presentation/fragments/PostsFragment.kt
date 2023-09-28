package com.xabbok.ambinetvortex.presentation.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.xabbok.ambinetvortex.R
import com.xabbok.ambinetvortex.presentation.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {
    private val viewModel: PostViewModel by activityViewModels()
}