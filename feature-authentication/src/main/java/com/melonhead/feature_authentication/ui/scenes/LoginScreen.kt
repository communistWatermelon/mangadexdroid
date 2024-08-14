package com.melonhead.feature_authentication.ui.scenes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melonhead.feature_authentication.BuildConfig
import com.melonhead.feature_authentication.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginScreen(
    onLoginTapped: (email: String, password: String) -> Unit
) {
    var emailField by rememberSaveable { mutableStateOf("") }
    var passwordField by rememberSaveable { mutableStateOf("") }
    var loggingIn by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (BuildConfig.DEBUG) {
        LaunchedEffect(key1 = true) {
            emailField = BuildConfig.DEBUG_EMAIL
            passwordField = BuildConfig.DEBUG_PASSWORD
        }
    }

    fun signIn() {
        onLoginTapped(emailField, passwordField)
        loggingIn = true
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val emailNode = AutofillNode(
            autofillTypes = listOf(AutofillType.EmailAddress),
            onFill = { emailField = it }
        )
        val passwordNode = AutofillNode(
            autofillTypes = listOf(AutofillType.Password),
            onFill = { emailField = it }
        )

        val autoFill = LocalAutofill.current

        LocalAutofillTree.current += emailNode
        LocalAutofillTree.current += passwordNode

        val focusManager = LocalFocusManager.current

        Image(painter = painterResource(id = R.drawable.mangadex_v2_svgrepo_com),
            contentDescription = "Mangadex Logo",
            modifier = Modifier
                .padding(48.dp)
                .size(250.dp))

        OutlinedTextField(value = emailField,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            onValueChange = { emailField = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .onGloballyPositioned {
                    emailNode.boundingBox = it.boundsInWindow()
                }
                .onFocusChanged { focusState ->
                    autoFill?.run {
                        if (focusState.isFocused) {
                            requestAutofillForNode(emailNode)
                        } else {
                            cancelAutofillForNode(emailNode)
                        }
                    }
                }
        )
        OutlinedTextField(value = passwordField,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = {
                signIn()
            }),
            onValueChange = { passwordField = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    isPasswordVisible = !isPasswordVisible
                }) {
                    Icon(
                        imageVector = if (isPasswordVisible)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff,
                        contentDescription = "Password Visibility"
                    )
                }
            },
            modifier = Modifier
                .padding(bottom = 24.dp)
                .onGloballyPositioned {
                    passwordNode.boundingBox = it.boundsInWindow()
                }
                .onFocusChanged { focusState ->
                    autoFill?.run {
                        if (focusState.isFocused) {
                            requestAutofillForNode(passwordNode)
                        } else {
                            cancelAutofillForNode(passwordNode)
                        }
                    }
                }
        )
        Button(enabled = !loggingIn, onClick = {
            if (emailField.isNotBlank() && passwordField.isNotBlank()) {
                signIn()
            }
        }) {
            if (loggingIn) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(12.dp),
                    strokeWidth = 2.dp)
            } else {
                Text(text = "Sign In")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    com.melonhead.core_ui.theme.MangadexFollowerTheme {
        LoginScreen(onLoginTapped = { _, _ -> })
    }
}
