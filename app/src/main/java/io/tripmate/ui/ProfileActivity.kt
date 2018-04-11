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
import android.support.v4.content.ContextCompat
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import io.peanutsdk.util.bindView
import io.peanutsdk.widget.ForegroundImageView
import io.tripmate.R
import io.tripmate.data.Driver
import io.tripmate.data.Passenger
import io.tripmate.data.Profile
import io.tripmate.util.GlideApp
import io.tripmate.util.TripMatePrefs
import io.tripmate.util.TripMateUtils
import io.tripmate.util.User

/**
 * User profile screen
 */
class ProfileActivity : Activity() {
    //Main
    private val container: ViewGroup by bindView(R.id.main)

    //Container #1
    private val profile: ForegroundImageView by bindView(R.id.profile)
    private val username: TextView by bindView(R.id.username)
    private val email: TextView by bindView(R.id.email)

    //User specific
    private val passengerContainer: ViewGroup by bindView(R.id.username)
    private val driverContainer: ViewGroup by bindView(R.id.username)

    //Location
    private val locationContainer: ViewGroup by bindView(R.id.layout_location)
    private val location: TextView by bindView(R.id.location)

    //Phone
    private val phoneContainer: ViewGroup by bindView(R.id.layout_phone)
    private val phone: TextView by bindView(R.id.phone)

    //Payment
    private val paymentContainer: ViewGroup by bindView(R.id.profile_passenger)
    private val payment: TextView by bindView(R.id.payment)

    //Bus details
    private val terminalContainer: ViewGroup by bindView(R.id.profile_driver)
    private val terminal: TextView by bindView(R.id.terminal)

    //Toolbar
    private val back: ImageButton by bindView(R.id.toolbar_back)
    private val title: TextView by bindView(R.id.toolbar_text)
    private val save: ImageButton by bindView(R.id.toolbar_save)

    private lateinit var prefs: TripMatePrefs
    private lateinit var loading: MaterialDialog
    private var hasStoragePermission: Boolean = false
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Load preferences
        prefs = TripMatePrefs[this]

        //Get loading dialog
        loading = TripMateUtils.getDialog(this@ProfileActivity)

        //Setup toolbar
        title.text = getString(R.string.profile)
        back.setOnClickListener({ onBackPressed() })
        save.setOnClickListener({ doSave() })

