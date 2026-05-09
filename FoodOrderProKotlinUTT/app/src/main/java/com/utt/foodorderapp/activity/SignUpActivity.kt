package com.utt.foodorderapp.activity

import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.AppConfig
import com.utt.foodorderapp.constant.GlobalFunction.gotoMainActivity
import com.utt.foodorderapp.databinding.ActivitySignUpBinding
import com.utt.foodorderapp.model.User
import com.utt.foodorderapp.prefs.DataStoreManager
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import com.utt.foodorderapp.utils.StringUtil.isValidEmail

class SignUpActivity : BaseActivity() {

    private var mActivitySignUpBinding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mActivitySignUpBinding!!.root)
        mActivitySignUpBinding!!.rdbUser.isChecked = true
        mActivitySignUpBinding!!.imgBack.setOnClickListener { onBackPressed() }
        mActivitySignUpBinding!!.layoutSignIn.setOnClickListener { finish() }
        mActivitySignUpBinding!!.btnSignUp.setOnClickListener { onClickValidateSignUp() }
    }

    private fun onClickValidateSignUp() {
        val strEmail = mActivitySignUpBinding!!.edtEmail.text.toString().trim { it <= ' ' }
        val strPassword = mActivitySignUpBinding!!.edtPassword.text.toString().trim { it <= ' ' }
        if (isEmpty(strEmail)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_password_require), Toast.LENGTH_SHORT).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show()
        } else {
            if (mActivitySignUpBinding!!.rdbAdmin.isChecked) {
                if (!strEmail.contains(AppConfig.ADMIN_EMAIL_FORMAT)) {
                    Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_invalid_admin), Toast.LENGTH_SHORT).show()
                } else {
                    signUpUser(strEmail, strPassword)
                }
                return
            }
            if (strEmail.contains(AppConfig.ADMIN_EMAIL_FORMAT)) {
                Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_invalid_user), Toast.LENGTH_SHORT).show()
            } else {
                signUpUser(strEmail, strPassword)
            }
        }
    }

    private fun signUpUser(email: String, password: String) {
        showProgressDialog(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    showProgressDialog(false)
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            val userObject = User(user.email, password)
                            if (user.email != null && user.email!!.contains(AppConfig.ADMIN_EMAIL_FORMAT)) {
                                userObject.isAdmin = true
                            }
                            DataStoreManager.user = userObject
                            gotoMainActivity(this)
                            finishAffinity()
                        }
                    } else {
                        Toast.makeText(this@SignUpActivity, getString(R.string.msg_sign_up_error),
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}