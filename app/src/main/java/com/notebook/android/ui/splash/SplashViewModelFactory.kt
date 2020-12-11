package com.notebook.android.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.auth.viewmodel.AuthViewModel

class SplashViewModelFactory(
    val splashRepository: SplashRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SplashVM(splashRepository) as T
    }
}