package io.tripmate.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView
import io.tripmate.R
import io.tripmate.data.Driver
import io.tripmate.data.Passenger
import io.tripmate.data.Profile
import io.tripmate.util.*

/**
 * [User] account completion
 */
class RegisterActivity : Activity() {
    private val container: ViewGroup by bindView(R.id.container)
    private val avatar: ForegroundImageView by bindView(R.id.user_avatar)
    private val username: TextInputEditText by bindView(R.id.username_content)
    private val phone: TextInputEditText by bindView(R.id.phone_content)
    private val method: AutoCompleteTextView by bindView(R.id.payment_content)
    private val methodLabel: TextInputLayout by bindView(R.id.payment_content_label)
    private val register: Button by bindView(R.id.register_button)

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private lateinit var user: User
    private var imageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Init shared prefs and material dialog
        prefs = TripMatePrefs[this]
        loading = TripMateUtils.getDialog(this@RegisterActivity)

        avatar.setOnClickListener({ openGallery() })

        //get user data from intent
        val intent = intent
        if (intent.hasExtra(EXTRA_USER)) {
            user = intent.getParcelableExtra<User>(EXTRA_USER)
            bindUser()
        }
    }

    private fun setupAutoComplete() {
        //Create array of objects
        val paymentMethods = arrayOf<String>(
                PaymentMethod.MTN_MOMO.value,
                PaymentMethod.VODAFONE_MOMO.value,
                PaymentMethod.AIRTEL_CASH.value,
                PaymentMethod.CREDIT_CARD.value,
                PaymentMethod.TIGO_CASH.value
        )

        //Adapter for dropdown
        val adapter = ArrayAdapter<String>(this@RegisterActivity, android.R.layout.simple_list_item_1, paymentMethods)
        method.setAdapter(adapter)

        if (method.hasFocus()) {
            method.showDropDown()
        }

        method.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                method.showDropDown()
            }
        })
    }

    private fun bindUser() {
        if (user is Passenger) {
            TransitionManager.beginDelayedTransition(container)
            methodLabel.visibility = View.VISIBLE
            setupAutoComplete()
        } else {
            TransitionManager.beginDelayedTransition(container)
            methodLabel.visibility = View.GONE

            //Set hint
            methodLabel.hint = BUS_NUMBER_HINT
            method.hint = BUS_NUMBER_HINT

            //Watches for changes in user input
            //todo: Add in next update
            method.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        username.setText(user.email)
        if (!user.phone.isNullOrEmpty()) phone.setText(user.phone)
    }

    fun register(v: View) {
        //Get values
        val usernameText = username.text.toString()
        val phoneText = phone.text.toString()
        val paymentMethod = method.text.toString()

        when {
        //Incorrect username format?
            usernameText.isEmpty() -> {
                username.error = getString(R.string.prompt_username_error)
                username.requestFocus()
            }

        //Incorrect phone number format?
            phoneText.isEmpty() || phoneText.length < 10 -> {
                phone.error = getString(R.string.prompt_phone_error)
                phone.requestFocus()
            }

        //Incorrect payment method?
            methodLabel.visibility == View.VISIBLE && paymentMethod.isEmpty() -> {
                method.error = getString(R.string.prompt_method_error)
                method.requestFocus()
            }

        //No internet?
            !prefs.isConnected -> {
                showFailed("You need internet connection")
            }

            !imageUri.isNullOrEmpty() -> {
                loading.show()
                uploadImages()
            }

        //todo: add in update
        /*user is Driver && !hasProperBusNumber() -> {

        }*/

        //Ready to go
            else -> {
                loading.show()
                uploadUser()
            }
        }
    }

    private fun hasProperBusNumber(): Boolean {
        val busNumber = method.text.toString()
        //todo: add in update
        return false
    }

    private fun uploadUser() {
        //User is a passenger
        if (user is Passenger) {
            //Cast user to passenger class
            val newUser = user as Passenger
            val passenger = Passenger(newUser.key, username.text.toString(), newUser.email, phone.text.toString(),
                    method.text.toString(), Profile(imageUri, imageUri, imageUri), newUser.location)

            //Upload Passenger
            prefs.db.collection(TripMateUtils.USER_REF)
                    .document(prefs.getAccessToken())
                    .set(passenger)
                    .addOnCompleteListener(this@RegisterActivity, { task ->
                        if (task.isSuccessful) {
                            if (loading.isShowing) loading.dismiss()
                            //Set payment option offline
                            prefs.setPaymentMethod(PaymentMethod.valueOf(method.text.toString()))

                            //Navigate user to home screen
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            intent.putExtra(HomeActivity.EXTRA_USER, passenger)
                            startActivity(intent)
                            finish()
                        }
                    }).addOnFailureListener(this@RegisterActivity, { exception ->
                        showFailed(exception.localizedMessage)
                    })
        } else {
            //Cast user to driver class
            val newUser = user as Driver
            val driver = Driver(newUser.key, newUser.username, newUser.email, phone.text.toString(),
                    Profile(imageUri, imageUri, imageUri), null, method.text.toString(),
                    null, null)

            //Upload Driver
            prefs.db.collection(TripMateUtils.USER_REF)
                    .document(prefs.getAccessToken())
                    .set(driver)
                    .addOnCompleteListener(this@RegisterActivity, { task ->
                        if (task.isSuccessful) {
                            if (loading.isShowing) loading.dismiss()
                            //Set tracking state
                            prefs.enableTracking(false)

                            //
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            intent.putExtra(HomeActivity.EXTRA_USER, driver)
                            startActivity(intent)
                            finish()
                        }
                    }).addOnFailureListener(this@RegisterActivity, { exception ->
                        showFailed(exception.localizedMessage)
                    })
        }

    }

    private fun uploadImages() {
        if (prefs.isLoggedIn) {
            prefs.bucket.getReference(TripMateUtils.USER_REF).child(prefs.getAccessToken() + ".jpg")
                    .putFile(Uri.parse(imageUri))
                    .addOnSuccessListener(this@RegisterActivity, { taskSnapshot ->
                        if (taskSnapshot.task.isSuccessful) {
                            imageUri = taskSnapshot.downloadUrl.toString()
                            Toast.makeText(applicationContext, "Almost done", Toast.LENGTH_SHORT)
                                    .show()
                            uploadUser()
                        }
                    }).addOnFailureListener(this@RegisterActivity, { exception ->
                        showFailed(exception.localizedMessage)
                    })
        }
    }

    private fun showFailed(message: String?) {
        if (loading.isShowing) loading.dismiss()
        Snackbar.make(container, "Drush! $message", Snackbar.LENGTH_LONG).show()
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this@RegisterActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                launchGallery()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQ_CODE)
            }
        } else {
            launchGallery()
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Open with..."), GALLERY_REQ_CODE)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchGallery()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GALLERY_REQ_CODE -> when (resultCode) {
                RESULT_OK -> {
                    //Check intent data
                    if (data != null && data.data != null) {
                        val uri = data.data
                        //Load image int view
                        GlideApp.with(this@RegisterActivity)
                                .load(uri)
                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .transition(withCrossFade())
                                .circleCrop()
                                .into(avatar)
                        imageUri = uri.toString()
                    }
                }
                RESULT_FIRST_USER, RESULT_CANCELED -> { /*NO IMAGE PICKED*/
                }
            }
        }
    }

    companion object {
        const val EXTRA_USER = "EXTRA_USER"
        const val BUS_NUMBER_HINT = "Bus number (eg: GT-1928-18)"
        private const val STORAGE_REQ_CODE = 17
        private const val GALLERY_REQ_CODE = 18
    }
}
