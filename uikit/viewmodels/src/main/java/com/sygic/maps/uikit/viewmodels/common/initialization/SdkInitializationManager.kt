/*
 * Copyright (c) 2019 Sygic a.s. All rights reserved.
 *
 * This project is licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sygic.maps.uikit.viewmodels.common.initialization

import androidx.annotation.RestrictTo
import com.sygic.maps.uikit.views.common.utils.logError
import com.sygic.sdk.SygicEngine

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SdkInitializationManager {

    @InitializationState
    var initializationState: Int

    interface Callback {
        fun onSdkInitialized()
        fun onError(@SygicEngine.OnInitListener.InitError error: Int) {
            val errorType = when (error) {
                SygicEngine.OnInitListener.InitError.InternalInit -> "Internal init"
                SygicEngine.OnInitListener.InitError.Resources -> "Resources"
                else -> "Unknown"
            }
            logError("SDK Initialization failed: $errorType error :(")
        }
    }

    fun initialize(callback: Callback)
    fun initialize(callback: () -> Unit) { initialize(object : Callback { override fun onSdkInitialized() = callback() }) }

    fun isInitialized() = initializationState == InitializationState.INITIALIZED

    fun onReady(block: () -> Unit) = if (isInitialized()) block.invoke() else initialize { block.invoke() }
}