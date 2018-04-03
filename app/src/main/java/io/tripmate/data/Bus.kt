package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import io.tripmate.util.SearchableItem

/**
 * Bus data model
 */
class Bus : SearchableItem {
    override var key: String? = null
    override var number: String? = null
    override var type: String? = null
    var image: String? = null
    var terminalKey: String? = null
    var seats: List<Seat>? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        number = parcel.readString()
        type = parcel.readString()
        image = parcel.readString()
        terminalKey = parcel.readString()
        seats = parcel.createTypedArrayList(Seat)
    }

    constructor()

    constructor(key: String, number: String, type: String, image: String,
                terminalKey: String, seats: List<Seat> = emptyList()) {
        this.key = key
        this.number = number
        this.type = type
        this.image = image
        this.terminalKey = terminalKey
        this.seats = seats
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(number)
        parcel.writeString(type)
        parcel.writeString(image)
        parcel.writeString(terminalKey)
        parcel.writeTypedList(seats)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Bus(key=$key, number=$number, type=$type, image=$image, terminalKey=$terminalKey, seats=$seats)"
    }

    companion object CREATOR : Parcelable.Creator<Bus> {
        override fun createFromParcel(parcel: Parcel): Bus {
            return Bus(parcel)
        }

        override fun newArray(size: Int): Array<Bus?> {
            return arrayOfNulls(size)
        }
    }
}
