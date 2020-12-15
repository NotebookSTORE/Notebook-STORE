package com.notebook.android.model

open class ActivityState

object InitialState : ActivityState()
object ProgressState : ActivityState()
object SuccessState : ActivityState()
data class ErrorState(val exception: Exception) : ActivityState()