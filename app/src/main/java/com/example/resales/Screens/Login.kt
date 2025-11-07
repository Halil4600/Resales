package com.example.resales.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.resales.Models.AuthenticationViewModel
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    vm: AuthenticationViewModel,
    onDone: () -> Unit, // popBack til Home ved succes
) {
    // Hvis logget ind, navigér straks tilbage til Home
    val user: FirebaseUser? = vm.user
    LaunchedEffect(user) {
        if (user != null) onDone()
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) } // toggle mellem login/registrering

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isLogin) "Login" else "Register") }) }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPwd = !showPwd }) {
                        Icon(
                            imageVector = if (showPwd) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            if (vm.message.isNotEmpty()) {
                Text(vm.message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (isLogin) vm.signIn(email, password) else vm.signUp(email, password)
                },
                enabled = !vm.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLogin) "Sign in" else "Register")
            }

            TextButton(
                onClick = { isLogin = !isLogin },
                enabled = !vm.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLogin) "Create account" else "I already have an account")
            }
        }
    }
}
