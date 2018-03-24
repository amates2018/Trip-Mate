package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint

/**
 * Bus terminal data model
 */
class Terminal : Parcelable {
    var key: String? = null
    var location: GeoPoint? = null
    var drivers: List<Driver>? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        drivers = parcel.createTypedArrayList(Driver)
    }

    constructor()

    constructor(key: String, location: GeoPoint, drivers: List<Driver>) {
        this.key = key
        this.location = location
        this.drivers = drivers
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeTypedList(drivers)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Terminal> {
        override fun createFromParcel(parcel: Parcel): Terminal {
            return Terminal(parcel)
        }

        override fun newArray(size: Int): Array<Terminal?> {
            return arrayOfNulls(size)
        }
    }
}
