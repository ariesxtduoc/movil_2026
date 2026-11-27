package com.example.android_16.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.android_16.model.RolUsuario
import com.example.android_16.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var esRegistro by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.TECNICO.clave) }
    var mostrarPassword by remember { mutableStateOf(false) }

    var mostrarRecuperar by remember { mutableStateOf(false) }
    var emailRecuperacion by remember { mutableStateOf("") }
    var nuevaPasswordRecuperacion by remember { mutableStateOf("") }
    var mostrarPassRecuperacion by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn, uiState.perfil) {
        if (uiState.isLoggedIn && uiState.perfil != null) {
            onLoginSuccess(uiState.perfil!!.rol)
        }
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.limpiarMensajes()
        }
    }

    if (mostrarRecuperar) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isRecovering) {
                    mostrarRecuperar = false
                    authViewModel.limpiarTodosLosErrores()
                }
            },
            title = { Text("Restablecer contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ingresa el correo de tu cuenta y define tu nueva contraseña.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = emailRecuperacion,
                        onValueChange = {
                            emailRecuperacion = it
                            authViewModel.limpiarErrorCampo("email")
                        },
                        label = { Text("Correo de la cuenta") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.errorEmail != null) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        isError = uiState.errorEmail != null,
                        supportingText = {
                            uiState.errorEmail?.let { Text(it) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nuevaPasswordRecuperacion,
                        onValueChange = {
                            nuevaPasswordRecuperacion = it
                            authViewModel.limpiarErrorCampo("password")
                        },
                        label = { Text("Nueva contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.errorPassword != null) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                IconButton(onClick = { mostrarPassRecuperacion = !mostrarPassRecuperacion }) {
                                    Icon(
                                        imageVector = if (mostrarPassRecuperacion) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            }
                        },
                        isError = uiState.errorPassword != null,
                        supportingText = {
                            uiState.errorPassword?.let { Text(it) }
                        },
                        visualTransformation = if (mostrarPassRecuperacion) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.recuperarPassword(
                            email = emailRecuperacion,
                            nuevaPassword = nuevaPasswordRecuperacion
                        ) {
                            email = emailRecuperacion
                            password = ""
                            nuevaPasswordRecuperacion = ""
                            mostrarRecuperar = false
                        }
                    },
                    enabled = !uiState.isRecovering
                ) {
                    if (uiState.isRecovering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cambiar clave")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarRecuperar = false
                        authViewModel.limpiarTodosLosErrores()
                    },
                    enabled = !uiState.isRecovering
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "ACIS",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ACIS Operaciones",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (esRegistro) "Crea tu cuenta de acceso" else "Gestión de Órdenes de Trabajo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedVisibility(
                        visible = esRegistro,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                nombre = it
                                authViewModel.limpiarErrorCampo("nombre")
                            },
                            label = { Text("Nombre completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.errorNombre != null) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            isError = uiState.errorNombre != null,
                            supportingText = {
                                uiState.errorNombre?.let { Text(it) }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.limpiarErrorCampo("email")
                        },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.errorEmail != null) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        isError = uiState.errorEmail != null,
                        supportingText = {
                            uiState.errorEmail?.let { Text(it) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            authViewModel.limpiarErrorCampo("password")
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.errorPassword != null) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                    Icon(
                                        imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            }
                        },
                        isError = uiState.errorPassword != null,
                        supportingText = {
                            uiState.errorPassword?.let { Text(it) }
                        },
                        visualTransformation = if (mostrarPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!esRegistro) {
                        TextButton(
                            onClick = {
                                emailRecuperacion = email
                                nuevaPasswordRecuperacion = ""
                                authViewModel.limpiarTodosLosErrores()
                                mostrarRecuperar = true
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("¿Olvidaste tu contraseña?")
                        }
                    }

                    AnimatedVisibility(
                        visible = esRegistro,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Selecciona tu rol de usuario:",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = rolSeleccionado == RolUsuario.ADMIN.clave,
                                    onClick = { rolSeleccionado = RolUsuario.ADMIN.clave },
                                    label = { Text(RolUsuario.ADMIN.etiqueta) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = rolSeleccionado == RolUsuario.SUPERVISOR.clave,
                                    onClick = { rolSeleccionado = RolUsuario.SUPERVISOR.clave },
                                    label = { Text(RolUsuario.SUPERVISOR.etiqueta) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = rolSeleccionado == RolUsuario.TECNICO.clave,
                                    onClick = { rolSeleccionado = RolUsuario.TECNICO.clave },
                                    label = { Text(RolUsuario.TECNICO.etiqueta) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = rolSeleccionado == RolUsuario.CLIENTE.clave,
                                    onClick = { rolSeleccionado = RolUsuario.CLIENTE.clave },
                                    label = { Text(RolUsuario.CLIENTE.etiqueta) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = !uiState.error.isNullOrBlank()) {
                        Text(
                            text = uiState.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (esRegistro) {
                                authViewModel.registrar(nombre, email, password, rolSeleccionado)
                            } else {
                                authViewModel.login(email, password)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (esRegistro) "Crear cuenta" else "Iniciar sesión")
                        }
                    }

                    TextButton(
                        onClick = {
                            esRegistro = !esRegistro
                            authViewModel.limpiarTodosLosErrores()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            if (esRegistro) {
                                "¿Ya tienes cuenta? Inicia sesión"
                            } else {
                                "¿No tienes cuenta? Regístrate"
                            }
                        )
                    }
                }
            }
        }
    }
}