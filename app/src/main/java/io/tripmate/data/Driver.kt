package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import io.tripmate.util.User


/**
 * Driver data model
 */
class Driver : User {
    override var key: String? = null
    override var username: String? = null
    override var email: String? = null
    override var phone: String? = null
    override var profile: Profile? = null
    var busKey: String? = null
    var busNumber: String? = null
    var busType: String? = null
    var terminalKey: String? = null
    var isTracking: Boolean = false

    constructor()

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        username = parcel.readString()
        email = parcel.readString()
        phone = parcel.readString()
        profile = parcel.readParcelable(Profile::class.java.classLoader)
        busKey = parcel.readString()
        busNumber = parcel.readString()
        busType = parcel.readString()
        terminalKey = parcel.readString()
        isTracking = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    }

    constructor(key: String?, username: String?, email: String?, phone: String?,
                profile: Profile?, busKey: String?, busNumber: String?, busType: String?,
                terminalKey: String?, isTracking: Boolean = false) {
        this.key = key
        this.username = username
        this.email = email
        this.phone = phone
        this.profile = profile
        this.busKey = busKey
        this.busNumber = busNumber
        this.busType = busType
        this.terminalKey = terminalKey
        this.isTracking = isTracking
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeParcelable(profile, flags)
        parcel.writeString(busKey)
        parcel.writeString(busNumber)
        parcel.writeString(busType)
        parcel.writeString(terminalKey)
        parcel.writeValue(isTracking)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Driver> {
        override fun createFromParcel(parcel: Parcel): Driver {
            return Driver(parcel)
        }

        override fun newArray(size: Int): Array<Driver?> {
            return arrayOfNulls(size)
        }
    }


}
