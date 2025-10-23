package com.example.resales.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.resales.Models.SalesItem

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Tilbage")
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nyt item", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(desc, { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                priceText, { priceText = it.filter(Char::isDigit) },
                label = { Text("Price") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pic, { pic = it }, label = { Text("Picture URL (optional)") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Annullér") }
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
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun unixToDate(seconds: Int): String =
    java.time.Instant.ofEpochSecond(seconds.toLong())
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
