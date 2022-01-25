/*
 * Copyright 2021 Surf LLC
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
package ru.surfstudio.compose.routing

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
    val controller: NavHostController,
    val backPressedDispatcher: OnBackPressedDispatcher
) : DefaultLifecycleObserver {

    /**
     * Lifecycle owner
     */
    var lifecycleOwner: LifecycleOwner? = null
        private set

    /**
     * Save start destination
     */
    var startDestination: NavDestination? = null
        private set

    /**
     * Save back destination
     */
    private var backDestination: NavDestination? = null

    /**
     * Data flow for back with data
     */
    private val listListener: MutableMap<String, Any> = mutableMapOf()

    /**
     * CoroutineScope for pager
     */
    private var scope: CoroutineScope? = null

    /**
     * Save count open start destination
     */
    private var firstDestinationCount: Int = 0

    /**
     * Change destination direction
     */
    private var isBack: Boolean = false

    /**
     * Index skip
     */
    private var skipOnBackPressPager: List<Int> = listOf()

    /**
     * Navigation [PagerState]
     */
    private var pager: PagerState? = null

    /**
     * State navigation [PagerState]
     */
    private var pagerEnable: Boolean = true

    /**
     * State index page
     */
    private var pagerIndex: Int = 0

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
            clearPager()
            // add start destination
            if (startDestination == null) {
                startDestination = destination
                firstDestinationCount = 0
            }
            // change counter open start destination
            if (startDestination?.route == destination.route && !isBack) {
                firstDestinationCount++
            }
            // save back destination
            backDestination = currentDestinationMutableFlow.value
            // save current destination
            currentDestinationMutableFlow.value = destination
            // disable destination direction
            isBack = false
        }
    }

    init {
        // add listener changed destination
        controller.addOnDestinationChangedListener(callback)
        // add lifecycle
        lifecycle.addObserver(this)
    }

    /**
     * Clear data pager
     */
    private fun clearPager() {
        pager = null
        scope = null
        pagerIndex = 0
        skipOnBackPressPager = listOf()
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

    /**
     * Check is navigation stack empty
     */
    fun hasEnabledCallbacks(): Boolean {
        // check pager
        if (pager != null && pagerEnable && pager!!.currentPage > 0) {
            return true
        }
        // check root destination
        if (startDestination?.route == currentDestinationMutableFlow.value?.route && firstDestinationCount <= 1) {
            return false
        }
        // check BackPressedDispatcher
        return backPressedDispatcher.hasEnabledCallbacks()
    }

    /**
     * Step to back on navigation or pager
     */
    fun onBackPressed() {
        pager?.let {
            if (!it.isScrollInProgress) {
                if (it.currentPage > 0 && pagerEnable) {
                    scope?.launch {
                        val index = it.currentPage - 1
                        if (skipOnBackPressPager.contains(index) && index - 1 >= 0) {
                            it.animateScrollToPage(index - 1)
                        } else {
                            it.animateScrollToPage(index)
                        }
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
        val route by remember { mutableStateOf(currentDestination?.route.orEmpty()) }
        if (!listListener.containsKey(route)) {
            listListener[route] = MutableStateFlow(value)
        }
        return (listListener[route] as MutableStateFlow<T?>).asStateFlow()
    }

    /**
     * Step to back on navigation with data
     */
    fun <T> onBackPressed(data: T) {
        listListener[backDestination?.route]?.let {
            (it as MutableStateFlow<T?>).value = data
        }
        onBackPressed()
    }

    /**
     * Press back [OnBackPressedDispatcher] with counter
     */
    private fun backPressed() {
        isBack = true
        // change counter open start destination
        if (startDestination?.route == currentDestinationMutableFlow.value?.route) {
            firstDestinationCount--
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
    fun toRoutePopStack(route: () -> Unit) {
        currentDestinationFlow.value?.id?.let { id ->
            controller.popBackStack(id, true)
            route.invoke()
        }
    }

    /**
     * Clear all stack and open screens
     */
    fun toRoutePopAllStack(route: () -> Unit) {
        toRoutePopAllStack(routes = listOf(route).toTypedArray())
    }

    /**
     * Clear all stack and open screens
     */
    fun toRoutePopAllStack(vararg routes: () -> Unit) {
        if (routes.isNotEmpty()) {
            // clear stack
            startDestination?.let { des ->
                (0..firstDestinationCount).forEach { _ ->
                    controller.popBackStack(des.id, true)
                }
            }
            // clear first destination
            startDestination = null
            // emit routes
            routes.forEach {
                it.invoke()
            }
        }
    }

    /**
     * Set pager [PagerState] and callback change
     */
    fun setPager(scope: CoroutineScope, state: PagerState, vararg skip: Int) {
        if (pager == null) {
            pager = state
            this.scope = scope
            skipOnBackPressPager = skip.toList()
        }
    }

    /**
     * Add listen change pager
     */
    fun listenChangePager(change: (page: Int, isBack: Boolean) -> Unit = { _, _ -> }) {
        pager?.let {
            scope?.launch {
                snapshotFlow { it.currentPage }.collect {
                    change.invoke(it, pagerIndex > it)
                    pagerIndex = it
                }
            }
        }
    }

    /**
     * Enable navigation pager
     */
    fun enablePager() {
        pagerEnable = true
    }

    /**
     * Disable navigation pager
     */
    fun disablePager() {
        pagerEnable = false
    }

    companion object {
        /**
         * Save current destination
         */
        private var currentDestinationMutableFlow: MutableStateFlow<NavDestination?> =
            MutableStateFlow(null)

        /**
         * Get current destination flow
         */
        val currentDestinationFlow get() = currentDestinationMutableFlow.asStateFlow()

        /**
         * Get current destination
         */
        val currentDestination get() = currentDestinationMutableFlow.value
    }
}
