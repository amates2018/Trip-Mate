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
    var bus: Bus? = null
    var isAvailable: Boolean = false
    var uid: String? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        booking = parcel.readLong()
        bus = parcel.readParcelable(Bus::class.java.classLoader)
        isAvailable = parcel.readByte() != 0.toByte()
        uid = parcel.readString()
    }

    constructor()

    constructor(key: String, booking: Long, bus: Bus?, isAvailable: Boolean, uid: String) {
        this.key = key
        this.booking = booking
        this.bus = bus
        this.isAvailable = isAvailable
        this.uid = uid
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeLong(booking)
        parcel.writeParcelable(bus, flags)
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
