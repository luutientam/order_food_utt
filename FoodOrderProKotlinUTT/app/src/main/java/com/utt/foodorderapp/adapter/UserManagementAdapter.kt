package com.utt.foodorderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ItemUserManagementBinding
import com.utt.foodorderapp.model.User

class UserManagementAdapter(
        private val users: MutableList<User>,
        private val listener: IUserManagementListener
) : RecyclerView.Adapter<UserManagementAdapter.UserManagementViewHolder>() {

    interface IUserManagementListener {
        fun toggleUser(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserManagementViewHolder {
        val binding = ItemUserManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserManagementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserManagementViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context
        holder.binding.tvEmail.text = user.email
        holder.binding.tvRole.text = "${context.getString(R.string.user_role)}: ${user.role}"
        holder.binding.tvStatus.text = if (user.isActive) context.getString(R.string.status_active) else context.getString(R.string.status_locked)
        holder.binding.tvToggleStatus.text = if (user.isActive) context.getString(R.string.action_lock) else context.getString(R.string.action_unlock)
        holder.binding.tvToggleStatus.setOnClickListener { listener.toggleUser(user) }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class UserManagementViewHolder(val binding: ItemUserManagementBinding) : RecyclerView.ViewHolder(binding.root)
}