        val intent = intent
        if (intent.hasExtra(EXTRA_USER)) {
            val user = intent.getParcelableExtra<User>(EXTRA_USER)
            bindUser(user)
        }

    }

    private fun doSave() {
        val hasImage = imageUri != null
        loading.show()
        //If user has set a profile image
        if (hasImage) {
            uploadImage()
        } else {
            uploadUser()
        }

    }

    //Upload user image to cloud storage bucket
    private fun uploadImage() {
        prefs.bucket.getReference(TripMateUtils.USER_REF).child(prefs.getAccessToken() + ".jpg")
                .putFile(imageUri!!)
                .addOnSuccessListener(this@ProfileActivity, { taskSnapshot ->
                    if (taskSnapshot.task.isSuccessful) {
                        imageUri = taskSnapshot.downloadUrl
                        uploadUser()
                    }
                }).addOnFailureListener(this@ProfileActivity, { exception ->
                    showFailed(exception.localizedMessage)
                })
    }

    private fun showFailed(message: String?) {
        if (loading.isShowing) loading.dismiss()
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun uploadUser() {
        val _username = username.text.toString()
        val _phone = phone.text.toString()
        val _email = email.text.toString()


        //Check internet connection
        if (prefs.isConnected) {
            //Check login state
            if (prefs.isLoggedIn) {
                val accessToken = prefs.getAccessToken()
                //get database reference for all users
                val userRef = prefs.db.collection(TripMateUtils.USER_REF)

                //Set map for user data
                val hashMap = hashMapOf(
                        Pair("username", _username),
                        Pair("email", _email),
                        Pair("phone", _phone),
                        Pair("profile", Profile(imageUri.toString(), imageUri.toString(), imageUri.toString()))
                )

                //Push data to database
                userRef.document(accessToken)
                        .update(hashMap)
                        .addOnCompleteListener(this@ProfileActivity, { task ->
                            if (task.isSuccessful) {
                                loading.dismiss()
                                Toast.makeText(applicationContext, "Your profile has been updated " +
                                        "successfully", Toast.LENGTH_SHORT).show()
                                finishAfterTransition()
                            } else {
                                showFailed(task.exception?.localizedMessage)
                            }
                        })
            }
        } else {
            loading.dismiss()
            Toast.makeText(applicationContext, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    //Bind user data to screen
    //First determine what type of user is available
    private fun bindUser(user: User?) {
        if (user == null) {
            Snackbar.make(container, "No user found", Snackbar.LENGTH_INDEFINITE).show()
            return
        }

        //Load profile image
        GlideApp.with(this@ProfileActivity)
                .load(user.profile?.regular)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.avatar_placeholder)
                .fallback(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .transition(withCrossFade())
                .circleCrop()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(profile)

        //Add profile image edit option
        profile.setOnClickListener({
            if (hasStoragePermission) {
                pickImage()
            } else {
                requestStoragePermission()
            }
        })

        //Load username, email address and phone number
        username.text = user.username?.toLowerCase()
        email.text = user.email
        phone.text = user.phone

        //Add click action for phone, location & username
        phoneContainer.setOnClickListener({ v -> showDialogFor(v) })
        username.setOnClickListener({ v -> showDialogFor(v) })
        terminalContainer.setOnClickListener({ v -> showDialogFor(v) })
        locationContainer.setOnClickListener({ pickLocation() })

        if (user is Passenger) {
            //Do passenger UI
            setupPassengerView(user)
        } else {
            //Do driver UI
            setupDriverView(user as Driver)
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            hasStoragePermission = true
            pickImage()
        } else {
            if (ContextCompat.checkSelfPermission(this@ProfileActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                hasStoragePermission = false
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQ_CODE)
            } else {
                hasStoragePermission = true
                pickImage()
            }
        }
    }

    //Pick images from user's gallery
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Pick your profile image"), GALLERY_REQ_CODE)
    }

    private fun showDialogFor(v: View?) {
        if (v != null) {
            val customView: View = layoutInflater.inflate(R.layout.custom_edit_text, null, false)
            val edtContent = customView.findViewById<EditText>(R.id.edit_text)
            val content = edtContent.text.toString()

            MaterialDialog.Builder(this@ProfileActivity)
                    .theme(Theme.LIGHT)
                    .customView(customView, true)
                    .positiveText("Ok")
                    .negativeText("Cancel")
                    .onNegative({ dialog, _ ->
                        dialog.dismiss()
                    })
                    .onPositive({ dialog, _ ->
                        dialog.dismiss()
                        if (!content.isEmpty()) {
                            when (v.id) {
                                R.id.username -> {
                                    username.text = content
                                }
                                R.id.layout_phone -> {
                                    phone.text = content
                                }
                                R.id.profile_driver -> {

                                }
                                R.id.profile_passenger -> {
                                    
                                }
                                else -> return@onPositive
                            }
                        }
                    })
                    .build()
        }
    }

    private fun pickLocation() {
        try {
            val locationIntent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(AutocompleteFilter.Builder()
                            .setCountry("GH")
                            .build())
                    .build(this@ProfileActivity)
            startActivityForResult(locationIntent, LOCATION_REQ_RESULT)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    //Setup screen for driver
    private fun setupDriverView(driver: Driver) {
        TransitionManager.beginDelayedTransition(container)
        terminalContainer.visibility = View.VISIBLE
    }

    //Setup screen for passenger
    private fun setupPassengerView(passenger: Passenger) {
        //passenger specific details
        TransitionManager.beginDelayedTransition(container)
        paymentContainer.visibility = ViewGroup.VISIBLE

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                LOCATION_REQ_RESULT -> {
                    if (data != null) {
                        val place = PlaceAutocomplete.getPlace(this@ProfileActivity, data)
                        location.text = place.address
                    }
                }
                GALLERY_REQ_CODE -> {
                    if (data != null) {
                        imageUri = data.data

                        GlideApp.with(this@ProfileActivity)
                                .load(imageUri)
                                .placeholder(R.drawable.avatar_placeholder)
                                .transition(withCrossFade())
                                .circleCrop()
                                //.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .into(profile)
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasStoragePermission = true
            pickImage()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestStoragePermission()
        }
    }

    companion object {
        const val EXTRA_USER = "EXTRA_USER"
        private const val LOCATION_REQ_RESULT = 53
        private const val GALLERY_REQ_CODE = 54
        private const val STORAGE_REQ_CODE = 55
    }
}
