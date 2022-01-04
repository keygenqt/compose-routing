Simplifies and systematizes the work with routing

### Implementation interface

```kotlin
/**
 * Routing for [ReposScreen]
 */
object RepoRoute {
    val screen = object : NavigationRouteArgument2 {
        override val argument0: String = "id"
        override val argument1: String = "url"
        override val route: String = "RepoRouteDefault/{$argument0}/{$argument1}"
    }

    val screenWithId = object : NavigationRouteArgument1 {
        override val argument0: String = "id"
        override val route: String = "RepoRouteDefault/{$argument0}"
    }

    val screenWithIdAndUrl = object : NavigationRouteArgument2 {
        override val argument0: String = "id"
        override val argument1: String = "url"
        override val route: String = "RepoRouteDefault/{$argument0}/{$argument1}"
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