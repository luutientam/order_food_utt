package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.adapter.FeedbackAdapter.FeedbackViewHolder
import com.utt.foodorderapp.databinding.ItemFeedbackBinding
import com.utt.foodorderapp.model.Feedback

class FeedbackAdapter(
        private val mListFeedback: List<Feedback>?,
        private val mDeleteListener: IDeleteFeedbackListener? = null
) : RecyclerView.Adapter<FeedbackViewHolder>() {

    interface IDeleteFeedbackListener {
        fun onDelete(feedback: Feedback)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val itemFeedbackBinding = ItemFeedbackBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        return FeedbackViewHolder(itemFeedbackBinding)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = mListFeedback!![position]
        holder.mItemFeedbackBinding.tvEmail.text = feedback.email
        holder.mItemFeedbackBinding.tvFeedback.text = "[${feedback.rating}/5] ${feedback.comment}"
        holder.mItemFeedbackBinding.tvDeleteReview.setOnClickListener {
            mDeleteListener?.onDelete(feedback)
        }
        holder.mItemFeedbackBinding.tvDeleteReview.visibility = if (mDeleteListener != null) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun getItemCount(): Int {
        return mListFeedback?.size ?: 0
    }

    class FeedbackViewHolder(val mItemFeedbackBinding: ItemFeedbackBinding) : RecyclerView.ViewHolder(mItemFeedbackBinding.root)
}