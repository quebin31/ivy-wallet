package com.ivy.wallet.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> {
    return this
}

fun Fragment.args(putArgs: Bundle.() -> Unit): Fragment {
    arguments = Bundle().apply { putArgs() }
    return this
}

fun Fragment.stringArg(key: String): String? {
    return arguments?.getString(key, null)
}

suspend fun <T> ioThread(action: suspend () -> T): T = withContext(Dispatchers.IO) {
    return@withContext action()
}

suspend fun <T> uiThread(action: suspend () -> T): T = withContext(Dispatchers.Main) {
    return@withContext action()
}