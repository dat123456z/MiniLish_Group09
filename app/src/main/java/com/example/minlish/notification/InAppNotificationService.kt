package com.example.minlish.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InAppNotificationService {
    private val _notificationEvents = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val notificationEvents = _notificationEvents.asSharedFlow()

    fun show(title: String, message: String) {
        _notificationEvents.tryEmit(title to message)
    }
}
