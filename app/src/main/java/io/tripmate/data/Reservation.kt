package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Trip reservations data model: Models user reserved trips
 */
class Reservation : Parcelable {
    var key: String? = null
    var trip: Trip? = null
    var passenger: Passenger? = null
    var timestamp: Date? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        trip = parcel.readParcelable(Trip::class.java.classLoader)
        passenger = parcel.readParcelable(Passenger::class.java.classLoader)
        val tmp = parcel.readLong()
        timestamp = if (tmp > -1L) Date(tmp) else null
    }


    constructor()

    constructor(key: String, trip: Trip, passenger: Passenger, timestamp: Date) {
        this.key = key
        this.trip = trip
        this.passenger = passenger
        this.timestamp = timestamp
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeParcelable(trip, flags)
        parcel.writeParcelable(passenger, flags)
        parcel.writeLong(if (timestamp == null) -1L else timestamp!!.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reservation> {
        override fun createFromParcel(parcel: Parcel): Reservation {
            return Reservation(parcel)
        }

        override fun newArray(size: Int): Array<Reservation?> {
            return arrayOfNulls(size)
        }
    }


}
