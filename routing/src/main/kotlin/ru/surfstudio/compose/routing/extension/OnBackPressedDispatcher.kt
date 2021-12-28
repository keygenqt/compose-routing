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
 
package ru.surfstudio.compose.routing.extension

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import ru.surfstudio.compose.routing.utils.ListenDestination

/**
 * BackPressedDispatcher with [OnBackPressedCallback] by compose status
 *
 * @param enable state handleOnBackPressed
 * @param emit lambda for emit is enable
 *
 * @author Vitaliy Zarubin
 */
@Composable
fun NavigationDispatcherCallback(
    dispatcher: OnBackPressedDispatcher?,
    lifecycleOwner: LifecycleOwner?,
    enable: Boolean,
    emit: () -> Unit
) {
    val navigatorButton = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (ListenDestination.isClearStart()) {
                    remove()
                } else {
                    emit.invoke()
                }
            }
        }
    }
    navigatorButton.isEnabled = enable
    lifecycleOwner?.let {
        DisposableEffect(lifecycleOwner) {
            dispatcher?.addCallback(lifecycleOwner, navigatorButton)
            onDispose {
                navigatorButton.remove()
            }
        }
    }
}

/**
 * BackPressedDispatcher for [HorizontalPager] by compose status
 *
 * @param enable state handleOnBackPressed
 * @param emit lambda for emit is enable
 *
 * @author Vitaliy Zarubin
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun NavigationDispatcherCallbackPager(
    dispatcher: OnBackPressedDispatcher?,
    lifecycleOwner: LifecycleOwner?,
    state: PagerState,
    enable: Boolean,
    emit: () -> Unit
) = NavigationDispatcherCallback(
    dispatcher = dispatcher,
    lifecycleOwner = lifecycleOwner,
    enable = enable || state.isScrollInProgress,
    emit = {
        if (!state.isScrollInProgress) {
            emit.invoke()
        }
    }
)
