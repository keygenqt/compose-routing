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

interface NavigationRoute {
    /** Unique static route of screen, used in `composable`. */
    val route: String

    /**
     * Unique dynamic route of screen which depends on root, used in `composable`.
     * This configuration is useful for nested graphs in order to reuse one screen in different graphs
     * making the whole navigation more flexible.
     */
    fun configureRoute(root: String) = "$root/$route"
}

interface NavigationRouteArgument1 : NavigationRoute {
    val argument0: String

    /** Navigate to screen with given args */
    fun routeWithArguments(argument0: String?) = route
        .replace("{${this.argument0}}", argument0 ?: "")

    /** Navigate to screen with given args when the root is needed */
    fun routeWithRootAndArguments(root: String, argument0: String?) =
        "$root/${routeWithArguments(argument0)}"
}

interface NavigationRouteArgument2 : NavigationRouteArgument1 {
    val argument1: String

    /** Navigate to screen with given args */
    fun routeWithArguments(
        argument0: String?,
        argument1: String?,
    ) = routeWithArguments(argument0)
        .replace("{${this.argument1}}", argument1 ?: "")

    /** Navigate to screen with given args when the root is needed */
    fun routeWithRootAndArguments(root: String, argument0: String?, argument1: String?) =
        "$root/${routeWithArguments(argument0, argument1)}"
}

interface NavigationRouteArgument3 : NavigationRouteArgument2 {
    val argument2: String

    /** Navigate to screen with given args */
    fun routeWithArguments(
        argument0: String?,
        argument1: String?,
        argument2: String?,
    ) = routeWithArguments(argument0, argument1)
        .replace("{${this.argument2}}", argument2 ?: "")

    /** Navigate to screen with given args when the root is needed */
    fun routeWithRootAndArguments(
        root: String,
        argument0: String?,
        argument1: String?,
        argument2: String?
    ) = "$root/${routeWithArguments(argument0, argument1, argument2)}"
}

interface NavigationRouteArgument4 : NavigationRouteArgument3 {
    val argument3: String

    /** Navigate to screen with given args */
    fun routeWithArguments(
        argument0: String?,
        argument1: String?,
        argument2: String?,
        argument3: String?,
    ) = routeWithArguments(argument0, argument1, argument2)
        .replace("{${this.argument3}}", argument3 ?: "")

    /** Navigate to screen with given args when the root is needed */
    fun routeWithRootAndArguments(
        root: String,
        argument0: String?,
        argument1: String?,
        argument2: String?,
        argument3: String?
    ) = "$root/${routeWithArguments(argument0, argument1, argument2, argument3)}"
}

interface NavigationRouteArgument5 : NavigationRouteArgument4 {
    val argument4: String

    /** Navigate to screen with given args */
    fun routeWithArguments(
        argument0: String?,
        argument1: String?,
        argument2: String?,
        argument3: String?,
        argument4: String?,
    ) = routeWithArguments(argument0, argument1, argument2, argument3)
        .replace("{${this.argument4}}", argument4 ?: "")

    /** Navigate to screen with given args when the root is needed */
    fun routeWithRootAndArguments(
        root: String,
        argument0: String?,
        argument1: String?,
        argument2: String?,
        argument3: String?,
        argument4: String?
    ) = "$root/${routeWithArguments(argument0, argument1, argument2, argument3, argument4)}"
}