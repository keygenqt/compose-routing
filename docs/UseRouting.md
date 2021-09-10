### Use Routing

Then you can easily create as many events as you need by manipulating them

```kotlin
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