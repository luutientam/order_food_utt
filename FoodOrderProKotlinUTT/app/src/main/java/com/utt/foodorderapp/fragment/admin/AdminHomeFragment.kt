package com.utt.foodorderapp.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.R
import com.utt.foodorderapp.activity.AddFoodActivity
import com.utt.foodorderapp.activity.AdminMainActivity
import com.utt.foodorderapp.adapter.AdminFoodAdapter
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.getTextSearch
import com.utt.foodorderapp.constant.GlobalFunction.hideSoftKeyboard
import com.utt.foodorderapp.constant.GlobalFunction.startActivity
import com.utt.foodorderapp.databinding.FragmentAdminHomeBinding
import com.utt.foodorderapp.fragment.BaseFragment
import com.utt.foodorderapp.listener.IOnManagerFoodListener
import com.utt.foodorderapp.model.Food
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import java.util.*

class AdminHomeFragment : BaseFragment() {

    private var mFragmentAdminHomeBinding: FragmentAdminHomeBinding? = null
    private var mListFood: MutableList<Food>? = null
    private var mAdminFoodAdapter: AdminFoodAdapter? = null
    private var mFoodChildEventListener: ChildEventListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminHomeBinding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        initView()
        initListener()
        getListFood("")
        return mFragmentAdminHomeBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.home))
        }
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminHomeBinding!!.rcvFood.layoutManager = linearLayoutManager
        mListFood = ArrayList()
        mAdminFoodAdapter = AdminFoodAdapter(mListFood, object : IOnManagerFoodListener {
            override fun onClickUpdateFood(food: Food?) {
                onClickEditFood(food)
            }

            override fun onClickDeleteFood(food: Food?) {
                deleteFoodItem(food)
            }
        })
        mFragmentAdminHomeBinding!!.rcvFood.adapter = mAdminFoodAdapter
    }

    private fun initListener() {
        mFragmentAdminHomeBinding!!.btnAddFood.setOnClickListener { onClickAddFood() }
        mFragmentAdminHomeBinding!!.imgSearch.setOnClickListener { searchFood() }
        mFragmentAdminHomeBinding!!.edtSearchName.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood()
                return@setOnEditorActionListener true
            }
            false
        }
        mFragmentAdminHomeBinding!!.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    searchFood()
                }
            }
        })
    }

    private fun onClickAddFood() {
        val currentActivity = activity ?: return
        startActivity(currentActivity, AddFoodActivity::class.java)
    }

    private fun onClickEditFood(food: Food?) {
        val currentActivity = activity ?: return
        val bundle = Bundle()
        bundle.putSerializable(AppConfig.KEY_INTENT_FOOD_OBJECT, food)
        startActivity(currentActivity, AddFoodActivity::class.java, bundle)
    }

    private fun deleteFoodItem(food: Food?) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    if (activity == null) {
                        return@setPositiveButton
                    }
                    ControllerApplication[activity!!].foodDatabaseReference
                            .child(food!!.id.toString()).removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                Toast.makeText(activity,
                                        getString(R.string.msg_delete_movie_successfully), Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun searchFood() {
        val binding = mFragmentAdminHomeBinding ?: return
        val strKey = binding.edtSearchName.text.toString().trim { it <= ' ' }
        if (mListFood != null) {
            mListFood!!.clear()
        } else {
            mListFood = ArrayList()
        }
        getListFood(strKey)
        activity?.let { hideSoftKeyboard(it) }
    }

    private fun getListFood(keyword: String?) {
        val currentActivity = activity ?: return
        val foodReference = ControllerApplication[currentActivity].foodDatabaseReference
        mFoodChildEventListener?.let { listener ->
            foodReference.removeEventListener(listener)
        }
        mFoodChildEventListener = object : ChildEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood == null || mAdminFoodAdapter == null) {
                            return
                        }
                        if (isEmpty(keyword)) {
                            mListFood!!.add(0, food)
                        } else {
                            val searchKey = getTextSearch(keyword).lowercase(Locale.getDefault()).trim { it <= ' ' }
                            val foodName = getTextSearch(food.name).lowercase(Locale.getDefault()).trim { it <= ' ' }
                            val categoryName = getTextSearch(food.categoryName).lowercase(Locale.getDefault()).trim { it <= ' ' }
                            val restaurantName = getTextSearch(food.restaurantName).lowercase(Locale.getDefault()).trim { it <= ' ' }
                            if (foodName.contains(searchKey) || categoryName.contains(searchKey) || restaurantName.contains(searchKey)) {
                                mListFood!!.add(0, food)
                            }
                        }
                        mAdminFoodAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood == null || mListFood!!.isEmpty() || mAdminFoodAdapter == null) {
                            return
                        }
                        for (i in mListFood!!.indices) {
                            if (food.id == mListFood!![i].id) {
                                mListFood!![i] = food
                                break
                            }
                        }
                        mAdminFoodAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood == null || mListFood!!.isEmpty() || mAdminFoodAdapter == null) {
                            return
                        }
                        for (foodObject in mListFood!!) {
                            if (food.id == foodObject.id) {
                                mListFood!!.remove(foodObject)
                                break
                            }
                        }
                        mAdminFoodAdapter!!.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                }
        foodReference.addChildEventListener(mFoodChildEventListener!!)
    }

    override fun onDestroyView() {
        val currentActivity = activity
        if (currentActivity != null && mFoodChildEventListener != null) {
            ControllerApplication[currentActivity].foodDatabaseReference.removeEventListener(mFoodChildEventListener!!)
        }
        mFoodChildEventListener = null
        mFragmentAdminHomeBinding = null
        mAdminFoodAdapter = null
        super.onDestroyView()
    }
}