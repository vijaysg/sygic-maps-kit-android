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

package com.sygic.maps.module.navigation

import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sygic.maps.module.common.MapFragmentWrapper
import com.sygic.maps.module.navigation.component.*
import com.sygic.maps.module.navigation.databinding.LayoutNavigationBinding
import com.sygic.maps.module.navigation.di.DaggerNavigationComponent
import com.sygic.maps.module.navigation.di.NavigationComponent
import com.sygic.maps.module.navigation.listener.OnInfobarButtonsClickListener
import com.sygic.maps.module.navigation.listener.OnInfobarButtonsClickListenerWrapper
import com.sygic.maps.module.navigation.types.SignpostType
import com.sygic.maps.module.navigation.viewmodel.NavigationFragmentViewModel
import com.sygic.maps.uikit.viewmodels.common.regional.units.DistanceUnit
import com.sygic.maps.uikit.viewmodels.navigation.infobar.InfobarViewModel
import com.sygic.maps.uikit.viewmodels.navigation.preview.RoutePreviewControlsViewModel
import com.sygic.maps.uikit.viewmodels.navigation.signpost.FullSignpostViewModel
import com.sygic.maps.uikit.viewmodels.navigation.signpost.SimplifiedSignpostViewModel
import com.sygic.maps.uikit.views.common.extensions.*
import com.sygic.maps.uikit.views.common.toast.InfoToastComponent
import com.sygic.maps.uikit.views.navigation.actionmenu.ActionMenuBottomDialogFragment
import com.sygic.maps.uikit.views.navigation.actionmenu.data.ActionMenuData
import com.sygic.maps.uikit.views.navigation.actionmenu.listener.ActionMenuItemClickListener
import com.sygic.maps.uikit.views.navigation.infobar.Infobar
import com.sygic.maps.uikit.views.navigation.preview.RoutePreviewControls
import com.sygic.maps.uikit.views.navigation.signpost.FullSignpostView
import com.sygic.maps.uikit.views.navigation.signpost.SimplifiedSignpostView
import com.sygic.sdk.route.RouteInfo

const val NAVIGATION_FRAGMENT_TAG = "navigation_fragment_tag"
internal const val KEY_DISTANCE_UNITS = "distance_units"
internal const val KEY_SIGNPOST_ENABLED = "signpost_enabled"
internal const val KEY_SIGNPOST_TYPE = "signpost_type"
internal const val KEY_PREVIEW_CONTROLS_ENABLED = "preview_controls_enabled"
internal const val KEY_PREVIEW_MODE = "preview_mode"
internal const val KEY_INFOBAR_ENABLED = "infobar_enabled"
internal const val KEY_ROUTE_INFO = "route_info"

