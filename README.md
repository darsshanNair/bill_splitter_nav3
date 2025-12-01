# Bill Splitter - Jetpack Navigation 3 Demo App

A comprehensive Bill Splitter application demonstrating **Finite State-Machine Navigation** using Jetpack Navigation 3.

## 🎯 Purpose

This app was created as a DevFest presentation sample to demonstrate:
1. **Finite State-Machine Navigation Pattern** with Nav3
2. Clean architecture with separation of concerns
3. State management with ViewModel
4. Modern Compose UI patterns

## 🏗️ Architecture Overview

### Navigation Pattern: Finite State Machine

The app models navigation as a finite state machine where:
- Each screen is a **state** (represented by `BillSplitterRoute` sealed interface)
- User actions trigger **transitions** between states
- The back stack maintains the history of states

```
┌─────────┐     Start      ┌──────────────────┐
│  Home   │ ───────────►   │ AddParticipants  │
└─────────┘                └──────────────────┘
                                    │
                               Continue
                                    ▼
                           ┌──────────────┐
                           │ ViewExpenses │◄─┐
                           └──────────────┘  │
                                │ │          │
                          Add/Edit│          │
                                ▼ │          │
                           ┌─────────────┐   │
                           │ AddExpense  │───┘
                           └─────────────┘
                                │
                           Calculate
                                ▼
                           ┌──────────────┐
                           │ SplitResults │
                           └──────────────┘
                                │
                                ▼
                           ┌──────────────┐
                           │ Settlements  │
                           └──────────────┘
                                │
                             Finish
                                ▼
                            ┌─────────┐
                            │  Home   │
                            └─────────┘
```

## 📦 Project Structure

```
com.example.billsplitter/
├── model/
│   └── Models.kt                 # Data classes and BillSplitterRoute sealed interface
├── viewmodel/
│   └── BillSplitterViewModel.kt  # State management and business logic
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── AddParticipantsScreen.kt
│   │   ├── AddExpenseScreen.kt
│   │   ├── ViewExpensesScreen.kt
│   │   ├── SplitResultsScreen.kt
│   │   └── SettlementsScreen.kt
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── navigation/
│   └── Navigator.kt              # Navigation state manager
├── BillSplitterApp.kt            # Main app composable with NavDisplay
└── MainActivity.kt
```

## 🔑 Key Nav3 Concepts Demonstrated

### 1. **Routes as NavKey**

```kotlin
sealed interface BillSplitterRoute : NavKey {
    data object Home : BillSplitterRoute
    data object AddParticipants : BillSplitterRoute
    data class AddExpense(val editingExpenseId: String? = null) : BillSplitterRoute
    // ... more routes
}
```

**Why?** Implementing `NavKey` allows Nav3 to automatically persist the back stack across configuration changes and process death.

### 2. **Navigator - Single Source of Truth**

```kotlin
class Navigator(
    initialBackStack: SnapshotStateList<BillSplitterRoute> = 
        mutableListOf(BillSplitterRoute.Home).toMutableStateList()
) {
    var backStack: SnapshotStateList<BillSplitterRoute> by mutableStateOf(initialBackStack)
    
    fun navigateTo(route: BillSplitterRoute) {
        backStack.add(route)
    }
    
    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}
```

**Why?**
- You own the back stack (not the library)
- Simple list operations for navigation
- Easy to test and reason about
- Can implement custom navigation logic

### 3. **NavDisplay - The UI Component**

```kotlin
NavDisplay(
    backStack = navigator.backStack,
    onBack = { navigator.navigateBack() },
    enterTransition = { slideInHorizontally(tween(300)) { it } },
    exitTransition = { slideOutHorizontally(tween(300)) { -it } },
    entryProvider = { route ->
        when (route) {
            is BillSplitterRoute.Home -> NavEntry(route) {
                HomeScreen(onStartNewBill = { ... })
            }
            // ... more routes
        }
    }
)
```

**Why?**
- Declarative: UI observes the back stack
- Customizable animations per route or globally
- No internal state - you control everything

### 4. **Entry Provider Pattern**

The entry provider is a function that maps routes to composable content:

```kotlin
entryProvider = { route ->
    when (route) {
        is BillSplitterRoute.AddExpense -> NavEntry(route) {
            val editingExpense = route.editingExpenseId?.let { 
                viewModel.getExpenseById(it) 
            }
            AddExpenseScreen(
                editingExpense = editingExpense,
                onSaveExpense = { ... }
            )
        }
    }
}
```

