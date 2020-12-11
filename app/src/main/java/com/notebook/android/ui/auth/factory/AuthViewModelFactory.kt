package com.notebook.android.ui.auth.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notebook.android.ui.auth.repository.AuthRepository
import com.notebook.android.ui.auth.viewmodel.AuthViewModel

class AuthViewModelFactory(
    val authRepository: AuthRepository
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository) as T
    }
}