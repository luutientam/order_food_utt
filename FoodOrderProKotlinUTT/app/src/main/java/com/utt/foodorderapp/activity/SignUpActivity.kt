@file:Suppress("DEPRECATION")

package com.utt.foodorderapp.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.utt.foodorderapp.R
import com.utt.foodorderapp.constant.GlobalFunction.gotoMainActivity
import com.utt.foodorderapp.data.repository.AuthRepository
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
    private var googleSelectedRole: String = com.utt.foodorderapp.model.User.ROLE_CUSTOMER
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
            authViewModel.signUpWithGoogle(idToken, googleSelectedRole)
        } catch (exception: ApiException) {
            showGoogleSignUpError(exception)
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
        val selectedRole = getSelectedRole()
        if (isEmpty(strEmail)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show()
        } else if (isEmpty(strPassword)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_password_require), Toast.LENGTH_SHORT).show()
        } else if (!isValidEmail(strEmail)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show()
        } else if (strPassword.length < PASSWORD_MIN_LENGTH) {
            val message = resolveStringByName("msg_password_too_short").ifEmpty { PASSWORD_TOO_SHORT_MESSAGE }
            Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
        } else if (selectedRole == com.utt.foodorderapp.model.User.ROLE_ADMIN && !strEmail.endsWith(ADMIN_EMAIL_DOMAIN)) {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_email_invalid_admin), Toast.LENGTH_SHORT).show()
        } else {
            signUpUser(strEmail, strPassword, selectedRole)
        }
    }

    private fun signUpUser(email: String, password: String, selectedRole: String) {
        mActivitySignUpBinding!!.btnSignUp.tag = selectedRole
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
        val webClientId = resolveGoogleWebClientId()
        if (webClientId.isEmpty()) {
            googleSignInClient = null
            return
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun resolveGoogleWebClientId(): String {
        val defaultWebClientId = resolveStringByName("default_web_client_id")
        if (defaultWebClientId.isNotEmpty()) {
            return defaultWebClientId
        }
        val manualWebClientId = resolveStringByName("google_web_client_id")
        if (manualWebClientId.isNotEmpty() && !manualWebClientId.contains("YOUR_GOOGLE_WEB_CLIENT_ID")) {
            return manualWebClientId
        }
        return ""
    }

    private fun resolveStringByName(name: String): String {
        val stringResId = resources.getIdentifier(name, "string", packageName)
        if (stringResId == 0) return ""
        return getString(stringResId).trim()
    }

    private fun onClickGoogleSignUp() {
        val client = googleSignInClient
        if (client == null) {
            showGoogleNotConfigured()
            return
        }
        if (getSelectedRole() == com.utt.foodorderapp.model.User.ROLE_ADMIN) {
            val message = resolveStringByName("msg_google_admin_not_supported").ifEmpty { GOOGLE_ADMIN_NOT_SUPPORTED_MESSAGE }
            Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
            return
        }
        googleSelectedRole = getSelectedRole()
        launchGoogleChooser(client)
    }

    private fun observeSignUpState() {
        authViewModel.signUpState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showProgressDialog(true)
                is UiState.Error -> {
                    showProgressDialog(false)
                    showAuthError(state.message, isSignUp = true)
                }
                is UiState.Success -> {
                    showProgressDialog(false)
                    val selectedRole = mActivitySignUpBinding!!.btnSignUp.tag as? String ?: com.utt.foodorderapp.model.User.ROLE_CUSTOMER
                    if (state.data.role != selectedRole) {
                        clearCurrentSession()
                        Toast.makeText(this@SignUpActivity, getString(R.string.msg_sign_in_wrong_role), Toast.LENGTH_SHORT).show()
                        return@observe
                    }
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
                    if (state.message == "locked-account") {
                        Toast.makeText(this@SignUpActivity, getString(R.string.msg_account_locked), Toast.LENGTH_SHORT).show()
                    } else {
                        showAuthError(state.message, isSignUp = true)
                    }
                }
                is UiState.Success -> {
                    showProgressDialog(false)
                    if (state.data.role != googleSelectedRole) {
                        clearCurrentSession()
                        Toast.makeText(this@SignUpActivity, getString(R.string.msg_sign_in_wrong_role), Toast.LENGTH_SHORT).show()
                        return@observe
                    }
                    DataStoreManager.user = state.data
                    gotoMainActivity(this)
                    finishAffinity()
                }
            }
        }
    }

    private fun showGoogleNotConfigured() {
        val message = resolveStringByName("msg_google_not_configured").ifEmpty { GOOGLE_NOT_CONFIGURED_MESSAGE }
        Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showGoogleSignUpError(exception: ApiException? = null) {
        val message = when (exception?.statusCode) {
            CommonStatusCodes.DEVELOPER_ERROR -> GOOGLE_DEVELOPER_ERROR_MESSAGE
            12500 -> GOOGLE_OAUTH_CONFIG_ERROR_MESSAGE
            12501 -> GOOGLE_SIGN_IN_CANCELLED_MESSAGE
            else -> resolveStringByName("msg_google_sign_up_error").ifEmpty { GOOGLE_SIGN_UP_ERROR_MESSAGE }
        }
        Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAuthError(error: String, isSignUp: Boolean) {
        val message = when (error) {
            AuthRepository.ERROR_INVALID_EMAIL -> getString(R.string.msg_email_invalid)
            AuthRepository.ERROR_EMAIL_ALREADY_IN_USE -> resolveStringByName("msg_sign_up_email_exists")
                    .ifEmpty { getString(R.string.msg_sign_up_error) }
            AuthRepository.ERROR_INVALID_CREDENTIALS -> if (isSignUp) {
                getString(R.string.msg_sign_up_error)
            } else {
                resolveStringByName("msg_sign_in_invalid_credentials").ifEmpty { getString(R.string.msg_sign_in_error) }
            }
            AuthRepository.ERROR_USER_DISABLED -> getString(R.string.msg_account_locked)
            AuthRepository.ERROR_TOO_MANY_REQUESTS -> resolveStringByName("msg_auth_too_many_requests")
                    .ifEmpty { getString(R.string.msg_sign_up_error) }
            AuthRepository.ERROR_NETWORK -> resolveStringByName("msg_auth_network_error")
                    .ifEmpty { getString(R.string.msg_sign_up_error) }
            else -> ""
        }
        if (message.isNotEmpty()) {
            Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
            return
        }
        if (error.length < 120) {
            Toast.makeText(this@SignUpActivity, error, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@SignUpActivity, getString(R.string.msg_sign_up_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGoogleChooser(client: GoogleSignInClient) {
        clearCurrentSession()
        client.signOut().addOnCompleteListener {
            googleSignUpLauncher.launch(client.signInIntent)
        }
    }

    private fun clearCurrentSession() {
        FirebaseAuth.getInstance().signOut()
        DataStoreManager.user = null
    }

    companion object {
        private const val PASSWORD_MIN_LENGTH = 6
        private const val ADMIN_EMAIL_DOMAIN = "@admin.com"
        private const val PASSWORD_TOO_SHORT_MESSAGE = "Mat khau phai co it nhat 6 ky tu"
        private const val GOOGLE_ADMIN_NOT_SUPPORTED_MESSAGE = "Khong ho tro dang ky Google cho tai khoan quan tri"
        private const val GOOGLE_NOT_CONFIGURED_MESSAGE = "Google Sign-In chua cau hinh"
        private const val GOOGLE_SIGN_UP_ERROR_MESSAGE = "Dang ky Google that bai"
        private const val GOOGLE_DEVELOPER_ERROR_MESSAGE = "Google Sign-In loi cau hinh SHA-1/SHA-256 hoac client ID"
        private const val GOOGLE_OAUTH_CONFIG_ERROR_MESSAGE = "Google Sign-In loi OAuth. Hay kiem tra Firebase Auth + google-services.json"
        private const val GOOGLE_SIGN_IN_CANCELLED_MESSAGE = "Ban da huy dang nhap Google"
    }
}