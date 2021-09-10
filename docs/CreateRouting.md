### Create Routing

With a set of interfaces, you can create a routed object

```kotlin
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
```