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

package com.sygic.maps.module.navigation.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.sygic.maps.module.common.theme.ThemeManager
import com.sygic.maps.module.common.viewmodel.ThemeSupportedViewModel
import com.sygic.maps.module.navigation.*
import com.sygic.maps.module.navigation.component.*
import com.sygic.maps.module.navigation.types.SignpostType
import com.sygic.maps.tools.annotations.Assisted
import com.sygic.maps.tools.annotations.AutoFactory
import com.sygic.maps.uikit.viewmodels.common.location.LocationManager
import com.sygic.maps.uikit.viewmodels.common.navigation.preview.RouteDemonstrationManager
import com.sygic.maps.uikit.viewmodels.common.permission.PermissionsManager
import com.sygic.maps.uikit.viewmodels.common.regional.RegionalManager
import com.sygic.maps.uikit.viewmodels.common.regional.units.DistanceUnit
import com.sygic.maps.uikit.viewmodels.common.sdk.model.ExtendedCameraModel
import com.sygic.maps.uikit.viewmodels.common.sdk.model.ExtendedMapDataModel
import com.sygic.maps.uikit.viewmodels.common.utils.requestLocationAccess
import com.sygic.maps.uikit.views.common.extensions.getBoolean
import com.sygic.maps.uikit.views.common.extensions.getParcelableValue
import com.sygic.maps.uikit.views.common.extensions.isLandscape
import com.sygic.maps.uikit.views.common.extensions.withLatestFrom
import com.sygic.maps.uikit.views.common.utils.UniqueMutableLiveData
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapAnimation
import com.sygic.sdk.map.MapCenter
import com.sygic.sdk.map.MapCenterSettings
import com.sygic.sdk.map.`object`.MapRoute
import com.sygic.sdk.navigation.NavigationManager
import com.sygic.sdk.route.RouteInfo

private const val DEFAULT_NAVIGATION_TILT = 60f
private val PORTRAIT_MAP_CENTER = MapCenter(0.5f, 0.25f)
private val PORTRAIT_MAP_CENTER_SETTING = MapCenterSettings(
    PORTRAIT_MAP_CENTER,
    PORTRAIT_MAP_CENTER,
    MapAnimation.NONE,
    MapAnimation.NONE
)
private val LANDSCAPE_MAP_CENTER = MapCenter(0.75f, 0.3f)
private val LANDSCAPE_MAP_CENTER_SETTING = MapCenterSettings(
    LANDSCAPE_MAP_CENTER,
    LANDSCAPE_MAP_CENTER,
    MapAnimation.NONE,
    MapAnimation.NONE
)

@AutoFactory
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NavigationFragmentViewModel internal constructor(
    app: Application,
    @Assisted arguments: Bundle?,
    themeManager: ThemeManager,
    private val cameraModel: ExtendedCameraModel,
    private val mapDataModel: ExtendedMapDataModel,
    private val regionalManager: RegionalManager,
    private val locationManager: LocationManager,
    private val permissionsManager: PermissionsManager,
    private val navigationManager: NavigationManager,
    private val routeDemonstrationManager: RouteDemonstrationManager
) : ThemeSupportedViewModel(app, themeManager), DefaultLifecycleObserver, NavigationManager.OnRouteChangedListener {

    @LayoutRes
    val signpostLayout: Int
    val signpostEnabled: MutableLiveData<Boolean> = MutableLiveData(SIGNPOST_ENABLED_DEFAULT_VALUE)
    val previewControlsEnabled: MutableLiveData<Boolean> = MutableLiveData(PREVIEW_CONTROLS_ENABLED_DEFAULT_VALUE)
    val lanesViewEnabled: MutableLiveData<Boolean> = MutableLiveData(LANES_VIEW_ENABLED_DEFAULT_VALUE)

    val previewMode: MutableLiveData<Boolean> = MutableLiveData(false)
    val routeInfo: MutableLiveData<RouteInfo> = UniqueMutableLiveData()

    var distanceUnit: DistanceUnit
        get() = regionalManager.distanceUnit.value!!
        set(value) {
            regionalManager.distanceUnit.value = value
        }

    init {
        with(arguments) {
            previewMode.value = getBoolean(KEY_PREVIEW_MODE, PREVIEW_MODE_DEFAULT_VALUE)
            previewControlsEnabled.value = getBoolean(KEY_PREVIEW_CONTROLS_ENABLED, PREVIEW_CONTROLS_ENABLED_DEFAULT_VALUE)
            signpostEnabled.value = getBoolean(KEY_SIGNPOST_ENABLED, SIGNPOST_ENABLED_DEFAULT_VALUE)
            lanesViewEnabled.value = getBoolean(KEY_LANES_VIEW_ENABLED, LANES_VIEW_ENABLED_DEFAULT_VALUE)
            signpostLayout = when (getParcelableValue(KEY_SIGNPOST_TYPE) ?: SIGNPOST_TYPE_DEFAULT_VALUE) {
                SignpostType.FULL -> R.layout.layout_signpost_full_view_stub
                SignpostType.SIMPLIFIED -> R.layout.layout_signpost_simplified_view_stub
            }
            distanceUnit = getParcelableValue(KEY_DISTANCE_UNITS) ?: DISTANCE_UNITS_DEFAULT_VALUE
            getParcelableValue<RouteInfo>(KEY_ROUTE_INFO)?.let { routeInfo.value = it }
        }

        routeInfo.observeForever(::setRouteInfo)
        previewMode.withLatestFrom(routeInfo).observeForever { processRoutePreview(it.first, it.second) }
    }

    override fun onStart(owner: LifecycleOwner) {
        locationManager.positionOnMapEnabled = !previewMode.value!!
        navigationManager.addOnRouteChangedListener(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        cameraModel.mapCenterSettings = if (isLandscape()) LANDSCAPE_MAP_CENTER_SETTING else PORTRAIT_MAP_CENTER_SETTING
    }

    override fun onRouteChanged(routeInfo: RouteInfo?) {
        mapDataModel.removeAllMapRoutes()
        routeInfo?.let {
            this.routeInfo.value = it
            mapDataModel.addMapRoute(MapRoute.from(it).build())
        }
    }

    private fun setRouteInfo(routeInfo: RouteInfo) {
        // set the default navigation camera state
        cameraModel.apply {
            tilt = DEFAULT_NAVIGATION_TILT
            movementMode = Camera.MovementMode.FollowGpsPositionWithAutozoom
            rotationMode = Camera.RotationMode.Vehicle
        }

        // set the new RouteInfo for navigation
        navigationManager.setRouteForNavigation(routeInfo)
    }

    private fun processRoutePreview(previewActive: Boolean, routeInfo: RouteInfo) {
        if (previewActive) {
            // start preview mode
            locationManager.positionOnMapEnabled = false
            routeDemonstrationManager.start(routeInfo)
        } else {
            // stop the previous demonstration first
            routeDemonstrationManager.stop()

            // start navigation mode
            requestLocationAccess(permissionsManager, locationManager) {
                locationManager.positionOnMapEnabled = true
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        locationManager.positionOnMapEnabled = false
        navigationManager.removeOnRouteChangedListener(this)
    }

    override fun onCleared() {
        super.onCleared()

        mapDataModel.removeAllMapRoutes()
        routeDemonstrationManager.destroy()
        navigationManager.stopNavigation()
    }
}
