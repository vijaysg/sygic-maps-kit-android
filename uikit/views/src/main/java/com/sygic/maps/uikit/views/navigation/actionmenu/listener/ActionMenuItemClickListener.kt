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

package com.sygic.maps.uikit.views.navigation.actionmenu.listener

import androidx.annotation.RestrictTo
import androidx.lifecycle.LiveData
import com.sygic.maps.uikit.views.navigation.actionmenu.data.ActionMenuData
import com.sygic.maps.uikit.views.navigation.actionmenu.data.ActionMenuItem

/**
 * Interface definition for a callback to be invoked when a click to the action menu item has been made.
 */
@FunctionalInterface
interface ActionMenuItemClickListener {

    /**
     * Called when a click to the action menu item has been made.
     */
    fun onActionMenuItemClick(actionMenuItem: ActionMenuItem)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionMenuItemsProviderWrapper {
    val actionMenuItemsProvider: LiveData<ProviderComponent>

    data class ProviderComponent(
        val actionMenuData: ActionMenuData,
        val actionMenuItemClickListener: ActionMenuItemClickListener
    )
}