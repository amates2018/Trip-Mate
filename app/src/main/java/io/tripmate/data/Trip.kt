package io.tripmate.data


import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import io.tripmate.util.SearchableItem

/**
 * User's trip data model
 */
class Trip : SearchableItem {
    override var key: String? = null
    override var origin: String? = null
    override var destination: String? = null
    var duration: Double = 0.00
    var driver: Driver? = null
    var bus: Bus? = null
    var price: Double = 0.00
    private var oGeoPoint: GeoPoint? = null
    private var dGeoPoint: GeoPoint? = null

    constructor(parcel: Parcel) : this() {
        key = parcel.readString()
        origin = parcel.readString()
        destination = parcel.readString()
        duration = parcel.readDouble()
        price = parcel.readDouble()
        driver = parcel.readParcelable(Driver::class.java.classLoader)
        bus = parcel.readParcelable(Bus::class.java.classLoader)
    }

    constructor()

    constructor(key: String?, origin: String?, destination: String?,
                driver: Driver?, bus: Bus?, price: Double, duration: Double, oGeoPoint: GeoPoint,
                dGeoPoint: GeoPoint?) {
        this.key = key
        this.origin = origin
        this.destination = destination
        this.price = price
        this.duration = duration
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
        parcel.writeDouble(price)
        parcel.writeDouble(duration)
        parcel.writeParcelable(driver, flags)
        parcel.writeParcelable(bus, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Trip) return false

        if (key != other.key) return false
        if (origin != other.origin) return false
        if (destination != other.destination) return false
        if (duration != other.duration) return false
        if (driver != other.driver) return false
        if (bus != other.bus) return false
        if (price != other.price) return false
        if (oGeoPoint != other.oGeoPoint) return false
        if (dGeoPoint != other.dGeoPoint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (origin?.hashCode() ?: 0)
        result = 31 * result + (destination?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + (driver?.hashCode() ?: 0)
        result = 31 * result + (bus?.hashCode() ?: 0)
        result = 31 * result + price.hashCode()
        result = 31 * result + (oGeoPoint?.hashCode() ?: 0)
        result = 31 * result + (dGeoPoint?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Trip(key=$key, origin=$origin, destination=$destination, duration=$duration, driver=$driver, bus=$bus, price=$price, oGeoPoint=$oGeoPoint, dGeoPoint=$dGeoPoint)"
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
