package com.example.languageribbon_front

import android.app.Application
import com.kakao.sdk.common.KakaoSdk


class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Kakao Sdk 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
    }
}
