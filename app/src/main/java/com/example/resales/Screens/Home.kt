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
import androidx.compose.ui.platform.testTag
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

    sortByDate: (Boolean) -> Unit,
    sortByPrice: (Boolean) -> Unit,
    onFilterDescription: (String) -> Unit,
    onFilterMaxPrice: (Int?) -> Unit,
    onResetFilters: () -> Unit,

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

            var q by rememberSaveable { mutableStateOf("") }
            var maxPriceText by rememberSaveable { mutableStateOf("") }
            var dateAsc by rememberSaveable { mutableStateOf(true) }
            var priceAsc by rememberSaveable { mutableStateOf(true) }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    label = { Text("Filter by description") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("filter_desc") // TESTTAG
                )
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { maxPriceText = it.filter(Char::isDigit) },
                    label = { Text("Max price") },
                    modifier = Modifier
                        .width(140.dp)
                        .testTag("filter_max_price") // TESTTAG
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        sortByDate(dateAsc)
                        dateAsc = !dateAsc
                    },
                    modifier = Modifier.testTag("btn_sort_date")
                ) {
                    Text(if (dateAsc) "Date ↓" else "Date ↑")
                }

                TextButton(
                    onClick = {
                        sortByPrice(priceAsc)
                        priceAsc = !priceAsc
                    },
                    modifier = Modifier.testTag("btn_sort_price")
                ) {
                    Text(if (priceAsc) "Price ↓" else "Price ↑")
                }

                Button(
                    onClick = {
                        onResetFilters()
                        if (q.isNotBlank()) onFilterDescription(q)
                        onFilterMaxPrice(maxPriceText.toIntOrNull())
                    },
                    modifier = Modifier.testTag("btn_filter")
                ) {
                    Text("Filter")
                }
                TextButton(
                    onClick = {
                        q = ""
                        maxPriceText = ""
                        onResetFilters()
                    },
                    modifier = Modifier.testTag("btn_reset")
                ) { Text("Reset") }
            }

            Spacer(Modifier.height(8.dp))

            PullToRefreshBox(
                isRefreshing = itemsLoading,
                onRefresh = onItemsReload
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("items_list"),
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
            .testTag("item_card")
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.description,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("item_title_${item.id}")
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text("${item.price} kr", modifier = Modifier.testTag("item_price_${item.id}"))
                    Text(unixToDate(item.time), style = MaterialTheme.typography.bodySmall)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Sælger: ${item.sellerEmail}", style = MaterialTheme.typography.bodySmall)
                    Text("Telefon: ${item.sellerPhone}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
private fun unixToDate(seconds: Int): String =
    java.time.Instant.ofEpochSecond(seconds.toLong())
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
