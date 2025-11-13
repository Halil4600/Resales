package com.example.resales

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.resales.Models.SalesItem
import com.example.resales.Screens.SalesItemList
import org.junit.Rule
import org.junit.Test

class SalesItemListTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val all = listOf(
        SalesItem(1, "Phone", 1000, "a@a.dk", "111", 1700000000, null),
        SalesItem(2, "Laptop", 2000, "b@b.dk", "222", 1700000001, null),
        SalesItem(3, "Bike",   500, "c@c.dk", "333", 1700000002, null)
    )

    @Composable
    private fun TestHost() {
        var shown by remember { mutableStateOf(all) }

        SalesItemList(
            items = shown,
            errorMessage = "",
            onItemsReload = { /* no-op */ },
            itemsLoading = false,
            onItemSelected = { },
            onItemDeleted = { },
            onAdd = { },

            sortByDate = { asc ->
                shown = if (asc) shown.sortedBy { it.time } else shown.sortedByDescending { it.time }
            },
            sortByPrice = { asc ->
                shown = if (asc) shown.sortedBy { it.price } else shown.sortedByDescending { it.price }
            },

            onFilterDescription = { q ->
                shown = shown.filter { it.description.contains(q, ignoreCase = true) }
            },
            onFilterMaxPrice = { max ->
                if (max != null) shown = shown.filter { it.price <= max }
            },
            onResetFilters = { shown = all },

            isLoggedIn = false,
            onLoginClick = {},
            onProfileClick = {},
            onLogoutClick = {}
        )
    }

    @Test
    fun filter_by_description_then_reset() {
        composeRule.setContent { TestHost() }

        // Før filter: 3 kort
        composeRule.onAllNodesWithTag("item_card").assertCountEquals(3)

        // Skriv "Phone" og filtrér
        composeRule.onNodeWithTag("filter_desc").performTextInput("Phone")
        composeRule.onNodeWithTag("btn_filter").performClick()

        // Efter filter: 1 kort
        composeRule.onAllNodesWithTag("item_card").assertCountEquals(1)

        // Reset → 3 igen
        composeRule.onNodeWithTag("btn_reset").performClick()
        composeRule.onAllNodesWithTag("item_card").assertCountEquals(3)
    }

    @Test
    fun filter_by_max_price() {
        composeRule.setContent { TestHost() }

        // max price = 1000 → 2 kort (Phone=1000, Bike=500)
        composeRule.onNodeWithTag("filter_max_price").performTextInput("1000")
        composeRule.onNodeWithTag("btn_filter").performClick()

        composeRule.onAllNodesWithTag("item_card").assertCountEquals(2)
    }

    @Test
    fun sort_by_price_changes_order() {
        composeRule.setContent { TestHost() }

        // Før sortering: 3 kort
        composeRule.onAllNodesWithTag("item_card").assertCountEquals(3)

        // Første kort indeholder "Phone" (vores oprindelige rækkefølge: Phone, Laptop, Bike)
        composeRule.onAllNodesWithTag("item_card")[0]
            .assert(hasText("Phone", substring = true))

        // Klik på sortér-efter-pris knappen (stigende: 500, 1000, 2000)
        composeRule.onNodeWithTag("btn_sort_price").performClick()

        // Nu bør første kort indeholde "Bike" (500 kr)
        composeRule.onAllNodesWithTag("item_card")[0]
            .assert(hasText("Bike", substring = true))

        // Klik igen → faldende (2000, 1000, 500)
        composeRule.onNodeWithTag("btn_sort_price").performClick()

        // Nu bør første kort indeholde "Laptop" (2000 kr)
        composeRule.onAllNodesWithTag("item_card")[0]
            .assert(hasText("Laptop", substring = true))
    }

}
