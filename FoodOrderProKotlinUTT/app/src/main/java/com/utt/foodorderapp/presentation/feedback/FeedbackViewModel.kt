package com.utt.foodorderapp.presentation.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.data.repository.FeedbackRepository
import com.utt.foodorderapp.model.Feedback
import com.utt.foodorderapp.presentation.common.UiState

class FeedbackViewModel(
        private val feedbackRepository: FeedbackRepository = FeedbackRepository()
) : ViewModel() {

    private var listener: ValueEventListener? = null

    private val _feedbacksState = MutableLiveData<UiState<List<Feedback>>>(UiState.Idle)
    val feedbacksState: LiveData<UiState<List<Feedback>>> = _feedbacksState

    private val _submitState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    val submitState: LiveData<UiState<Boolean>> = _submitState

    fun loadFeedbacks() {
        _feedbacksState.value = UiState.Loading
        listener?.let { feedbackRepository.removeFeedbackListener(it) }
        listener = feedbackRepository.observeFeedbacks({ list ->
            _feedbacksState.postValue(UiState.Success(list))
        }, { message ->
            _feedbacksState.postValue(UiState.Error(message))
        })
    }

    fun submitFeedback(feedback: Feedback) {
        _submitState.value = UiState.Loading
        feedbackRepository.submitFeedback(feedback) { error ->
            if (error == null) {
                _submitState.postValue(UiState.Success(true))
            } else {
                _submitState.postValue(UiState.Error(error.message))
            }
        }
    }

    fun deleteFeedback(feedback: Feedback) {
        if (feedback.id <= 0) return
        feedbackRepository.deleteFeedback(feedback.id)
    }

    override fun onCleared() {
        listener?.let { feedbackRepository.removeFeedbackListener(it) }
        listener = null
        super.onCleared()
    }
}
