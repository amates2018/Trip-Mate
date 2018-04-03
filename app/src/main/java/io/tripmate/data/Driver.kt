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


    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        username = parcel.readString()
        email = parcel.readString()
        phone = parcel.readString()
        busKey = parcel.readString()
        busNumber = parcel.readString()
        busType = parcel.readString()
        terminalKey = parcel.readString()
        profile = parcel.readParcelable(Profile::class.java.classLoader)
    }

    constructor()

    constructor(key: String?, username: String?, email: String?, phone: String?,
                busKey: String?, busNumber: String?, busType: String?,
                terminalKey: String?, profile: Profile?) {
        this.key = key
        this.username = username
        this.email = email
        this.phone = phone
        this.busKey = busKey
        this.busNumber = busNumber
        this.busType = busType
        this.terminalKey = terminalKey
        this.profile = profile
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(busKey)
        parcel.writeString(busNumber)
        parcel.writeString(busType)
        parcel.writeString(terminalKey)
        parcel.writeParcelable(profile, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Driver(key=$key, username=$username, email=$email, phone=$phone, profile=$profile, busKey=$busKey, busNumber=$busNumber, busType=$busType, terminalKey=$terminalKey)"
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
