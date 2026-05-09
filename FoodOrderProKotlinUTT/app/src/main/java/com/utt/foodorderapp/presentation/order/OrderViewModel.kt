package com.utt.foodorderapp.presentation.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.data.repository.OrderRepository
import com.utt.foodorderapp.model.Order
import com.utt.foodorderapp.presentation.common.UiState

class OrderViewModel(
        private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private var listener: ValueEventListener? = null

    private val _ordersState = MutableLiveData<UiState<List<Order>>>(UiState.Idle)
    val ordersState: LiveData<UiState<List<Order>>> = _ordersState

    fun observeOrders(filter: (Order) -> Boolean) {
        _ordersState.value = UiState.Loading
        listener?.let { orderRepository.removeOrderValueListener(it) }
        listener = orderRepository.observeOrders({ list ->
            _ordersState.postValue(UiState.Success(list.filter(filter)))
        }, { message ->
            _ordersState.postValue(UiState.Error(message))
        })
    }

    fun updateOrderStatus(orderId: Long, status: Int, onDone: (Boolean) -> Unit) {
        orderRepository.getOrder(orderId) { currentOrder ->
            if (currentOrder == null) {
                onDone(false)
                return@getOrder
            }
            val currentStatus = currentOrder.getStatusValue()
            val isAllowed = when (status) {
                Order.STATUS_PREPARING -> currentStatus == Order.STATUS_NEW
                Order.STATUS_CANCEL -> currentStatus == Order.STATUS_NEW || currentStatus == Order.STATUS_PREPARING || currentStatus == Order.STATUS_DELIVERING
                Order.STATUS_SUCCESS -> currentStatus == Order.STATUS_DELIVERING
                else -> false
            }
            if (!isAllowed) {
                onDone(false)
                return@getOrder
            }
            val payload: MutableMap<String, Any> = HashMap()
            payload["status"] = status
            payload["completed"] = status == Order.STATUS_SUCCESS
            orderRepository.updateOrder(orderId, payload) { error ->
                onDone(error == null)
            }
        }
    }

    override fun onCleared() {
        listener?.let { orderRepository.removeOrderValueListener(it) }
        listener = null
        super.onCleared()
    }
}
