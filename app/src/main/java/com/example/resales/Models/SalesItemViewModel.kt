package com.example.resales.Models

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.resales.Repository.SalesRepository

class SalesItemViewModel : ViewModel() {
    private val repository = SalesRepository()

    val items: State<List<SalesItem>> = repository.items
    val errorMessage: State<String> = repository.errorMessage
    val isLoading: State<Boolean> = repository.isLoading

    fun sortByDate(ascending: Boolean) = repository.sortByDate(ascending)
    fun sortByPrice(ascending: Boolean) = repository.sortByPrice(ascending)

    fun getSalesItems() {
        repository.getSalesItems()
    }

    fun filterByDescription(text: String) = repository.filterByDescription(text)

    fun filterByMaxPrice(max: Int?) {
        repository.filterByMaxPrice(max)
    }

    fun resetFilters() = repository.resetFilters()

    fun add(item: SalesItem) = repository.add(item)

    fun removeById(id: Int) = repository.delete(id)

}