**Why?**
- Type-safe navigation
- Access route parameters directly
- Easy to pass dependencies to screens

### 5. **State Management with ViewModel**

```kotlin
class BillSplitterViewModel : ViewModel() {
    private val _participants = MutableStateFlow<List<Person>>(emptyList())
    val participants: StateFlow<List<Person>> = _participants.asStateFlow()
    
    fun addParticipant(name: String) {
        _participants.value = _participants.value + Person(name = name)
    }
}
```

**Why?**
- Business logic separated from UI
- State survives configuration changes
- Testable independently

## 🎨 Features Demonstrated

### Navigation Features
- ✅ Forward navigation with arguments
- ✅ Back navigation
- ✅ Clear back stack and start fresh
- ✅ Pop back to specific destination
- ✅ Custom transition animations
- ✅ State preservation across config changes

### UI Features
- ✅ Material 3 Design
- ✅ Dynamic color support (Android 12+)
- ✅ Responsive layouts
- ✅ Form validation
- ✅ List management (add/remove/edit)
- ✅ Confirmation dialogs

### Business Logic
- ✅ Group expense tracking
- ✅ Fair split calculation
- ✅ Optimal settlement algorithm (greedy approach)
- ✅ Real-time balance calculation

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 35 (Android 15)

### Dependencies
```gradle
implementation("androidx.navigation:navigation-android:3.0.0")
implementation("androidx.navigation:navigation-compose:3.0.0")
```

### Building the App
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run the app

## 📚 Learning Resources

### Official Documentation
- [Navigation 3 Guide](https://developer.android.com/guide/navigation/navigation-3)
- [Nav3 Recipes Repository](https://github.com/android/nav3-recipes)
- [Nav3 Announcement Blog](https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html)

### Key Concepts
1. **You own the back stack**: Nav3 doesn't hide navigation state from you
2. **Building blocks over black boxes**: Compose smaller APIs for complex functionality
3. **Declarative UI**: Navigation state is just Compose state
4. **Type safety**: Use sealed classes/interfaces for routes

## 🎓 DevFest Presentation Talking Points

### 1. **Why Nav3?**
- Nav2 (original Navigation Component) was designed pre-Compose
- Nav3 embraces Compose's declarative model
- Better state management and flexibility

### 2. **Finite State Machine Benefits**
- Clear navigation flow
- Easy to visualize and document
- Prevents invalid navigation states
- Simplifies testing

### 3. **Developer Experience**
- Less boilerplate than Nav2
- Direct access to navigation state
- Easier debugging (just inspect the list!)
- Custom navigation logic without fighting the library

### 4. **Migration Strategy**
- Start with new features using Nav3
- Gradually migrate existing screens
- Both Nav2 and Nav3 can coexist
- Use the migration guide in nav3-recipes

## 🧪 Testing Strategy

### Unit Tests
- Test Navigator logic independently
- Test ViewModel business logic
- Test settlement algorithm

### UI Tests
- Test navigation flows
- Test form validation
- Test state updates

## 🔄 Comparison: Nav2 vs Nav3

| Feature | Nav2 | Nav3 |
|---------|------|------|
| Back stack ownership | Library | Developer |
| State management | Internal | External (Compose State) |
| UI Component | NavHost | NavDisplay |
| Route definition | String-based or type-safe | Type-safe with NavKey |
| Multi-pane layouts | Complex | Built-in Scenes |
| Customization | Limited | Highly extensible |

## 💡 Advanced Topics (Future Enhancements)

1. **Scenes for Adaptive Layouts**
    - List-detail view on tablets
    - Two-pane layout for large screens

2. **Deep Linking**
    - Handle external links to specific expenses
    - Share split results via URL

3. **Modularization**
    - Split features into separate modules
    - Use Hilt for dependency injection

4. **Result Passing**
    - Return results between destinations
    - Event-based communication

## 📝 License

```
Copyright 2025 Bill Splitter Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
```

## 🤝 Contributing

This is a demo app for educational purposes. Feel free to fork and experiment!

## 📧 Contact

For questions about Nav3 or this demo, file issues on the [nav3-recipes repository](https://github.com/android/nav3-recipes/issues).

---

**Built with ❤️ for DevFest 2025**
