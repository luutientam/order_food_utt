package com.pro.foodorder.activity

import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.pro.foodorder.R
import com.pro.foodorder.databinding.ActivityForgotPasswordBinding
import com.pro.foodorder.utils.StringUtil.isEmpty
import com.pro.foodorder.utils.StringUtil.isValidEmail

class ForgotPasswordActivity : BaseActivity() {

    private var mActivityForgotPasswordBinding: ActivityForgotPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(mActivityForgotPasswordBinding!!.root)
        mActivityForgotPasswordBinding!!.imgBack.setOnClickListener { onBackPressed() }
        mActivityForgotPasswordBinding!!.btnResetPassword.setOnClickListener { onClickValidateResetPassword() }
    }

    private fun onClickValidateResetPassword() {
        val strEmail = mActivityForgotPasswordBinding!!.edtEmail.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(this@ForgotPasswordActivity, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(this@ForgotPasswordActivity, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show()
        } else {
            resetPassword(strEmail)
        }
    }

    private fun resetPassword(email: String) {
        showProgressDialog(true)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task: Task<Void?> ->
                    showProgressDialog(false)
                    if (task.isSuccessful) {
                        Toast.makeText(this@ForgotPasswordActivity,
                                getString(R.string.msg_reset_password_successfully),
                                Toast.LENGTH_SHORT).show()
                        mActivityForgotPasswordBinding!!.edtEmail.setText("")
                    }
                }
    }
}