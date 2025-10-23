@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.resales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.resales.Models.SalesItem
import com.example.resales.Models.SalesItemViewModel
import com.example.resales.Screens.AuthenticationScreen
import com.example.resales.Screens.ProfileScreen
import com.example.resales.Screens.SalesItemList
import com.example.resales.auth.AuthenticationViewModel
import com.example.resales.ui.theme.ResalesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ResalesTheme { MainScreen() } }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    salesVm: SalesItemViewModel = viewModel(),
    authVm: AuthenticationViewModel = viewModel()
) {
    val navController = rememberNavController()

    val items = salesVm.items.value
    val errorMessage = salesVm.errorMessage.value
    val isLoading = salesVm.isLoading.value
    val isLoggedIn = authVm.user != null

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SalesItemList.route
    ) {
        // HOME
        composable(NavRoutes.SalesItemList.route) {
            SalesItemList(
                modifier = modifier,
                items = items,
                errorMessage = errorMessage,
                onItemsReload = { salesVm.getSalesItems() },
                itemsLoading = isLoading,
                onItemSelected = { _: SalesItem -> },
                onItemDeleted = { /* salesVm.remove(it) når DELETE er klar */ },
                onAdd = { /* senere */ },

                // sortering
                sortByDate = { asc -> salesVm.sortByDate(ascending = asc) },
                sortByPrice = { asc -> salesVm.sortByPrice(ascending = asc) },

                // filtrering
                onFilterDescription = { txt -> salesVm.filterByDescription(txt) },
                onFilterMaxPrice = { max -> salesVm.filterByMaxPrice(max) },
                onResetFilters = { salesVm.resetFilters() },

                // auth actions i AppBar
                isLoggedIn = isLoggedIn,
                onLoginClick = { navController.navigate(NavRoutes.Login.route) },
                onProfileClick = { navController.navigate(NavRoutes.Profile.route) },
                onLogoutClick = { authVm.signOut() }
            )
        }

        // AUTH
        composable(NavRoutes.Login.route) {
            AuthenticationScreen(
                vm = authVm,
                onDone = { navController.popBackStack() } // tilbage til Home
            )
        }

        // PROFILE (Mine items + Add/Delete)
        composable(NavRoutes.Profile.route) {
            val email = authVm.user?.email ?: ""
            ProfileScreen(
                currentUserEmail = email,
                allItems = salesVm.items.value,
                isLoading = salesVm.isLoading.value,
                errorMessage = salesVm.errorMessage.value,
                onAddItem = { item -> salesVm.add(item) },               // POST -> repo.getSalesItems()
                onDeleteItem = { id -> salesVm.removeById(id) },         // DELETE -> repo.getSalesItems()
                onBack = { navController.popBackStack() }
            )
        }
    }
}
