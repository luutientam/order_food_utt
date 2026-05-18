package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.model.Review
import com.utt.foodorderapp.utils.DateTimeUtils

class ReviewListAdapter(
        private val items: MutableList<Review>
) : RecyclerView.Adapter<ReviewListAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.tv_user)
        val rating: RatingBar = view.findViewById(R.id.rating_value)
        val comment: TextView = view.findViewById(R.id.tv_comment)
        val time: TextView = view.findViewById(R.id.tv_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val review = items[position]
        holder.user.text = maskEmail(review.userEmail)
        holder.rating.rating = review.rating.toFloat()
        if (review.comment.isNullOrBlank()) {
            holder.comment.visibility = View.GONE
        } else {
            holder.comment.visibility = View.VISIBLE
            holder.comment.text = review.comment
        }
        holder.time.text = DateTimeUtils.convertTimeStampToDate(review.createdAt)
    }

    fun update(newItems: List<Review>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    /** Ẩn bớt email cho có vẻ riêng tư: abc***@gmail.com */
    private fun maskEmail(email: String?): String {
        if (email.isNullOrEmpty()) return "Ẩn danh"
        val at = email.indexOf('@')
        if (at <= 1) return email
        val prefix = email.substring(0, minOf(3, at))
        val domain = email.substring(at)
        return "$prefix***$domain"
    }
}
