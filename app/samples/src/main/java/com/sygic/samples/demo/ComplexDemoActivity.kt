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

package com.sygic.samples.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sygic.maps.module.browsemap.BROWSE_MAP_FRAGMENT_TAG
import com.sygic.maps.module.browsemap.BrowseMapFragment
import com.sygic.maps.module.common.extensions.*
import com.sygic.maps.module.navigation.NAVIGATION_FRAGMENT_TAG
import com.sygic.maps.module.navigation.NavigationFragment
import com.sygic.maps.uikit.viewmodels.common.sdk.skin.VehicleSkin
import com.sygic.maps.uikit.views.common.extensions.isPermissionNotGranted
import com.sygic.maps.uikit.views.common.extensions.longToast
import com.sygic.maps.uikit.views.common.extensions.requestPermission
import com.sygic.samples.R
import com.sygic.samples.app.activities.CommonSampleActivity
import com.sygic.samples.demo.states.BrowseMapDemoDefaultState
import com.sygic.samples.demo.viewmodels.ComplexDemoActivityViewModel
import com.sygic.samples.utils.getLastValidLocation
import com.sygic.samples.utils.getPrimaryRoute
import com.sygic.sdk.position.GeoCoordinates
import com.sygic.sdk.route.Route
import com.sygic.sdk.route.RoutePlan
import com.sygic.sdk.route.RoutingOptions
import kotlinx.android.synthetic.main.activity_complex_demo.*

private const val REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION = 7001
private const val REQUEST_CODE_GOOGLE_API_CLIENT = 7002
private const val REQUEST_CODE_SETTING_ACTIVITY = 7003

class ComplexDemoActivity : CommonSampleActivity() {

    override val wikiModulePath: String? = null

    private lateinit var browseMapFragment: BrowseMapFragment
    private lateinit var viewModel: ComplexDemoActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_complex_demo)

        viewModel = ViewModelProviders.of(this).get(ComplexDemoActivityViewModel::class.java).apply {
            hidePlaceDetailObservable.observe(this@ComplexDemoActivity, Observer { browseMapFragment.hidePlaceDetail() })
            computePrimaryRouteObservable.observe(this@ComplexDemoActivity, Observer { createRoutePlanAndComputeRoute(it) })
            restoreDefaultStateObservable.observe(
                this@ComplexDemoActivity,
                Observer { BrowseMapDemoDefaultState.setTo(browseMapFragment) }
            )
            showPlaceDetailObservable.observe(
                this@ComplexDemoActivity,
                Observer { browseMapFragment.showPlaceDetail(it.first, it.second) }
            )
            routeComputeProgressVisibilityObservable.observe(
                this@ComplexDemoActivity,
                Observer { routeComputeProgressBarContainer.visibility = it }
            )
        }

        browseMapFragment = if (savedInstanceState == null) {
            placeBrowseMapFragment().apply { BrowseMapDemoDefaultState.setTo(this) }
        } else {
            supportFragmentManager.findFragmentByTag(BROWSE_MAP_FRAGMENT_TAG) as BrowseMapFragment
        }

        browseMapFragment.setOnMapClickListener(viewModel.onMapClickListener)
        browseMapFragment.setSearchConnectionProvider(viewModel.searchModuleConnectionProvider)
    }

    private fun placeBrowseMapFragment() =
        BrowseMapFragment().also {
            supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, it, BROWSE_MAP_FRAGMENT_TAG)
                ?.runOnCommit {
                    viewModel.mapDataModel = it.mapDataModel
                    viewModel.cameraDataModel = it.cameraDataModel
                }
                ?.commit()
        }

    private fun placeNavigationFragment(route: Route) =
        NavigationFragment().also {
            it.setVehicleSkin(VehicleSkin.CAR)
            it.route = route
            supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, it, NAVIGATION_FRAGMENT_TAG)
                ?.addToBackStack(null)
                ?.commit()
        }

    private fun createRoutePlanAndComputeRoute(destination: GeoCoordinates) {
        requestLastValidLocation { lastValidLocation ->
            val routePlan = RoutePlan().apply {
                setStart(lastValidLocation)
                setDestination(destination)
                routingOptions = RoutingOptions().apply {
                    transportMode = RoutingOptions.TransportMode.Car
                    routingType = RoutingOptions.RoutingType.Economic
                }
            }

            routePlan.getPrimaryRoute {
                routeComputeProgressBarContainer.visibility = View.GONE
                placeNavigationFragment(it)
            }
        }
    }

    private fun requestLastValidLocation(currentLocationCallback: (GeoCoordinates) -> Unit) {
        if (isPermissionNotGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION
            )
            return
        }

        if (isGpsNotEnabled()) {
            if (isGooglePlayServicesAvailable()) {
                createGoogleApiLocationRequest(REQUEST_CODE_GOOGLE_API_CLIENT)
            } else {
                showGenericNoGpsDialog(REQUEST_CODE_SETTING_ACTIVITY)
            }
            return
        }

        getLastValidLocation(currentLocationCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    viewModel.targetPosition?.let { createRoutePlanAndComputeRoute(it) }
                } else {
                    longToast("Sorry, location permission is needed!")
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_SETTING_ACTIVITY, REQUEST_CODE_GOOGLE_API_CLIENT -> {
                if (isGpsEnabled()) {
                    viewModel.targetPosition?.let { createRoutePlanAndComputeRoute(it) }
                } else {
                    longToast("GPS module is not enabled :(")
                    finish()
                }
            }
        }
    }
}