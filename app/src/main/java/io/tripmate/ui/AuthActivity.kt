package io.tripmate.ui

import android.app.Activity
import android.os.Bundle
import io.tripmate.R

class AuthActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }
}
