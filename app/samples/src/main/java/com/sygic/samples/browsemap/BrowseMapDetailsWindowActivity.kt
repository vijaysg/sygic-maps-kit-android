package com.sygic.samples.browsemap

import android.os.Bundle
import com.sygic.maps.module.browsemap.BrowseMapFragment
import com.sygic.samples.browsemap.detail.CustomDetailsViewFactory
import com.sygic.maps.uikit.viewmodels.common.sdk.mapobject.MapMarker
import com.sygic.samples.R
import com.sygic.samples.app.activities.CommonSampleActivity

class BrowseMapDetailsWindowActivity : CommonSampleActivity() {

    override val wikiModulePath: String = "Module-Browse-Map#browse-map---details-view"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_browsemap_details_window)

        val browseMapFragment = supportFragmentManager.findFragmentById(R.id.browseMapFragment) as BrowseMapFragment
        browseMapFragment.addMapMarkers(
            listOf(
                MapMarker(48.143489, 17.150560),
                MapMarker(48.165561, 17.139550),
                MapMarker(48.155028, 17.155674),
                MapMarker(48.141797, 17.097001),
                MapMarker.Builder()
                    .coordinates(48.162805, 17.101621)
                    .title("My Marker 1")
                    .build(),
                MapMarker.Builder()
                    .coordinates(48.134756, 17.127729)
                    .title("My Marker 2")
                    .build()
            )
        )

        browseMapFragment.setDetailsViewFactory(CustomDetailsViewFactory())
    }
}