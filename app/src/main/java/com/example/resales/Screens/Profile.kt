@file:OptIn(ExperimentalMaterialApi::class)

package com.example.resales.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.resales.Models.SalesItem
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding


// ↓ NY import til baggrundsfarven i swipe-baggrunden
import androidx.compose.foundation.background

// SwipeToDismiss (Material 2)
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.automirrored.filled.ArrowBack
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserEmail: String,
    allItems: List<SalesItem>,
    isLoading: Boolean,
    errorMessage: String,
    onAddItem: (SalesItem) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onBack: () -> Unit
) {
    val myItems = remember(allItems, currentUserEmail) {
        allItems.filter { it.sellerEmail.equals(currentUserEmail, ignoreCase = true) }
    }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Min profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbage")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).padding(12.dp)) {
            if (errorMessage.isNotEmpty()) {
                Text("Problem: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
            if (isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            if (myItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ingen items endnu. Tryk på + for at tilføje.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myItems, key = { it.id }) { item ->

                        val dismissState = rememberDismissState(
                            confirmStateChange = { value ->
                                if (value == DismissValue.DismissedToStart) {
                                    onDeleteItem(item.id)
                                }
                                true
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.EndToStart),
                            background = {
                                val bg = if (dismissState.dismissDirection == DismissDirection.EndToStart)
                                    MaterialTheme.colorScheme.errorContainer
                                else
                                    MaterialTheme.colorScheme.surface

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(bg)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Slet",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            },
                            dismissContent = {
                                ElevatedCard(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.description, style = MaterialTheme.typography.titleMedium)
                                            Text("${item.price} kr", style = MaterialTheme.typography.bodyMedium)
                                            Text(unixToDate(item.time), style = MaterialTheme.typography.bodySmall)
                                        }
                                        IconButton(onClick = { onDeleteItem(item.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Slet")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAdd) {
            AddItemSheet(
                currentUserEmail = currentUserEmail,
                onDismiss = { showAdd = false },
                onSave = { newItem ->
                    onAddItem(newItem)
                    showAdd = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemSheet(
    currentUserEmail: String,
    onDismiss: () -> Unit,
    onSave: (SalesItem) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pic by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scroll = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nyt item", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it.filter(Char::isDigit) },
                label = { Text("Price") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = pic,
                onValueChange = { pic = it },
                label = { Text("Picture URL (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Annullér")
                }
                Button(
                    onClick = {
                        val nowSec = (System.currentTimeMillis() / 1000L).toInt()
                        onSave(
                            SalesItem(
                                id = -1,
                                description = desc.trim(),
                                price = priceText.toIntOrNull() ?: 0,
                                sellerEmail = currentUserEmail,
                                sellerPhone = phone.trim(),
                                time = nowSec,
                                pictureUrl = pic.trim().ifBlank { null }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Gem") }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

private fun unixToDate(seconds: Int): String =
    java.time.Instant.ofEpochSecond(seconds.toLong())
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
