package com.utt.foodorderapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.MainActivity
import com.utt.foodorderapp.adapter.FeedbackAdapter
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.databinding.FragmentFeedbackBinding
import com.utt.foodorderapp.model.Feedback
import com.utt.foodorderapp.prefs.DataStoreManager.Companion.user
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.presentation.feedback.FeedbackViewModel
import com.utt.foodorderapp.utils.StringUtil.isEmpty

class FeedbackFragment : BaseFragment() {

    private var mFragmentFeedbackBinding: FragmentFeedbackBinding? = null
    private val feedbackViewModel: FeedbackViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentFeedbackBinding = FragmentFeedbackBinding.inflate(inflater, container, false)
        mFragmentFeedbackBinding!!.edtEmail.setText(user!!.email)
        mFragmentFeedbackBinding!!.rcvFeedback.layoutManager = LinearLayoutManager(activity)
        mFragmentFeedbackBinding!!.tvSendFeedback.setOnClickListener { onClickSendFeedback() }
        observeViewModel()
        feedbackViewModel.loadFeedbacks()
        return mFragmentFeedbackBinding!!.root
    }

    private fun onClickSendFeedback() {
        if (activity == null) {
            return
        }
        val activity = activity as MainActivity?
        val strName = mFragmentFeedbackBinding!!.edtName.text.toString()
        val strPhone = mFragmentFeedbackBinding!!.edtPhone.text.toString()
        val strEmail = mFragmentFeedbackBinding!!.edtEmail.text.toString()
        val strComment = mFragmentFeedbackBinding!!.edtComment.text.toString()
        val rating = mFragmentFeedbackBinding!!.edtRating.text.toString().toIntOrNull() ?: 5
        val restaurantId = mFragmentFeedbackBinding!!.edtRestaurantId.text.toString().toLongOrNull() ?: 0L
        val foodId = mFragmentFeedbackBinding!!.edtFoodId.text.toString().toLongOrNull() ?: 0L
        when {
            isEmpty(strName) -> {
                showToastMessage(activity, getString(R.string.name_require))
            }
            isEmpty(strComment) -> {
                showToastMessage(activity, getString(R.string.comment_require))
            }
            else -> {
                activity!!.showProgressDialog(true)
                val feedbackId = System.currentTimeMillis()
                val feedback = Feedback(feedbackId, strName, strPhone, strEmail, strComment, rating, restaurantId, foodId, feedbackId)
                feedbackViewModel.submitFeedback(feedback)
            }
        }
    }

    private fun sendFeedbackSuccess() {
        activity?.let { hideSoftKeyboard(it) }
        showToastMessage(activity, getString(R.string.send_feedback_success))
        mFragmentFeedbackBinding!!.edtName.setText("")
        mFragmentFeedbackBinding!!.edtPhone.setText("")
        mFragmentFeedbackBinding!!.edtComment.setText("")
        mFragmentFeedbackBinding!!.edtRating.setText("5")
        mFragmentFeedbackBinding!!.edtRestaurantId.setText("")
        mFragmentFeedbackBinding!!.edtFoodId.setText("")
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.feedback))
        }
    }

    private fun observeViewModel() {
        feedbackViewModel.feedbacksState.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Unit
                is UiState.Error -> Unit
                is UiState.Success -> {
                    mFragmentFeedbackBinding?.rcvFeedback?.adapter = FeedbackAdapter(state.data)
                }
            }
        }
        feedbackViewModel.submitState.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> (activity as? MainActivity)?.showProgressDialog(true)
                is UiState.Error -> (activity as? MainActivity)?.showProgressDialog(false)
                is UiState.Success -> {
                    (activity as? MainActivity)?.showProgressDialog(false)
                    sendFeedbackSuccess()
                }
            }
        }
    }

    override fun onDestroyView() {
        mFragmentFeedbackBinding = null
        super.onDestroyView()
    }
}