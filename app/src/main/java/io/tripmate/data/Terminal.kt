package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint

/**
 * Bus terminal data model
 */
class Terminal : Parcelable {
    var key: String? = null
    var name: String? = null
    var location: GeoPoint? = null
    var drivers: List<Driver>? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        name = parcel.readString()
        drivers = parcel.createTypedArrayList(Driver)
    }

    constructor()

    constructor(key: String,name: String, location: GeoPoint, drivers: List<Driver>) {
        this.key = key
        this.name = name
        this.location = location
        this.drivers = drivers
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(name)
        parcel.writeTypedList(drivers)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Terminal(key=$key, name=$name, location=$location, drivers=$drivers)"
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
