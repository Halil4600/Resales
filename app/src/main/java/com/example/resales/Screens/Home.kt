package com.example.resales.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resales.Models.SalesItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesItemList(
    modifier: Modifier = Modifier,
    items: List<SalesItem>,
    errorMessage: String,
    onItemsReload: () -> Unit,
    itemsLoading: Boolean,
    onItemSelected: (SalesItem) -> Unit = {},
    onItemDeleted: (SalesItem) -> Unit = {},
    onAdd: () -> Unit = {},

    // sortering
    sortByDate: (Boolean) -> Unit,
    sortByPrice: (Boolean) -> Unit,

    // filtrering (separate)
    onFilterDescription: (String) -> Unit,
    onFilterMaxPrice: (Int?) -> Unit,
    onResetFilters: () -> Unit,

    // AUTH actions til AppBar
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales items") },
                actions = {
                    if (isLoggedIn) {
                        TextButton(onClick = onProfileClick) { Text("Min profil") }
                        TextButton(onClick = onLogoutClick) { Text("Log ud") }
                    } else {
                        TextButton(onClick = onLoginClick) { Text("Login") }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(12.dp)) {

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = "Problem: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ---------- Filtre ----------
            var q by rememberSaveable { mutableStateOf("") }
            var maxPriceText by rememberSaveable { mutableStateOf("") }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    label = { Text("Filter by description") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { maxPriceText = it.filter(Char::isDigit) },
                    label = { Text("Max price") },
                    modifier = Modifier.width(140.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Button(onClick = {
                    // VIGTIGT: start fra fuld liste -> filtrer videre
                    onResetFilters()
                    if (q.isNotBlank()) onFilterDescription(q)
                    onFilterMaxPrice(maxPriceText.toIntOrNull())
                }) {
                    Text("Filter")
                }
                TextButton(onClick = {
                    q = ""
                    maxPriceText = ""
                    onResetFilters()
                }) { Text("Reset") }
            }

            // ---------- Sortering ----------
            var dateAsc by rememberSaveable { mutableStateOf(true) }
            var priceAsc by rememberSaveable { mutableStateOf(true) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = {
                    sortByDate(dateAsc)     // true = ældste→nyeste
                    dateAsc = !dateAsc
                }) { Text(if (dateAsc) "Date ↓" else "Date ↑") }

                TextButton(onClick = {
                    sortByPrice(priceAsc)   // true = laveste→højeste
                    priceAsc = !priceAsc
                }) { Text(if (priceAsc) "Price ↓" else "Price ↑") }
            }

            Spacer(Modifier.height(8.dp))

            // ---------- Liste med pull-to-refresh ----------
            PullToRefreshBox(
                isRefreshing = itemsLoading,
                onRefresh = onItemsReload
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        SalesItemCard(
                            item = item,
                            onDelete = { onItemDeleted(item) },
                            onSelected = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesItemCard(
    item: SalesItem,
    onDelete: () -> Unit = {},
    onSelected: () -> Unit = {}
) {
    var expanded by rememberSaveable(item.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                expanded = !expanded
                onSelected()
            }
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.description, style = MaterialTheme.typography.titleMedium)
                Column(horizontalAlignment = Alignment.End) {
                    Text("${item.price} kr")
                    Text(unixToDate(item.time), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Detaljer
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Sælger: ${item.sellerEmail}", style = MaterialTheme.typography.bodySmall)
                    Text("Telefon: ${item.sellerPhone}", style = MaterialTheme.typography.bodySmall)
                    // TODO: pictureUrl -> vis med Coil, hvis du vil
                }
            }
        }
    }
}

// UNIX sekunder -> yyyy-MM-dd
private fun unixToDate(seconds: Int): String =
    java.time.Instant.ofEpochSecond(seconds.toLong())
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
