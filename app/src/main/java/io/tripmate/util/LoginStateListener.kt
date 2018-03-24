package io.tripmate.util

/**
 * Login state listener for new and existing users
 */
interface LoginStateListener {
    fun onLogin()
    fun onLogout()
}