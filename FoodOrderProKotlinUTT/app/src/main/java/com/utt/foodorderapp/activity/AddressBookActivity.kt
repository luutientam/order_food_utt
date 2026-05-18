package com.utt.foodorderapp.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.R
import com.utt.foodorderapp.adapter.AddressAdapter
import com.utt.foodorderapp.constant.GlobalFunction.showToastMessage
import com.utt.foodorderapp.data.repository.AddressRepository
import com.utt.foodorderapp.databinding.ActivityAddressBookBinding
import com.utt.foodorderapp.model.Address
import com.utt.foodorderapp.prefs.DataStoreManager

/**
 * Sổ địa chỉ giao hàng. Mở ở 2 mode:
 * - Quản lý: từ Tài khoản (mặc định)
 * - Chọn để giao: pass [EXTRA_PICK_MODE]=true. Khi user tap một địa chỉ → trả về qua setResult.
 */
class AddressBookActivity : BaseActivity() {

    private var binding: ActivityAddressBookBinding? = null
    private var adapter: AddressAdapter? = null
    private val items = mutableListOf<Address>()
    private val repository = AddressRepository()
    private var listener: ValueEventListener? = null
    private var pickMode = false
    private var currentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBookBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        currentUid = DataStoreManager.user?.uid
        if (currentUid.isNullOrEmpty()) {
            finish()
            return
        }

        pickMode = intent.getBooleanExtra(EXTRA_PICK_MODE, false)

        binding!!.toolbar.imgBack.visibility = View.VISIBLE
        binding!!.toolbar.imgCart.visibility = View.GONE
        binding!!.toolbar.tvTitle.text = getString(R.string.address_book_title)
        binding!!.toolbar.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding!!.btnAddAddress.setOnClickListener { showAddressForm(null) }
        adapter = AddressAdapter(items, object : AddressAdapter.Listener {
            override fun onSelect(address: Address) {
                if (pickMode) {
                    val result = Intent()
                    result.putExtra(EXTRA_RESULT_ADDRESS, address)
                    setResult(RESULT_OK, result)
                    finish()
                }
            }
            override fun onEdit(address: Address) = showAddressForm(address)
            override fun onDelete(address: Address) = confirmDelete(address)
            override fun onSetDefault(address: Address) = setDefault(address)
        })
        binding!!.rcvAddresses.layoutManager = LinearLayoutManager(this)
        binding!!.rcvAddresses.adapter = adapter

        loadAddresses()
    }

    private fun loadAddresses() {
        val uid = currentUid ?: return
        listener?.let { repository.removeListener(uid, it) }
        listener = repository.observeAddresses(uid, { list ->
            items.clear()
            items.addAll(list)
            adapter?.notifyDataSetChanged()
            binding?.tvEmpty?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding?.rcvAddresses?.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }, { msg ->
            showToastMessage(this, msg)
        })
    }

    private fun showAddressForm(editing: Address?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_address_form, null, false)
        val edtLabel = view.findViewById<EditText>(R.id.edt_label)
        val edtRecipient = view.findViewById<EditText>(R.id.edt_recipient)
        val edtPhone = view.findViewById<EditText>(R.id.edt_phone)
        val edtFullAddress = view.findViewById<EditText>(R.id.edt_full_address)
        val chkDefault = view.findViewById<CheckBox>(R.id.chk_default)

        editing?.let {
            edtLabel.setText(it.label ?: "")
            edtRecipient.setText(it.recipientName ?: "")
            edtPhone.setText(it.phone ?: "")
            edtFullAddress.setText(it.fullAddress ?: "")
            chkDefault.isChecked = it.isDefault
        }

        AlertDialog.Builder(this)
                .setTitle(if (editing == null) R.string.action_add_address else R.string.action_edit)
                .setView(view)
                .setPositiveButton(R.string.action_ok) { _, _ ->
                    val label = edtLabel.text.toString().trim()
                    val recipient = edtRecipient.text.toString().trim()
                    val phone = edtPhone.text.toString().trim()
                    val full = edtFullAddress.text.toString().trim()
                    val isDefault = chkDefault.isChecked
                    if (recipient.isEmpty() || phone.isEmpty() || full.isEmpty()) {
                        showToastMessage(this, getString(R.string.msg_address_required))
                        return@setPositiveButton
                    }
                    val toSave = editing ?: Address()
                    toSave.label = label.ifEmpty { "Địa chỉ" }
                    toSave.recipientName = recipient
                    toSave.phone = phone
                    toSave.fullAddress = full
                    toSave.isDefault = isDefault
                    saveAddress(toSave)
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    private fun saveAddress(address: Address) {
        val uid = currentUid ?: return
        repository.saveAddress(uid, address) { error ->
            if (error != null) {
                showToastMessage(this, getString(R.string.msg_get_date_error))
                return@saveAddress
            }
            if (address.isDefault) {
                repository.setDefault(uid, address.id) {}
            }
            showToastMessage(this, getString(R.string.msg_address_saved))
        }
    }

    private fun confirmDelete(address: Address) {
        AlertDialog.Builder(this)
                .setTitle(R.string.msg_delete_title)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.action_ok) { _, _ ->
                    val uid = currentUid ?: return@setPositiveButton
                    repository.deleteAddress(uid, address.id) { error ->
                        if (error != null) {
                            showToastMessage(this, getString(R.string.msg_get_date_error))
                        }
                    }
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    private fun setDefault(address: Address) {
        val uid = currentUid ?: return
        repository.setDefault(uid, address.id) { error ->
            if (error != null) {
                showToastMessage(this, getString(R.string.msg_get_date_error))
            }
        }
    }

    override fun onDestroy() {
        val uid = currentUid
        if (uid != null) repository.removeListener(uid, listener)
        listener = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PICK_MODE = "EXTRA_PICK_MODE"
        const val EXTRA_RESULT_ADDRESS = "EXTRA_RESULT_ADDRESS"
    }
}
