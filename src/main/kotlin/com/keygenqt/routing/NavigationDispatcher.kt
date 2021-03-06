/*
 * Copyright 2022 Vitaliy Zarubin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keygenqt.routing

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Navigation dispatcher for routing with pager
 */
@OptIn(ExperimentalPagerApi::class)
class NavigationDispatcher(
    lifecycle: Lifecycle,
    private val controller: NavHostController,
    private val backPressedDispatcher: OnBackPressedDispatcher
) : DefaultLifecycleObserver {

    /**
     * CoroutineScope for pager
     */
    private var _scope: CoroutineScope? = null

    /**
     * Save start destination
     */
    private var _startDestination: NavDestination? = null

    /**
     * Save count open start destination
     */
    private var _firstDestinationCount: Int = 0

    /**
     * Change destination direction
     */
    private var _isBack: Boolean = false

    /**
     * Save current destination
     */
    private var _currentDestination: NavDestination? = null

    /**
     * Save back destination
     */
    private var _backDestination: NavDestination? = null

    /**
     * Navigation [PagerState]
     */
    private var _pager: PagerState? = null

    /**
     * State navigation [PagerState]
     */
    private var _pagerEnable: Boolean = true

    /**
     * Get start destination
     */
    val startDestination get() = _startDestination

    /**
     * Get current destination
     */
    val currentDestination get() = _currentDestination

    /**
     * Lifecycle owner
     */
    private var lifecycleOwner: LifecycleOwner? = null

    /**
     * Data flow for back with data
     */
    private val listListener: MutableMap<String, Any> = mutableMapOf()

    /**
     * Custom navigator callback
     */
    private val navigatorCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    /**
     * Callback listen change destination
     */
    private val callback = NavController.OnDestinationChangedListener { controller, _, _ ->
        controller.currentDestination?.let { destination ->
            // clear pager
            _pager = null
            _scope = null
            // add start destination
            if (_startDestination == null) {
                _startDestination = destination
                _firstDestinationCount = 0
            }
            // change counter open start destination
            if (_startDestination?.route == destination.route && !_isBack) {
                _firstDestinationCount++
            }
            // save back destination
            _backDestination = _currentDestination
            // save current destination
            _currentDestination = destination
            // disable destination direction
            _isBack = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        // save lifecycle owner
        lifecycleOwner = owner
        // add custom callback
        backPressedDispatcher.addCallback(owner, navigatorCallback)
    }

    override fun onPause(owner: LifecycleOwner) {
        // clear lifecycle owner
        lifecycleOwner = null
        // remove callback
        navigatorCallback.remove()
    }

    init {
        // add listener changed destination
        controller.addOnDestinationChangedListener(callback)
        // add lifecycle
        lifecycle.addObserver(this)
    }

    /**
     * Check is navigation stack empty
     */
    fun hasEnabledCallbacks(): Boolean {
        // check pager
        if (_pager != null && _pagerEnable && _pager!!.currentPage > 0) {
            return true
        }
        // check root destination
        if (_startDestination?.route == _currentDestination?.route && _firstDestinationCount <= 1) {
            return false
        }
        // check BackPressedDispatcher
        return backPressedDispatcher.hasEnabledCallbacks()
    }

    /**
     * Step to back on navigation or pager
     */
    fun onBackPressed() {
        _pager?.let {
            if (!it.isScrollInProgress) {
                if (it.currentPage > 0 && _pagerEnable) {
                    _scope?.launch {
                        it.animateScrollToPage(it.currentPage - 1)
                    }
                } else {
                    backPressed()
                }
            }
        } ?: run {
            backPressed()
        }
    }

    /**
     * Create flow for listening
     */
    @Composable
    fun <T> onBackPressedData(value: T? = null): StateFlow<T?> {
        val route by remember { mutableStateOf(currentDestination?.route ?: "") }
        if (!listListener.containsKey(route)) {
             listListener[route] = MutableStateFlow(value)
        }
        return (listListener[route] as MutableStateFlow<T?>).asStateFlow()
    }

    /**
     * Step to back on navigation with data
     */
    fun <T> onBackPressed(data: T) {
        listListener[_backDestination?.route]?.let {
            (it as MutableStateFlow<T?>).value = data
        }
        onBackPressed()
    }

    /**
     * Press back [OnBackPressedDispatcher] with counter
     */
    private fun backPressed() {
        _isBack = true
        // change counter open start destination
        if (_startDestination?.route == _currentDestination?.route) {
            _firstDestinationCount--
        }
        // disable callback
        navigatorCallback.remove()
        // onBackPressed
        backPressedDispatcher.onBackPressed()
        // enable callback
        lifecycleOwner?.let {
            backPressedDispatcher.addCallback(it, navigatorCallback)
        }
    }

    /**
     * Clear stack and open screens
     */
    fun toRoutePopStack(vararg routes: () -> Unit) {
        if (routes.isNotEmpty()) {
            // clear stack
            _startDestination?.let { des ->
                (0.._firstDestinationCount).forEach { _ ->
                    controller.popBackStack(des.id, true)
                }
            }
            // clear first destination
            _startDestination = null
            // emit routes
            routes.forEach {
                it.invoke()
            }
        }
    }

    /**
     * Set pager [PagerState] and callback change
     */
    fun setPager(scope: CoroutineScope, state: PagerState) {
        if (_pager == null) {
            _pager = state
            _scope = scope
        }
    }

    /**
     * Add listen change pager
     */
    fun listenChangePager(scope: CoroutineScope, change: (Int) -> Unit = {}) {
        _pager?.let {
            scope.launch {
                snapshotFlow { it.currentPage }.collect {
                    change.invoke(it)
                }
            }
        }
    }

    /**
     * Enable navigation pager
     */
    fun enablePager() {
        _pagerEnable = true
    }

    /**
     * Disable navigation pager
     */
    fun disablePager() {
        _pagerEnable = false
    }
}
