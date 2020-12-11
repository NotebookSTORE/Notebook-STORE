package com.notebook.android.ui.drawerFrag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DrawerPartVMFactory(
    val drawerPartRepo: DrawerPartRepo
): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DrawerPartVM(drawerPartRepo) as T
    }
}