package com.notebook.android.model.helpSupport

data class FeedbackData(var status: Int? = null,
                   var error: Boolean,
                   var msg: String? = null) {
}