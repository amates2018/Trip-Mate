package io.tripmate.data


import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import io.tripmate.util.SearchableItem

/**
 * User's trip data model
 */
class Trip : Parcelable, SearchableItem {
    override var key: String? = null
    override var origin: String? = null
    override var destination: String? = null
    var driver: Driver? = null
    var bus: Bus? = null
    private var oGeoPoint: GeoPoint? = null
    private var dGeoPoint: GeoPoint? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        origin = parcel.readString()
        destination = parcel.readString()
        driver = parcel.readParcelable(Driver::class.java.classLoader)
        bus = parcel.readParcelable(Bus::class.java.classLoader)
    }

    constructor()

    constructor(key: String, origin: String, destination: String,
                driver: Driver, bus: Bus, oGeoPoint: GeoPoint, dGeoPoint: GeoPoint) {
        this.key = key
        this.origin = origin
        this.destination = destination
        this.driver = driver
        this.bus = bus
        this.oGeoPoint = oGeoPoint
        this.dGeoPoint = dGeoPoint
    }

    fun getoGeoPoint(): GeoPoint? {
        return oGeoPoint
    }

    fun setoGeoPoint(oGeoPoint: GeoPoint) {
        this.oGeoPoint = oGeoPoint
    }

    fun getdGeoPoint(): GeoPoint? {
        return dGeoPoint
    }

    fun setdGeoPoint(dGeoPoint: GeoPoint) {
        this.dGeoPoint = dGeoPoint
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(origin)
        parcel.writeString(destination)
        parcel.writeParcelable(driver, flags)
        parcel.writeParcelable(bus, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Trip> {
        override fun createFromParcel(parcel: Parcel): Trip {
            return Trip(parcel)
        }

        override fun newArray(size: Int): Array<Trip?> {
            return arrayOfNulls(size)
        }
    }
}