/**
 * A *[NavigationFragment]* is the core component for any navigation operation. It can be easily used for the navigation
 * purposes. By setting the [routeInfo] object will start the navigation process. Any pre build-in element such as
 * [FullSignpostView], [SimplifiedSignpostView], [Infobar], [CurrentSpeed] or [SpeedLimit] may be activated or deactivated and styled.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class NavigationFragment : MapFragmentWrapper<NavigationFragmentViewModel>(), OnInfobarButtonsClickListenerWrapper {

    override lateinit var fragmentViewModel: NavigationFragmentViewModel
    private lateinit var routePreviewControlsViewModel: RoutePreviewControlsViewModel
    private lateinit var infobarViewModel: InfobarViewModel

    override val infobarButtonsClickListenerProvider: LiveData<OnInfobarButtonsClickListener> = MutableLiveData()

    override fun executeInjector() =
        injector<NavigationComponent, NavigationComponent.Builder>(DaggerNavigationComponent.builder()) { it.inject(this) }

    /**
     * A *[distanceUnit]* defines all available [DistanceUnit]'s type.
     *
     * [DistanceUnit.KILOMETERS] (default) -> Kilometers/meters are used as the distance unit.
     *
     * [DistanceUnit.MILES_YARDS] -> Miles/yards are used as the distance unit.
     *
     * [DistanceUnit.MILES_FEETS] -> Miles/feets are used as the distance unit.
     */
    var distanceUnit: DistanceUnit
        get() = if (::fragmentViewModel.isInitialized) {
            fragmentViewModel.distanceUnit
        } else arguments.getParcelableValue(KEY_DISTANCE_UNITS) ?: DISTANCE_UNITS_DEFAULT_VALUE
        set(value) {
            arguments = Bundle(arguments).apply { putParcelable(KEY_DISTANCE_UNITS, value) }
            if (::fragmentViewModel.isInitialized) {
                fragmentViewModel.distanceUnit = value
            }
        }

    /**
     * A *[signpostEnabled]* modifies the SignpostView ([FullSignpostView] or [SimplifiedSignpostView]) visibility.
     *
     * @param [Boolean] true to enable the SignpostView, false otherwise.
     *
     * @return whether the SignpostView is on or off.
     */
    var signpostEnabled: Boolean
        get() = if (::fragmentViewModel.isInitialized) {
            fragmentViewModel.signpostEnabled.value!!
        } else arguments.getBoolean(KEY_SIGNPOST_ENABLED, SIGNPOST_ENABLED_DEFAULT_VALUE)
        set(value) {
            arguments = Bundle(arguments).apply { putBoolean(KEY_SIGNPOST_ENABLED, value) }
            if (::fragmentViewModel.isInitialized) {
                fragmentViewModel.signpostEnabled.value = value
            }
        }

    /**
     * A *[previewMode]* modifies whether the preview mode is on or off.
     *
     * @param [Boolean] true to enable the [previewMode], false otherwise.
     *
     * @return whether the [previewMode] is on or off.
     */
    var previewMode: Boolean
        get() = if (::fragmentViewModel.isInitialized) {
            fragmentViewModel.previewMode.value!!
        } else arguments.getBoolean(KEY_PREVIEW_MODE, PREVIEW_MODE_DEFAULT_VALUE)
        set(value) {
            arguments = Bundle(arguments).apply { putBoolean(KEY_PREVIEW_MODE, value) }
            if (::fragmentViewModel.isInitialized) {
                fragmentViewModel.previewMode.value = value
            }
        }

    /**
     * A *[previewControlsEnabled]* modifies the [RoutePreviewControls] visibility.
     *
     * @param [Boolean] true to enable the [RoutePreviewControls], false otherwise.
     *
     * @return whether the [RoutePreviewControls] is on or off.
     */
    var previewControlsEnabled: Boolean
        get() = if (::fragmentViewModel.isInitialized) {
            fragmentViewModel.previewControlsEnabled.value!!
        } else arguments.getBoolean(KEY_PREVIEW_CONTROLS_ENABLED, PREVIEW_CONTROLS_ENABLED_DEFAULT_VALUE)
        set(value) {
            arguments = Bundle(arguments).apply { putBoolean(KEY_PREVIEW_CONTROLS_ENABLED, value) }
            if (::fragmentViewModel.isInitialized) {
                fragmentViewModel.previewControlsEnabled.value = value
            }
        }

    /**
     * A *[infobarEnabled]* modifies the [Infobar] view visibility.
     *
     * @param [Boolean] true to enable the [Infobar] view, false otherwise.
     *
     * @return whether the [Infobar] view is on or off.
     */
    var infobarEnabled: Boolean
        get() = if (::fragmentViewModel.isInitialized) {
            fragmentViewModel.infobarEnabled.value!!
        } else arguments.getBoolean(KEY_INFOBAR_ENABLED, INFOBAR_ENABLED_DEFAULT_VALUE)
        set(value) {
            arguments = Bundle(arguments).apply { putBoolean(KEY_INFOBAR_ENABLED, value) }
            if (::fragmentViewModel.isInitialized) {
                fragmentViewModel.infobarEnabled.value = value
            }
        }

    /**
     * If not-null *[routeInfo]* is defined, then it will be used as an navigation routeInfo.
     *
     * @param [RouteInfo] route info object to be processed.
     *
     * @return [RouteInfo] the current route info value or `null` if not yet defined.
     */
    var routeInfo: RouteInfo?
        get() = if (::fragmentViewModel.isInitialized) {
            fragmentViewModel.routeInfo.value
        } else arguments.getParcelableValue(KEY_ROUTE_INFO)
        set(value) {
            arguments = Bundle(arguments).apply { putParcelable(KEY_ROUTE_INFO, value) }
            if (::fragmentViewModel.isInitialized && value != null) {
                fragmentViewModel.routeInfo.value = value
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentViewModel = viewModelOf(NavigationFragmentViewModel::class.java, arguments).apply {
            this.infoToastObservable.observe(
                this@NavigationFragment,
                Observer<InfoToastComponent> { showInfoToast(it) })
            this.actionMenuShowObservable.observe(
                this@NavigationFragment,
                Observer<ActionMenuData> { showActionMenu(it) })
            this.actionMenuHideObservable.observe(
                this@NavigationFragment,
                Observer<Any> { hideActionMenu() })
            this.actionMenuItemClickListenerObservable.observe(
                this@NavigationFragment,
                Observer<ActionMenuItemClickListener> { setActionMenuItemClickListener(it) })
            this.activityFinishObservable.observe(
                this@NavigationFragment,
                Observer<Any> { finish() })
        }
        infobarViewModel = viewModelOf(InfobarViewModel::class.java)
        routePreviewControlsViewModel = viewModelOf(RoutePreviewControlsViewModel::class.java)
        lifecycle.addObserver(fragmentViewModel)
        lifecycle.addObserver(routePreviewControlsViewModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        LayoutNavigationBinding.inflate(inflater, container, false).apply {
            navigationFragmentViewModel = fragmentViewModel
            infobarViewModel = this@NavigationFragment.infobarViewModel
            routePreviewControlsViewModel = this@NavigationFragment.routePreviewControlsViewModel
            lifecycleOwner = this@NavigationFragment

            signpostViewViewStub.setOnInflateListener { _, view ->
                DataBindingUtil.bind<ViewDataBinding>(view)?.let {
                    it.setVariable(
                        BR.signpostViewModel, when (view) {
                            is FullSignpostView -> viewModelOf(FullSignpostViewModel::class.java)
                            is SimplifiedSignpostView -> viewModelOf(SimplifiedSignpostViewModel::class.java)
                            else -> throw IllegalArgumentException("Unknown view in the SignpostView viewStub.")
                        }
                    )
                    it.lifecycleOwner = this@NavigationFragment
                }
            }

            with(root as ViewGroup) {
                super.onCreateView(inflater, this, savedInstanceState)?.let { addView(it, 0) }
            }
        }.root

    private fun showActionMenu(actionMenuData: ActionMenuData) {
        ActionMenuBottomDialogFragment.newInstance(actionMenuData).apply {
            itemClickListener = fragmentViewModel.actionMenuItemClickListener
        }.show(fragmentManager, ActionMenuBottomDialogFragment.TAG)
    }

    private fun setActionMenuItemClickListener(listener: ActionMenuItemClickListener) {
        fragmentManager?.findFragmentByTag(ActionMenuBottomDialogFragment.TAG)?.let { fragment ->
            (fragment as ActionMenuBottomDialogFragment).itemClickListener = listener
        }
    }

    private fun hideActionMenu() {
        fragmentManager?.findFragmentByTag(ActionMenuBottomDialogFragment.TAG)?.let { fragment ->
            (fragment as DialogFragment).dismiss()
        }
    }

    /**
     * Register a custom callback to be invoked when a click to the infobar button has been made.
     *
     * @param onClickListener [OnInfobarButtonsClickListener] callback to invoke on infobar button click.
     */
    fun setOnInfobarButtonsClickListener(onClickListener: OnInfobarButtonsClickListener?) {
        infobarButtonsClickListenerProvider.asMutable().value = onClickListener
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycle.removeObserver(fragmentViewModel)
        lifecycle.removeObserver(routePreviewControlsViewModel)
    }

    override fun resolveAttributes(attributes: AttributeSet) {
        with(requireContext().obtainStyledAttributes(attributes, R.styleable.NavigationFragment)) {
            if (hasValue(R.styleable.NavigationFragment_sygic_navigation_distanceUnit)) {
                distanceUnit = DistanceUnit.atIndex(
                    getInt(
                        R.styleable.NavigationFragment_sygic_navigation_distanceUnit,
                        DISTANCE_UNITS_DEFAULT_VALUE.ordinal
                    )
                )
            }
            if (hasValue(R.styleable.NavigationFragment_sygic_signpost_enabled)) {
                signpostEnabled =
                    getBoolean(
                        R.styleable.NavigationFragment_sygic_signpost_enabled,
                        SIGNPOST_ENABLED_DEFAULT_VALUE
                    )
            }
            if (hasValue(R.styleable.NavigationFragment_sygic_signpost_type)) {
                arguments = Bundle(arguments).apply {
                    putParcelable(
                        KEY_SIGNPOST_TYPE, SignpostType.atIndex(
                            getInt(
                                R.styleable.NavigationFragment_sygic_signpost_type,
                                SIGNPOST_TYPE_DEFAULT_VALUE.ordinal
                            )
                        )
                    )
                }
            }
            if (hasValue(R.styleable.NavigationFragment_sygic_navigation_previewMode)) {
                previewMode =
                    getBoolean(
                        R.styleable.NavigationFragment_sygic_navigation_previewMode,
                        PREVIEW_MODE_DEFAULT_VALUE
                    )
            }
            if (hasValue(R.styleable.NavigationFragment_sygic_previewControls_enabled)) {
                previewControlsEnabled =
                    getBoolean(
                        R.styleable.NavigationFragment_sygic_previewControls_enabled,
                        PREVIEW_CONTROLS_ENABLED_DEFAULT_VALUE
                    )
            }
            if (hasValue(R.styleable.NavigationFragment_sygic_infobar_enabled)) {
                infobarEnabled =
                    getBoolean(
                        R.styleable.NavigationFragment_sygic_infobar_enabled,
                        INFOBAR_ENABLED_DEFAULT_VALUE
                    )
            }

            recycle()
        }
    }
}
