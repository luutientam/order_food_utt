package com.utt.foodorderapp.constant

import android.content.Context
import com.utt.foodorderapp.R

object AppConfig {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun requireContext(): Context {
        check(::appContext.isInitialized) { "AppConfig is not initialized." }
        return appContext
    }

    private fun getString(resId: Int): String = requireContext().getString(resId)

    val GENERIC_ERROR: String
        get() = getString(R.string.config_generic_error)
    val CURRENCY: String
        get() = getString(R.string.config_currency)
    val TYPE_PAYMENT_CASH: Int
        get() = requireContext().resources.getInteger(R.integer.config_type_payment_cash)
    val PAYMENT_METHOD_CASH: String
        get() = getString(R.string.config_payment_method_cash)
    val ADMIN_EMAIL_FORMAT: String
        get() = getString(R.string.config_admin_email_format)

    const val KEY_INTENT_FOOD_OBJECT = "food_object"

    val ABOUT_US_TITLE: String
        get() = getString(R.string.config_about_us_title)
    val ABOUT_US_CONTENT: String
        get() = getString(R.string.config_about_us_content)
    val ABOUT_US_SLOGAN: String
        get() = getString(R.string.config_about_us_slogan)
    val ABOUT_US_WEBSITE_TITLE: String
        get() = getString(R.string.config_about_us_website_title)
    val PAGE_FACEBOOK: String
        get() = getString(R.string.config_page_facebook)
    val LINK_FACEBOOK: String
        get() = getString(R.string.config_link_facebook)
    val LINK_YOUTUBE: String
        get() = getString(R.string.config_link_youtube)
    val PHONE_NUMBER: String
        get() = getString(R.string.config_phone_number)
    val GMAIL: String
        get() = getString(R.string.config_gmail)
    val SKYPE_ID: String
        get() = getString(R.string.config_skype_id)
    val ZALO_LINK: String
        get() = getString(R.string.config_zalo_link)
    val WEBSITE: String
        get() = getString(R.string.config_website)
    val FIREBASE_URL: String
        get() = getString(R.string.config_firebase_url)
}
