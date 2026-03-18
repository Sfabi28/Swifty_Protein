package com.example.swifty_protein.data

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val onAppBackground: () -> Unit,
    private val onAppForeground: () -> Unit
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onAppBackground()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onAppForeground()
    }
}