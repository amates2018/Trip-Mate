package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import io.tripmate.util.SearchableItem
import java.util.*

/**
 * Passenger's ticket data model
 */
class Ticket : SearchableItem, Parcelable {
    override var key: String? = null
    override var origin: String? = null
    override var destination: String? = null
    var terminalKey: String? = null
    var passengerKey: String? = null
    var timestamp: Date? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        origin = parcel.readString()
        destination = parcel.readString()
        terminalKey = parcel.readString()
        passengerKey = parcel.readString()
        val tmp = parcel.readLong()
        timestamp = if (tmp > -1L) Date(tmp) else null
    }

    constructor()

    constructor(key: String, origin: String, destination: String, terminalKey: String,
                passengerKey: String, timestamp: Date = Date(System.currentTimeMillis())) {
        this.key = key
        this.origin = origin
        this.destination = destination
        this.terminalKey = terminalKey
        this.passengerKey = passengerKey
        this.timestamp = timestamp
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(origin)
        parcel.writeString(destination)
        parcel.writeString(terminalKey)
        parcel.writeString(passengerKey)
        parcel.writeLong(if (timestamp == null) -1L else timestamp!!.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ticket> {
        override fun createFromParcel(parcel: Parcel): Ticket {
            return Ticket(parcel)
        }

        override fun newArray(size: Int): Array<Ticket?> {
            return arrayOfNulls(size)
        }
    }
}
