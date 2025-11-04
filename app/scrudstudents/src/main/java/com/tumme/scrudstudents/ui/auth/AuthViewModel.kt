package com.tumme.scrudstudents.ui.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.local.model.UserEntity
import com.tumme.scrudstudents.data.local.model.UserRole
import com.tumme.scrudstudents.data.local.model.LevelCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val LAST_USERNAME_KEY = "last_username"
    private val LAST_USER_ID_KEY = "last_user_id"

    // Use SharedPreferences for persistence across ViewModel instances
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // [FIX 1] Simplify getters to rely ONLY on SharedPreferences for cross-session persistence.
    // This prevents the restore loop if SavedStateHandle fails to clear or restores an old value.
    private val lastLoggedInUsername: String?
        get() = prefs.getString(LAST_USERNAME_KEY, null)

    private val lastLoggedInUserId: Int?
        get() = prefs.getInt(LAST_USER_ID_KEY, 0).takeIf { it > 0 }

    init {
        // Restore user immediately when ViewModel is created
        restoreUser()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.authenticate(username, password)
                if (user != null) {
                    _currentUser.value = user
                    // Save to SharedPreferences
                    prefs.edit()
                        .putString(LAST_USERNAME_KEY, username)
                        .putInt(LAST_USER_ID_KEY, user.id)
                        .apply()
                    // [FIX 2] Also save to SavedStateHandle for configuration changes within the same session
                    savedStateHandle[LAST_USERNAME_KEY] = username
                    savedStateHandle[LAST_USER_ID_KEY] = user.id

                    android.util.Log.d("AuthViewModel", "Saved userId to SharedPreferences: ${user.id}")
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun register(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        role: UserRole,
        levelOfStudy: LevelCourse? = null,
        department: String? = null,
        specialization: String? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.registerUser(
                    username = username,
                    password = password,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    role = role,
                    levelOfStudy = levelOfStudy,
                    department = department,
                    specialization = specialization
                )
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        // Save to SharedPreferences
                        prefs.edit()
                            .putString(LAST_USERNAME_KEY, username)
                            .putInt(LAST_USER_ID_KEY, user.id)
                            .apply()
                        // [FIX 2] Also save to SavedStateHandle for configuration changes within the same session
                        savedStateHandle[LAST_USERNAME_KEY] = username
                        savedStateHandle[LAST_USER_ID_KEY] = user.id

                        android.util.Log.d("AuthViewModel", "Saved userId to SharedPreferences after registration: ${user.id}")
                        _authState.value = AuthState.Success(user)
                    },
                    onFailure = { error ->
                        _authState.value = AuthState.Error("Registration failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun logout() {
        _currentUser.value = null

        // Remove from SavedStateHandle (Synchronous)
        savedStateHandle.remove<String>(LAST_USERNAME_KEY)
        savedStateHandle.remove<Int>(LAST_USER_ID_KEY)

        // Use synchronous commit() to ensure SharedPreferences are cleared immediately
        prefs.edit()
            .remove(LAST_USERNAME_KEY)
            .remove(LAST_USER_ID_KEY)
            .commit()

        android.util.Log.d("AuthViewModel", "Logout successful: cleared local session data.")
        _authState.value = AuthState.Idle
    }

    fun restoreUser() {
        viewModelScope.launch {
            android.util.Log.d("AuthViewModel", "restoreUser called - currentUser: ${_currentUser.value?.id}, savedUserId: $lastLoggedInUserId, savedUsername: $lastLoggedInUsername")

            // First try to restore by user ID (faster) - Uses the simplified getter which checks SharedPreferences only
            val userId = lastLoggedInUserId
            if (userId != null && _currentUser.value == null) {
                android.util.Log.d("AuthViewModel", "Trying to restore by userId: $userId")
                val user = authRepository.getUserById(userId)
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Restored user by userId: ${user.id}, name: ${user.firstName} ${user.lastName}")
                    _currentUser.value = user
                    // Ensure SavedStateHandle is updated if restored from preferences
                    savedStateHandle[LAST_USER_ID_KEY] = user.id
                    savedStateHandle[LAST_USERNAME_KEY] = user.username
                    return@launch
                } else {
                    android.util.Log.w("AuthViewModel", "User not found for userId: $userId")
                }
            }

            // Fallback to username restore from SharedPreferences if user ID restore failed
            val username = lastLoggedInUsername
            if (username != null && _currentUser.value == null) {
                android.util.Log.d("AuthViewModel", "Trying to restore by username: $username")
                val user = authRepository.getUserByUsername(username)
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Restored user by username: ${user.id}, name: ${user.firstName} ${user.lastName}")
                    _currentUser.value = user

                    // Update SharedPreferences (using asynchronous apply for efficiency)
                    prefs.edit().putInt(LAST_USER_ID_KEY, user.id).apply()

                    // Update SavedStateHandle
                    savedStateHandle[LAST_USER_ID_KEY] = user.id
                } else {
                    android.util.Log.w("AuthViewModel", "User not found for username: $username")
                }
            }
        }
    }

    fun getLastUserId(): Int? {
        return lastLoggedInUserId
    }

    fun getSavedUsername(): String? {
        return lastLoggedInUsername
    }
}
