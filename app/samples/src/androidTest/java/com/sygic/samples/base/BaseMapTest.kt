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

package com.sygic.samples.base

import android.Manifest
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.sygic.samples.app.activities.CommonSampleActivity
import com.sygic.samples.base.idling.MapReadyIdlingResource
import com.sygic.samples.base.rules.DisableAnimationsRule
import org.junit.After
import org.junit.Before
import org.junit.Rule

open class BaseMapTest(activityClass: Class<out CommonSampleActivity>) {

    @get:Rule
    val disableAnimationsRule = DisableAnimationsRule()

    @get:Rule
    val activityRule = ActivityTestRule(activityClass)

    @get:Rule
    val grantLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    private var mapReadyIdlingResource: IdlingResource? = null

    protected val activity: CommonSampleActivity
        get() = activityRule.activity

    @Before
    fun registerIdlingResource() {
        mapReadyIdlingResource = MapReadyIdlingResource(activity)

        IdlingRegistry.getInstance().register(mapReadyIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        mapReadyIdlingResource?.let { IdlingRegistry.getInstance().unregister(it) }
    }
}