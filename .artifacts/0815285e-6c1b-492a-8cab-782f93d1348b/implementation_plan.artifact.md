# FindIt Campus - Authentication Module Implementation Plan

This implementation plan covers the complete Authentication module for **FindIt Campus**, developed on the `feature/authentication` branch in strict accordance with project architecture and team separation of concerns.

## User Review Required

> [!IMPORTANT]
> - **No modifications to `MainActivity.kt` or `HomeFragment.kt`**: Per team rules, Syrine owns the Home module and MainActivity. We will not modify `MainActivity.kt` or `HomeFragment.kt`.
> - **Navigation Architecture**: `LoginActivity` will be configured as the launcher activity in `AndroidManifest.xml`. On app startup, it checks `AuthRepository.getCurrentUser()`. If authenticated, it immediately launches `MainActivity` and finishes. If not, it displays the modern Login screen. After successful login or registration, it launches `MainActivity` and calls `finish()`. Logout is handled by clearing task stacks (`FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`) when returning to `LoginActivity`.

## Open Questions

- None. All architectural and design constraints (pink/yellow palette, MVVM, StateFlow, Coroutines, real Firebase Auth) are locked in.

## Proposed Changes

### Component 1: Data Model & Repository
- #### [MODIFY] [User.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/data/model/User.kt)
  - Update data class to include `uid`, `firstName`, `lastName`, `email` while preserving backward compatibility for any existing fields.
- #### [MODIFY] [AuthRepository.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/data/repository/AuthRepository.kt)
  - Centralize Firebase Auth operations using Kotlin coroutines (`suspend`), Result wrappers, `signInWithEmailAndPassword`, `createUserWithEmailAndPassword`, `signOut`, `sendPasswordResetEmail`, and `getCurrentUser`.

### Component 2: ViewModel & State Management
- #### [NEW] [AuthViewModel.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/viewmodel/AuthViewModel.kt)
  - Manage reactive `StateFlow` states for email, password, confirm password, first name, last name, loading, error messages, and success navigation events. Implement robust form validation.

### Component 3: UI Screens & Activities (Jetpack Compose + Material 3)
- #### [MODIFY] [LoginActivity.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/ui/auth/LoginActivity.kt)
  - Convert to `ComponentActivity`, check session on start, and host `LoginScreen`.
- #### [NEW] [LoginScreen.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/ui/auth/LoginScreen.kt)
  - Professional M3 Compose UI with FindIt Campus pink & yellow branding, inputs, password visibility toggle, forgot password dialog, loading indicators, and error banners.
- #### [MODIFY] [RegisterActivity.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/ui/auth/RegisterActivity.kt)
  - Convert to `ComponentActivity`, host `RegisterScreen`.
- #### [NEW] [RegisterScreen.kt](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/java/com/example/findit/ui/auth/RegisterScreen.kt)
  - Professional M3 Compose UI for registration with First Name, Last Name, Email, Password, Confirm Password, visibility controls, validation, and loading states.

### Component 4: AndroidManifest Configuration
- #### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Rahma/AndroidStudioProjects/FindIt/app/src/main/AndroidManifest.xml)
  - Set `LoginActivity` as the MAIN/LAUNCHER activity to handle session routing (so `MainActivity.kt` remains untouched).

## Verification Plan

### Automated Tests
- Build and compile verification via `./gradlew :app:assembleDebug`.

### Manual Verification
- Test all test cases:
  1. App startup with no session -> Login screen shown.
  2. App startup with active session -> Directly routes to MainActivity.
  3. Form validation (invalid email, short password, mismatched passwords, empty fields).
  4. User registration with valid details -> Success -> Navigates to MainActivity.
  5. Duplicate email registration -> Error handling.
  6. Login with correct credentials -> Success -> Navigates to MainActivity.
  7. Login with incorrect credentials -> Error message.
  8. Forgot password -> Password reset email sent.
  9. Logout -> Returns to LoginActivity and clears back stack.
