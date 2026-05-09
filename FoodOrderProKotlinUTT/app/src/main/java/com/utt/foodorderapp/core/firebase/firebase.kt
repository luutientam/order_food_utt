package com.example.swipe_photo.core.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


object firebase {

//    var isDebug: Boolean = false
//    var usePremiumKey: Boolean = false
//    var isRemoveAds: Boolean = true

    fun request(onDoneFetch: () -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCanceledListener {}
            .addOnCompleteListener { task: Task<Boolean?> ->
                if (task.isSuccessful) {
                    remoteConfig.activate()

                }
                onDoneFetch.invoke()
            }
    }
}
