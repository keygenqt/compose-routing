/**
 * Copyright © 2021 Surf. All rights reserved.
 */
package ru.surfstudio.compose.routing.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavDestination

@Composable
fun NavController.AddListenChangeNavigation(onChange: (destination: NavDestination) -> Unit) {
    DisposableEffect(this) {
        val callback = NavController.OnDestinationChangedListener { controller, _, _ ->
            controller.currentDestination?.let { route ->
                onChange.invoke(route)
            }
        }
        addOnDestinationChangedListener(callback)
        // remove the navController on dispose (i.e. when the composable is destroyed)
        onDispose {
            removeOnDestinationChangedListener(callback)
        }
    }
}
