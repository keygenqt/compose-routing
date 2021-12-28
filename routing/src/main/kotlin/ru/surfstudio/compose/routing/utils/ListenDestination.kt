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
 
package ru.surfstudio.compose.routing.utils

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import ru.surfstudio.compose.routing.extension.AddListenChangeNavigation

/**
 * For listen change route
 */
class ListenDestination {
    companion object {

        /**
         * Is action clear stack
         */
        private var IS_ENABLE_CLEAR: Boolean = false

        /**
         * Start graph destination route
         */
        private lateinit var START_DESTINATION: String

        /**
         * Destination root
         */
        private var ROOT_DESTINATION: String? = null

        /**
         * Destination action
         */
        private var ACTION_DESTINATION: String? = null

        /**
         * Init ListenDestination listen change navigation
         */
        @Composable
        fun Init(
            startDestination: String,
            controller: NavHostController,
            onChange: (destination: NavDestination) -> Unit
        ) {
            START_DESTINATION = startDestination
            controller.AddListenChangeNavigation {
                if (ROOT_DESTINATION == null) {
                    ROOT_DESTINATION = it.route
                }
                ACTION_DESTINATION = it.route
                onChange.invoke(it)
            }
        }

        /**
         * Is clear disable callback
         */
        fun isClearStart(): Boolean {
            return IS_ENABLE_CLEAR
        }

        /**
         * Get last destination
         */
        fun getActionDestination(): String? {
            return ACTION_DESTINATION
        }

        /**
         * Clear stack navigation
         */
        fun clearStack(
            dispatcher: OnBackPressedDispatcher,
        ): String {
            val rootRoute = ROOT_DESTINATION
            IS_ENABLE_CLEAR = true
            while (dispatcher.hasEnabledCallbacks()) {
                dispatcher.onBackPressed()
            }
            IS_ENABLE_CLEAR = false
            ROOT_DESTINATION = null
            return rootRoute ?: START_DESTINATION
        }

        /**
         * If not root navigation
         */
        fun hasStack(): Boolean {
            return ACTION_DESTINATION != ROOT_DESTINATION && ACTION_DESTINATION != null
        }
    }
}
