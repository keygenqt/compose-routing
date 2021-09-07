Compose Navigator Routing
===================

![picture](data/just-image.png)

### Idea

Interfaces that make it easier to work with navigator routing.

### Usage

```kotlin
// Routing
object ProfileNavScreen {
    val ViewProfileScreen = object : NavScreen {
        override val route: String = "ViewProfileScreen"
    }

    val UpdateProfileScreen = object : NavScreenWithArgument {
        override val routeWithArgument: String = "UpdateProfileScreen/{email}"
        override val argument0: String = "email"
    }

    val SettingsProfileScreen = object : NavScreenWithArgument2 {
        override val routeWithArgument: String = "SettingsProfileScreen?email={email}&phone={phone}"
        override val argument0: String = "email"
        override val argument1: String = "phone"
    }
}

// Common class for multiple routing
object CommonNav {
    val ProfileNav = ProfileNavScreen
}

// Actions for navigation
interface ProfileNavActions {
    val controller: NavHostController

    fun navigateToViewProfileScreen() {
        controller.navigate(CommonNav.ProfileNav.ViewProfileScreen.route)
    }

    fun navigateToUpdateProfileScreen(email: String) {
        CommonNav.ProfileNav.UpdateProfileScreen.apply {
            controller.navigate(
                getRoute(
                    argument0 = email,
                )
            )
        }
    }

    fun navigateToSettingsProfileScreen(email: String, phone: String) {
        CommonNav.ProfileNav.SettingsProfileScreen.apply {
            controller.navigate(
                getRoute(
                    argument0 = email,
                    argument1 = phone,
                )
            ) {
                popUpTo(routeWithArgument) { inclusive = true }
            }
        }
    }
}
```

#### Connection:

```gradle
repositories {
    maven("https://artifactory.keygenqt.com/artifactory/open-source")
}
dependencies {
    implementation("com.keygenqt.routing:compose-routing:0.0.1")
}
```

# License

```
Copyright 2021 Vitaliy Zarubin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```