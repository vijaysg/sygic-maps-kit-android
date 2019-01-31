package com.sygic.ui.common.sdk.utils

import android.Manifest
import androidx.lifecycle.Observer
import com.sygic.sdk.SygicEngine
import com.sygic.ui.common.sdk.location.LocationManager
import com.sygic.ui.common.sdk.permission.PermissionsManager

fun requestLocationAccess(permissionsManager: PermissionsManager, locationManager: LocationManager, onSuccess: () -> Unit) {
    requestLocationPermission(permissionsManager) {
        locationManager.requestToEnableGps({
            onSuccess()
        })
    }
}

fun requestLocationPermission(permissionsManager: PermissionsManager, onSuccess: () -> Unit) {
    permissionsManager.checkPermissionGranted(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Observer { permissionEnabled ->
            if (!permissionEnabled) {
                permissionsManager.requestPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    object : PermissionsManager.PermissionCallback {
                        override fun onPermissionGranted(permission: String) {
                            SygicEngine.openGpsConnection()
                            onSuccess()
                        }

                        override fun onPermissionDenied(permission: String) {
                            /* Currently do nothing */
                        }
                    })
            } else {
                onSuccess()
            }
        })
}

