package com.utt.foodorderapp.presentation.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.utt.foodorderapp.data.repository.FoodRepository
import com.utt.foodorderapp.data.repository.OrderRepository
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.presentation.common.UiState

class CartViewModel(
        application: Application,
        private val foodRepository: FoodRepository = FoodRepository(),
        private val orderRepository: OrderRepository = OrderRepository()
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
            application,
            FoodRepository(),
            OrderRepository()
    )

    private val _cartState = MutableLiveData<UiState<List<Food>>>(UiState.Idle)
    val cartState: LiveData<UiState<List<Food>>> = _cartState

    private val _orderState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    val orderState: LiveData<UiState<Boolean>> = _orderState

    fun loadCart() {
        _cartState.value = UiState.Success(foodRepository.getCart(getApplication()))
    }

    fun updateCartItem(food: Food) {
        foodRepository.updateCartItem(getApplication(), food)
        loadCart()
    }

    fun removeCartItem(food: Food) {
        foodRepository.removeCartItem(getApplication(), food)
        loadCart()
    }

    fun submitOrder(order: Order) {
        _orderState.value = UiState.Loading
        orderRepository.createOrder(order) { error ->
            if (error == null) {
                foodRepository.clearCart(getApplication())
                loadCart()
                _orderState.postValue(UiState.Success(true))
            } else {
                _orderState.postValue(UiState.Error(error.message))
            }
        }
    }
}
