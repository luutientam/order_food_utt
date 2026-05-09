package com.utt.foodorderapp.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.AdminMainActivity
import com.utt.foodorderapp.adapter.FeedbackAdapter
import com.utt.foodorderapp.databinding.FragmentAdminFeedbackBinding
import com.utt.foodorderapp.fragment.BaseFragment
import com.utt.foodorderapp.model.Feedback
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.presentation.feedback.FeedbackViewModel

class AdminFeedbackFragment : BaseFragment() {

    private var mFragmentAdminFeedbackBinding: FragmentAdminFeedbackBinding? = null
    private val feedbackViewModel: FeedbackViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminFeedbackBinding = FragmentAdminFeedbackBinding.inflate(inflater, container, false)
        initView()
        observeViewModel()
        feedbackViewModel.loadFeedbacks()
        return mFragmentAdminFeedbackBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.feedback))
        }
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminFeedbackBinding!!.rcvFeedback.layoutManager = linearLayoutManager
    }

    override fun onDestroyView() {
        mFragmentAdminFeedbackBinding = null
        super.onDestroyView()
    }

    private fun deleteFeedback(feedback: Feedback) {
        feedbackViewModel.deleteFeedback(feedback)
    }

    private fun observeViewModel() {
        feedbackViewModel.feedbacksState.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Unit
                is UiState.Error -> Unit
                is UiState.Success -> {
                    mFragmentAdminFeedbackBinding?.rcvFeedback?.adapter = FeedbackAdapter(state.data, object : FeedbackAdapter.IDeleteFeedbackListener {
                        override fun onDelete(feedback: Feedback) {
                            deleteFeedback(feedback)
                        }
                    })
                }
            }
        }
    }
}