package io.tripmate.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.PasswordEntry
import io.tripmate.R
import io.tripmate.api.AllUserDataManager
import io.tripmate.data.Driver
import io.tripmate.data.Passenger
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User
import io.tripmate.util.UserType

/**
 * Authentication for users
 */
class AuthActivity : Activity() {
    private val container: ViewGroup by bindView(R.id.container)
    private val username: TextInputEditText by bindView(R.id.username_content)
    private val password: PasswordEntry by bindView(R.id.password_content)
    private val resetPassword: Button by bindView(R.id.forgot_password)
    private val login: Button by bindView(R.id.login_button)

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private lateinit var dataManager: AllUserDataManager
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //Init preferences
        prefs = TripMatePrefs[this]

        //Setup dialog
        loading = TripMateUtils.getDialog(this@AuthActivity)


        dataManager = object : AllUserDataManager(this) {
            override fun onDataLoaded(data: List<User>) {
                if (data.isEmpty()) {
                    showOptionsDialog()
                    return
                } else {
                    val usernameText = username.text.toString()
                    for (newUser in data) {
                        if (!newUser.username.isNullOrEmpty() && newUser.username!! == usernameText
                                || !newUser.email.isNullOrEmpty() && newUser.email!! == usernameText) {
                            user = newUser
                            try {
                                prefs.auth.signInWithEmailAndPassword(newUser.email!!, password.text.toString())
                                        .addOnCompleteListener(this@AuthActivity, { task ->
                                            if (task.isSuccessful) {
                                                loading.dismiss()
                                                if (newUser is Driver) prefs.setAccessToken(newUser.key, UserType.TYPE_DRIVER)
                                                else prefs.setAccessToken(newUser.key, UserType.TYPE_PASSENGER)
                                            }
                                        }).addOnFailureListener(this@AuthActivity, { exception ->
                                            showLoginFailed(exception.localizedMessage)
                                        })
                            } catch (e: Exception) {
                                showLoginFailed(e.localizedMessage)
                            }
                            break
                        } else {
                            showOptionsDialog()
                            break
                        }
                    }


                    //Dismiss loading dialog
                    loading.dismiss()
                    //Check user login state
                    if (prefs.isLoggedIn) {
                        //Parse user item and send data to HomeActivity
                        val homeIntent = Intent(this@AuthActivity, HomeActivity::class.java)
                        homeIntent.putExtra(HomeActivity.EXTRA_USER, user)
                        startActivity(homeIntent)
                    } else {
                        showLoginFailed("Oops! Cannot sign in user. Please try again")
                    }

                }
            }
        }
    }

    private fun showOptionsDialog() {
        MaterialDialog.Builder(this@AuthActivity)
                .theme(Theme.DARK)
                .title("Oops...")
                .content("Account details for \"${username.text}\" does not exist")
                .positiveText("Register(Driver)")
                .negativeText("Register(Passenger)")
                .neutralText("Try Again")
                .onNegative({ dialog, _ ->
                    dialog.dismiss()
                    val usernameText = username.text.toString()
                    val passwordText = password.text.toString()
                    if (Patterns.EMAIL_ADDRESS.matcher(usernameText).matches()) {
                        createAccount(usernameText, passwordText)
                    } else {
                        username.error = "You need to enter a valid email address"
                        username.requestFocus()
                    }
                })
                .onPositive({ dialog, _ ->
                    dialog.dismiss()
                    val usernameText = username.text.toString()
                    val passwordText = password.text.toString()
                    if (Patterns.EMAIL_ADDRESS.matcher(usernameText).matches()) {
                        createAccount(usernameText, passwordText, true)
                    } else {
                        showLoginFailed("You need to enter a valid email address")
                        username.requestFocus()
                    }
                }).onNeutral { dialog, _ ->
                    dialog.dismiss()
                }
                .build().show()
    }

    override fun onDestroy() {
        dataManager.cancelLoading()
        super.onDestroy()
    }

    //Creates a new user account
    private fun createAccount(usernameText: String, passwordText: String, isDriver: Boolean = false) {
        loading.show()
        prefs.auth.createUserWithEmailAndPassword(usernameText, passwordText)
                .addOnCompleteListener(this@AuthActivity, { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result.user //get user
                        val key = firebaseUser.uid  //User's unique key
                        val document = prefs.db.collection(TripMateUtils.USER_REF)
                                .document(key)

                        //Check whether or not user is a driver
                        if (isDriver) {
                            //user is a driver
                            val driver = Driver(key, null, usernameText, firebaseUser.phoneNumber,
                                    null, null, null, null, null)
                            document.set(driver)
                                    .addOnCompleteListener(this@AuthActivity, { regTask ->
                                        if (regTask.isSuccessful) {
                                            loading.dismiss()
                                            prefs.setAccessToken(key, UserType.TYPE_DRIVER)
                                            val intent = Intent(this@AuthActivity, RegisterActivity::class.java)
                                            intent.putExtra(RegisterActivity.EXTRA_USER, driver)
                                            startActivity(intent)
                                            finish()
                                        }
                                    })
                        } else {
                            //User is a passenger
                            val passenger = Passenger(key, null, usernameText,
                                    firebaseUser.phoneNumber, null, null, null)
                            document.set(passenger)
                                    .addOnCompleteListener(this@AuthActivity, { regTask ->
                                        if (regTask.isSuccessful) {
                                            loading.dismiss()
                                            prefs.setAccessToken(key, UserType.TYPE_PASSENGER)
                                            val intent = Intent(this@AuthActivity, RegisterActivity::class.java)
                                            intent.putExtra(RegisterActivity.EXTRA_USER, passenger)
                                            startActivity(intent)
                                            finish()
                                        }
                                    })
                        }

                    }
                })
    }

    private fun showLoginFailed(message: String?) {
        if (loading.isShowing) loading.dismiss()
        if (message == null) {
            Toast.makeText(this@AuthActivity, message, Toast.LENGTH_LONG).show()
        } else {
            Snackbar.make(container, message, Snackbar.LENGTH_LONG).show()
        }
    }

    fun doLogin(v: View) {
        //Get values
        val usernameText = username.text.toString()
        val passwordText = password.text.toString()

        when {
        //Incorrect username format?
            usernameText.isEmpty() -> {
                showLoginFailed(getString(R.string.prompt_username_error))
                username.requestFocus()
            }

        //Incorrect password format?
            passwordText.isEmpty() || passwordText.length < 6 || TextUtils.isDigitsOnly(passwordText) -> {
                showLoginFailed(getString(R.string.prompt_password_error))
                password.requestFocus()
            }

        //No internet?
            !prefs.isConnected -> {
                showLoginFailed("You need internet connection")
            }

        //Ready to go
            else -> {
                loading.show()
                dataManager.loadAllUsers()
            }
        }
    }

    fun resetPassword(v: View) {
        val view = layoutInflater.inflate(R.layout.password_reset_layout, null, false)
        val content = view.findViewById<TextInputEditText>(R.id.custom_content)
        MaterialDialog.Builder(this@AuthActivity)
                .theme(Theme.LIGHT)
                .customView(view, false)
                .positiveText("Reset")
                .negativeText("Cancel")
                .onPositive({ dialog, _ ->
                    val contentText = content.text.toString()
                    when {
                    //No email entered?
                        contentText.isEmpty() -> {
                            Toast.makeText(applicationContext, "Enter your email address", Toast.LENGTH_SHORT)
                                    .show()
                            content.requestFocus()
                        }

                    //Email badly formatted?
                        !Patterns.EMAIL_ADDRESS.matcher(contentText).matches() -> {
                            Toast.makeText(applicationContext, "Invalid email address format",
                                    Toast.LENGTH_SHORT).show()
                            content.requestFocus()
                        }

                    //Send reset email to @contentText
                        else -> {
                            dialog.dismiss()
                            prefs.auth.sendPasswordResetEmail(contentText)
                                    .addOnCompleteListener(this@AuthActivity, { task ->
                                        loading.dismiss()
                                        if (task.isSuccessful) {
                                            showLoginFailed("Email sent to \"$contentText\" successfully")
                                            username.requestFocus()
                                        } else {
                                            showLoginFailed(task.exception?.localizedMessage)
                                        }
                                    }).addOnFailureListener(this@AuthActivity, { exception ->
                                        showLoginFailed(exception.localizedMessage)
                                    })
                        }
                    }
                })
                .onNegative({ dialog, _ ->
                    dialog.dismiss()
                })
                .build()
                .show()
    }

}
