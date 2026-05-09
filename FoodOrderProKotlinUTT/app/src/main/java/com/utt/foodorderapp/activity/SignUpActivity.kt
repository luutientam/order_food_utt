package com.utt.foodorderapp.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.GlobalFunction.gotoMainActivity
import com.utt.foodorderapp.databinding.ActivitySignUpBinding
import com.utt.foodorderapp.prefs.DataStoreManager
import com.utt.foodorderapp.presentation.auth.AuthViewModel
import com.utt.foodorderapp.presentation.common.UiState
import com.utt.foodorderapp.utils.StringUtil.isEmpty
import com.utt.foodorderapp.utils.StringUtil.isValidEmail

class SignUpActivity : BaseActivity() {

    private var mActivitySignUpBinding: ActivitySignUpBinding? = null
    private val authViewModel: AuthViewModel by viewModels()
    private var googleSignInClient: GoogleSignInClient? = null
    private val googleSignUpLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = result.data ?: run {
            showGoogleSignUpError()
            return@registerForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrEmpty()) {
                showGoogleNotConfigured()
                return@registerForActivityResult
            }
            authViewModel.signUpWithGoogle(idToken, getSelectedRole())
        } catch (exception: ApiException) {
            showGoogleSignUpError()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mActivitySignUpBinding!!.root)
        mActivitySignUpBinding!!.rdbUser.isChecked = true
        mActivitySignUpBinding!!.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        mActivitySignUpBinding!!.layoutSignIn.setOnClickListener { finish() }
        mActivitySignUpBinding!!.btnSignUp.setOnClickListener { onClickValidateSignUp() }
        val googleButtonId = resources.getIdentifier("btn_sign_up_google", "id", packageName)
        mActivitySignUpBinding!!.root.findViewById<Button>(googleButtonId)?.setOnClickListener { onClickGoogleSignUp() }
        setupGoogleSignIn()
        observeSignUpState()
        observeGoogleSignUpState()
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
            signUpUser(strEmail, strPassword)
        }
    }

    private fun signUpUser(email: String, password: String) {
        val selectedRole = getSelectedRole()
        authViewModel.signUp(email, password, selectedRole)
    }

    private fun getSelectedRole(): String {
        return when {
            mActivitySignUpBinding!!.rdbAdmin.isChecked -> com.utt.foodorderapp.model.User.ROLE_ADMIN
            mActivitySignUpBinding!!.rdbShipper.isChecked -> com.utt.foodorderapp.model.User.ROLE_SHIPPER
            else -> com.utt.foodorderapp.model.User.ROLE_CUSTOMER
        }
    }

    private fun setupGoogleSignIn() {
        if (GOOGLE_WEB_CLIENT_ID.isEmpty() || GOOGLE_WEB_CLIENT_ID.contains("YOUR_GOOGLE_WEB_CLIENT_ID")) {
            googleSignInClient = null
            return
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun onClickGoogleSignUp() {
        val client = googleSignInClient
        if (client == null) {
            showGoogleNotConfigured()
            return
        }
        googleSignUpLauncher.launch(client.signInIntent)
    }

    private fun observeSignUpState() {
        authViewModel.signUpState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showProgressDialog(true)
                is UiState.Error -> {
                    showProgressDialog(false)
                    Toast.makeText(this@SignUpActivity, getString(R.string.msg_sign_up_error), Toast.LENGTH_SHORT).show()
                }
                is UiState.Success -> {
                    showProgressDialog(false)
                    DataStoreManager.user = state.data
                    gotoMainActivity(this)
                    finishAffinity()
                }
            }
        }
    }

    private fun observeGoogleSignUpState() {
        authViewModel.googleSignUpState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showProgressDialog(true)
                is UiState.Error -> {
                    showProgressDialog(false)
                    if (state.message == "google-account-exists") {
                        Toast.makeText(this@SignUpActivity, GOOGLE_ACCOUNT_EXISTS_MESSAGE, Toast.LENGTH_SHORT).show()
                    } else {
                        showGoogleSignUpError()
                    }
                }
                is UiState.Success -> {
                    showProgressDialog(false)
                    DataStoreManager.user = state.data
                    gotoMainActivity(this)
                    finishAffinity()
                }
            }
        }
    }

    private fun showGoogleNotConfigured() {
        Toast.makeText(this@SignUpActivity, GOOGLE_NOT_CONFIGURED_MESSAGE, Toast.LENGTH_SHORT).show()
    }

    private fun showGoogleSignUpError() {
        Toast.makeText(this@SignUpActivity, GOOGLE_SIGN_UP_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val GOOGLE_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID"
        private const val GOOGLE_NOT_CONFIGURED_MESSAGE = "Google Sign-In chua cau hinh"
        private const val GOOGLE_SIGN_UP_ERROR_MESSAGE = "Dang ky Google that bai"
        private const val GOOGLE_ACCOUNT_EXISTS_MESSAGE = "Tai khoan Google da ton tai, vui long dang nhap"
    }
}