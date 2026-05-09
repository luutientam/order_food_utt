package com.utt.foodorderapp.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.UserManagementAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.data.repository.UserRepository
import com.utt.foodorderapp.databinding.ActivityManageUsersBinding
import com.utt.foodorderapp.model.User

class AdminUserManagementActivity : BaseActivity() {

    private var binding: ActivityManageUsersBinding? = null
    private var users: MutableList<User> = ArrayList()
    private var adapter: UserManagementAdapter? = null
    private var listener: ChildEventListener? = null
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initToolbar()
        adapter = UserManagementAdapter(users, object : UserManagementAdapter.IUserManagementListener {
            override fun toggleUser(user: User) {
                val userId = user.uid ?: return
                userRepository.updateUserActive(userId, !user.isActive) {
                    if (it == null) {
                        showToastMessage(this@AdminUserManagementActivity, getString(R.string.action_ok))
                    }
                }
            }
        })
        binding!!.rcvUsers.layoutManager = LinearLayoutManager(this)
        binding!!.rcvUsers.adapter = adapter
        subscribeUsers()
    }

    private fun initToolbar() {
        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.manage_accounts)
        binding!!.toolbar.imgBack.setOnClickListener { finish() }
    }

    private fun subscribeUsers() {
        listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(User::class.java) ?: return
                user.uid = snapshot.key
                users.add(0, user)
                adapter?.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updated = snapshot.getValue(User::class.java) ?: return
                updated.uid = snapshot.key
                for (i in users.indices) {
                    if (users[i].uid == updated.uid) {
                        users[i] = updated
                        break
                    }
                }
                adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ControllerApplication[this].userDatabaseReference.addChildEventListener(listener!!)
    }

    override fun onDestroy() {
        if (listener != null) {
            ControllerApplication[this].userDatabaseReference.removeEventListener(listener!!)
        }
        listener = null
        super.onDestroy()
    }
}
