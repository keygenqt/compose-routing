Simplifies working with OnBackPressedDispatcher, monitors Lifecycle and interacts with HorizontalPager.
Automates the popStack feature without tracking routing

### Implementation

```kotlin
// Added local static
val LocalNavigationDispatcher = staticCompositionLocalOf<NavigationDispatcher> {
    error("No Back Dispatcher provided")
}

// Implement NavigationDispatcher in Activity
setContent {
    rememberAnimatedNavController().let { controller ->
        CompositionLocalProvider(
            LocalNavigationDispatcher provides NavigationDispatcher(
                lifecycle = lifecycle,
                controller = controller,
                backPressedDispatcher = onBackPressedDispatcher
            )
        ) {
            AppTheme {
                NavGraph(controller)
            }
        }
    }
}
```

### Usage onBackPressed

```kotlin
val navigationDispatcher = LocalNavigationDispatcher.current

SmallTopAppBar(
    navigationIcon = {
        // check if not root destination
        if (navigationDispatcher.hasEnabledCallbacks()) {
            IconButton(onClick = {
                scope.launch {
                    // if has callbacks we can press back
                    navigationDispatcher.onBackPressed()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
)
```

### Usage with HorizontalPager

```kotlin
val state = rememberPagerState()
val scope = rememberCoroutineScope()

val navigationDispatcher = LocalNavigationDispatcher.current

// set pager for navigation
navigationDispatcher.setPager(scope, state)

// listen change pager
navigationDispatcher.listenChangePager(scope) {
    // it - page id
}

// enable onBackPressed for pager - default enable
navigationDispatcher.enablePager()

// disable onBackPressed for pager
navigationDispatcher.disablePager()
```

### Usage toRoutePopStack

```kotlin
val navigationDispatcher = LocalNavigationDispatcher.current

/**
 * To welcome page
 */
fun toWelcome() {
    controller.navigate(OtherNavRoute.welcome.default.route)
}

/**
 * To login page
 */
fun toSignIn() {
    controller.navigate(OtherNavRoute.signIn.default.route)
}

// clear back stack and open Welcome page + SignIn page
navigationDispatcher.toRoutePopStack(::toWelcome, ::toSignIn)
```