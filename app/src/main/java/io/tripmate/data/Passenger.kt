package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Nullable
import com.google.firebase.firestore.GeoPoint
import io.tripmate.util.User

/**
 * Data model for storing Passenger's data
 */
class Passenger : User {
    override var key: String? = null
    override var username: String? = null
    override var email: String? = null
    override var phone: String? = null
    override var profile: Profile? = null
    var payment: String? = null
    var location: GeoPoint? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        username = parcel.readString()
        email = parcel.readString()
        phone = parcel.readString()
        payment = parcel.readString()
        profile = parcel.readParcelable(Profile::class.java.classLoader)
    }

    //Default constructor for serialization and deserialization
    constructor()

    constructor(key: String?, username: String?, email: String?, phone: String?, payment: String?,
                profile: Profile?, @Nullable location: GeoPoint?) {
        this.key = key
        this.username = username
        this.email = email
        this.phone = phone
        this.payment = payment
        this.profile = profile
        this.location = location
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(payment)
        parcel.writeParcelable(profile, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Passenger(key=$key, username=$username, email=$email, phone=$phone, profile=$profile, payment=$payment, location=$location)"
    }

    companion object CREATOR : Parcelable.Creator<Passenger> {
        override fun createFromParcel(parcel: Parcel): Passenger {
            return Passenger(parcel)
        }

        override fun newArray(size: Int): Array<Passenger?> {
            return arrayOfNulls(size)
        }
    }


}
