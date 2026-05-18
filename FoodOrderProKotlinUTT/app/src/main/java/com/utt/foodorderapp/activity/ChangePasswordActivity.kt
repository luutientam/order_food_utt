package com.utt.foodorderapp.activity

import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.R
import com.utt.foodorderapp.databinding.ActivityChangePasswordBinding
import com.utt.foodorderapp.utils.StringUtil.isEmpty

class ChangePasswordActivity : BaseActivity() {

    private var mActivityChangePasswordBinding: ActivityChangePasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityChangePasswordBinding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(mActivityChangePasswordBinding!!.root)
        mActivityChangePasswordBinding!!.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        mActivityChangePasswordBinding!!.btnChangePassword.setOnClickListener { onClickValidateChangePassword() }
    }

    private fun onClickValidateChangePassword() {
        val strOldPassword = mActivityChangePasswordBinding!!.edtOldPassword.text.toString().trim { it <= ' ' }
        val strNewPassword = mActivityChangePasswordBinding!!.edtNewPassword.text.toString().trim { it <= ' ' }
        val strConfirmPassword = mActivityChangePasswordBinding!!.edtConfirmPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strOldPassword)) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_old_password_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strNewPassword)) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_new_password_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strConfirmPassword)) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_confirm_password_require), Toast.LENGTH_SHORT).show()
        } else if (strNewPassword != strConfirmPassword) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_confirm_password_invalid), Toast.LENGTH_SHORT).show()
        } else if (strOldPassword == strNewPassword) {
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_new_password_invalid), Toast.LENGTH_SHORT).show()
        } else {
            changePassword(strOldPassword, strNewPassword)
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        showProgressDialog(true)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentEmail = currentUser?.email
        if (currentUser == null || currentEmail.isNullOrEmpty()) {
            showProgressDialog(false)
            Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_sign_in_error), Toast.LENGTH_SHORT).show()
            return
        }
        val credential = EmailAuthProvider.getCredential(currentEmail, oldPassword)
        currentUser.reauthenticate(credential).addOnCompleteListener { authTask: Task<Void?> ->
            if (!authTask.isSuccessful) {
                showProgressDialog(false)
                Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_old_password_invalid), Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }
            currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask: Task<Void?> ->
                showProgressDialog(false)
                if (updateTask.isSuccessful) {
                    Toast.makeText(this@ChangePasswordActivity,
                            getString(R.string.msg_change_password_successfully), Toast.LENGTH_SHORT).show()
                    mActivityChangePasswordBinding!!.edtOldPassword.setText("")
                    mActivityChangePasswordBinding!!.edtNewPassword.setText("")
                    mActivityChangePasswordBinding!!.edtConfirmPassword.setText("")
                } else {
                    Toast.makeText(this@ChangePasswordActivity, getString(R.string.msg_sign_in_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}