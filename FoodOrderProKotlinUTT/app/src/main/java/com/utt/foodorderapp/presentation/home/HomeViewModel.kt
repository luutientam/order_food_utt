package com.utt.foodorderapp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.data.repository.FoodRepository
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.presentation.common.UiState

class HomeViewModel(
        private val foodRepository: FoodRepository = FoodRepository()
) : ViewModel() {

    private var foodsListener: ValueEventListener? = null

    private val _foodsState = MutableLiveData<UiState<List<Food>>>(UiState.Idle)
    val foodsState: LiveData<UiState<List<Food>>> = _foodsState

    fun loadFoods(query: String) {
        _foodsState.value = UiState.Loading
        foodsListener?.let { foodRepository.removeObserveFoods(it) }
        foodsListener = foodRepository.observeFoods(query, { list ->
            _foodsState.postValue(UiState.Success(list))
        }, { message ->
            _foodsState.postValue(UiState.Error(message))
        })
    }

    override fun onCleared() {
        foodsListener?.let { foodRepository.removeObserveFoods(it) }
        foodsListener = null
        super.onCleared()
    }
}
