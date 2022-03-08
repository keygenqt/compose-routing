Simplifies and systematizes the work with routing

### Implementation interface

For simple cases define `route` and use `routeWithArguments` for navigation.

```kotlin
/**
 * Routing for [ReposScreen]
 */
object RepoRoute {
    val screen = object : NavigationRoute {
        override val route: String = "RepoRouteScreen"
    }

    val screenWithId = object : NavigationRouteArgument1 {
        override val argument0: String = "id"
        override val route: String = "RepoRouteScreenWithId/{$argument0}"
    }

    val screenWithIdAndUrl = object : NavigationRouteArgument2 {
        override val argument0: String = "id"
        override val argument1: String = "url"
        override val route: String = "RepoRouteScreenWithIdAndUrl/{$argument0}/{$argument1}"
    }
}
```

For nested graphs define `route` and call `configureRoute`, `routeWithRootAndArguments` which respect root.

The nested graphs support helps to make the whole navigation more flexible and enables the extended way of reusing the same screen in different graphs, but routes will be unique because the root is present.

It can be useful for apps with `BottomNavigation` when one particular screen could be opened from different tabs at the same time when the bottom navigation is still present (multiple back stack).

```kotlin
object CatalogNav {
    object CatalogNavScreen : NavigationRouteArgument1 {
        override val argument0: String = "name"
        override val route: String = "CatalogNavScreen/{$argument0}"
    }
}
```

### Usage

```kotlin
/**
 * To repo view
 */
fun toRepo() {
    controller.navigate(ReposNavRoute.repo.screen.route)
}

/**
 * To repo view by ID
 */
fun toRepo(id: String) {
    with(ReposNavRoute.repo.screenWithId) {
        controller.navigate(routeWithArguments(argument0 = id))
    }
}

/**
 * To repo view by ID and Uri
 */
fun toRepo(id: String, url: Uri) {
    with(ReposNavRoute.repo.screenWithIdAndUrl) {
        controller.navigate(routeWithArguments(
            argument0 = id,
            argument1 = URLEncoder.encode(url.toString(), Charsets.UTF_8.name())
        ))
    }
}
```

Nested graph sample:

```kotlin
// Define catalog screen which depends on given root
with(CatalogNav.CatalogNavScreen) {
    composable(configureRoute(root = "sample_root")) { backStackEntry ->
        backStackEntry.arguments?.let { bundle ->
            CatalogScreen(
                name = bundle.getString(argument0)!!
            )
        }
    }
}
// Navigate to catalog
navController.navigate(
    CatalogNav.CatalogNavScreen(
        root = "sample_root",
        argument0 = categoryName
    )
)
```
