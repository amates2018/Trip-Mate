package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable

import io.tripmate.util.SearchableItem

/**
 * Seat data model
 */
class Seat : SearchableItem, Parcelable {
    override var key: String? = null
    var booking: Long = 0L
    var trip: Trip? = null
    var isAvailable: Boolean = false
    var uid: String? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        booking = parcel.readLong()
        trip = parcel.readParcelable(Trip::class.java.classLoader)
        isAvailable = parcel.readByte() != 0.toByte()
        uid = parcel.readString()
    }

    constructor() {}

    constructor(key: String, booking: Long, trip: Trip, isAvailable: Boolean, uid: String) {
        this.key = key
        this.booking = booking
        this.trip = trip
        this.isAvailable = isAvailable
        this.uid = uid
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeLong(booking)
        parcel.writeParcelable(trip, flags)
        parcel.writeByte(if (isAvailable) 1 else 0)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Seat> {
        override fun createFromParcel(parcel: Parcel): Seat {
            return Seat(parcel)
        }

        override fun newArray(size: Int): Array<Seat?> {
            return arrayOfNulls(size)
        }
    }
}
