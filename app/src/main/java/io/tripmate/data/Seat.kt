package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable


/**
 * Seat data model
 */
class Seat : Parcelable {
    var busKey: String? = null
    var number: Int = 0
    var isAvailable: Boolean = false

    constructor(parcel: Parcel) : this() {
        busKey = parcel.readString()
        number = parcel.readInt()
        isAvailable = parcel.readByte() != 0.toByte()
    }

    constructor()

    constructor(busKey: String?, number: Int, isAvailable: Boolean) {
        this.busKey = busKey
        this.number = number
        this.isAvailable = isAvailable
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(busKey)
        parcel.writeInt(number)
        parcel.writeByte(if (isAvailable) 1 else 0)
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
