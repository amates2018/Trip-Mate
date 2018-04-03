package io.tripmate.util

import android.os.Parcelable
import io.tripmate.data.Profile

/**
 * Base class for all users of the application (This is a list of their commonly shared properties)
 */
abstract class User : Parcelable {
    open var username: String? = null
    open var email: String? = null
    open var key: String? = null
    open var phone: String? = null
    open var profile: Profile? = null
}
