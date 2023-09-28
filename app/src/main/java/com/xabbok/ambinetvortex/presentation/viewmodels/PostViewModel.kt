package com.xabbok.ambinetvortex.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
    //private val scope: CoroutineScope = MainScope()
) : ViewModel() {

}