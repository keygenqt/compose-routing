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
import kotlinx.coroutines.flow.collectLatest
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

    override fun onCreate(owner: LifecycleOwner) {}

    override fun onStart(owner: LifecycleOwner) {}

    override fun onStop(owner: LifecycleOwner) {}

    override fun onDestroy(owner: LifecycleOwner) {}

    /**
     * Lifecycle owner
     */
    var lifecycleOwner: LifecycleOwner? = null
        private set

    /**
     * Data flow for back with data
     */
    private val listListener: MutableMap<String, Any> = mutableMapOf()

    /**
     * CoroutineScope for pager
     */
    private var scope: CoroutineScope? = null

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
     * Custom back press callback
     */
    private var onBackPressedCallback: (() -> Boolean)? = null

    /**
     * Custom back press callback force
     */
    private var onBackPressedCallbackForce: ((NavDestination?) -> Boolean)? = null

    /**
     * Custom navigator callback
     */
    private val navigatorCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }

    /**
     * Save routing list
     */
    private var destinationPathList: MutableList<NavDestination> = mutableListOf()

    /**
     * Get back destination
     */
    val backDestination get() = destinationPathList.take(destinationPathList.size - 1).lastOrNull()

    /**
     * Get first destination
     */
    val startDestination get() = destinationPathList.firstOrNull()

    /**
     * Count first destination for clear all stack
     */
    private val firstDestinationCount get() = destinationPathList.filter { startDestination?.route == it.route }.size

    /**
     * Create flow for listening
     */
    @Composable
    fun <T> backPressedDataFlow(value: T? = null): StateFlow<T?> {
        val route by remember { mutableStateOf(currentDestination?.route.orEmpty()) }
        if (!listListener.containsKey(route)) {
            listListener[route] = MutableStateFlow(value)
        }
        return (listListener[route] as MutableStateFlow<T?>).asStateFlow()
    }

    /**
     * For clear/change navigation data
     */
    fun <T> onBackPressedFlowUpdate(data: T) {
        listListener[backDestination?.route]?.let {
            (it as MutableStateFlow<T?>).value = data
        }
    }

    /**
     * Callback listen change destination
     */
    private val callback = NavController.OnDestinationChangedListener { controller, _, _ ->
        controller.currentDestination?.let { destination ->
            // save path routing
            if (controller.previousBackStackEntry?.destination == null) {
                destinationPathList.clear()
            }
            if (isBack && destinationPathList.isNotEmpty()) {
                destinationPathList.removeLast()
            } else {
                destinationPathList.add(destination)
            }
            // clear data
            clearAllData()
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
     * Clear pager data
     */
    private fun clearPagerData() {
        pager = null
        scope = null
        pagerIndex = 0
        skipOnBackPressPager = listOf()
    }

    /**
     * Clear custom callback
     */
    private fun clearCallbackData() {
        onBackPressedCallback = null
    }

    /**
     * Clear data after change route
     */
    private fun clearAllData() {
        clearPagerData()
        clearCallbackData()
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
     * Step to back on navigation with data
     */
    fun <T> onBackPressed(data: T) {
        listListener[backDestination?.route]?.let {
            (it as MutableStateFlow<T?>).value = data
        }
        onBackPressed()
    }

    /**
     * Step to back route on navigation with data
     */
    fun <T> onBackPressed(route: String?, data: T) {
        destinationPathList.reversed().find { it.route == route }?.let { destination ->
            controller.popBackStack(destination.id, false)
            listListener[destination.route]?.let {
                (it as MutableStateFlow<T?>).value = data
            }
        }
    }

    /**
     * Clear all callback and back
     */
    fun onBackPressedWithClear() {
        clearAllData()
        onBackPressedCallbackForce = null
        onBackPressed()
    }

    /**
     * Step to back on navigation or pager
     */
    fun onBackPressed() {
        // check state callback
        val statePressCallback = onBackPressedCallback == null
                || onBackPressedCallback?.invoke() == false

        // check state callback force
        val statePressCallbackForce = onBackPressedCallbackForce == null
                || onBackPressedCallbackForce?.invoke(currentDestination) == false

        // run back press
        if (statePressCallback && statePressCallbackForce) {
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
    }

    /**
     * Press back [OnBackPressedDispatcher] with counter
     */
    private fun backPressed() {
        isBack = true
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
    fun setListenChangePager(change: (page: Int, isBack: Boolean) -> Unit = { _, _ -> }) {
        pager?.let {
            scope?.launch {
                snapshotFlow { it.currentPage }.collectLatest {
                    change.invoke(it, pagerIndex > it)
                    pagerIndex = it
                }
            }
        }
    }

    /**
     * Clear data pager
     */
    fun removePager() {
        clearPagerData()
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

    /**
     * Check has callback
     */
    fun hasBackPressedCallback(): Boolean {
        return onBackPressedCallback != null
    }

    /**
     * Set on back press custom callback
     */
    fun setOnBackPressedCallback(callback: () -> Boolean) {
        onBackPressedCallback = callback
    }

    /**
     * Remove on back press custom callback
     */
    fun removeOnBackPressedCallback() {
        clearCallbackData()
    }

    /**
     * Check has callback force
     */
    fun hasBackPressedCallbackForce(): Boolean {
        return onBackPressedCallbackForce != null
    }

    /**
     * Set on back press custom callback force
     */
    fun setOnBackPressedCallbackForce(callback: (NavDestination?) -> Boolean) {
        onBackPressedCallbackForce = callback
    }

    /**
     * Remove on back press custom callback force
     */
    fun removeOnBackPressedCallbackForce() {
        onBackPressedCallbackForce = null
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
