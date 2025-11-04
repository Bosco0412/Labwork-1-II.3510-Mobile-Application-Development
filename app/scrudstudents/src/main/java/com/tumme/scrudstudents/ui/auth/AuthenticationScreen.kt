package com.tumme.scrudstudents.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.UserRole

@Composable
fun AuthenticationScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onAutoLogin: ((UserRole) -> Unit)? = null,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var showLogin by remember { mutableStateOf(true) }
    val currentUser by viewModel.currentUser.collectAsState()
    val authState by viewModel.authState.collectAsState()

    // Auto-login: If user is already logged in, navigate to dashboard
    LaunchedEffect(currentUser, authState) {
        val user = currentUser
        if (user != null && onAutoLogin != null) {
            // Small delay to ensure UI is ready
            kotlinx.coroutines.delay(300)
            onAutoLogin(user.role)
        }
    }

    // Restore user session on first load
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            viewModel.restoreUser()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (showLogin) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = { showLogin = false },
                viewModel = viewModel
            )
        } else {
            RegisterScreen(
                onRegisterSuccess = onLoginSuccess,
                onNavigateToLogin = { showLogin = true },
                viewModel = viewModel
            )
        }
    }
}
