package io.tripmate.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Profile data model for storing a user's profile image in various resolutions
 */
class Profile : Parcelable {
    var regular: String? = null
    var thumb: String? = null
    var cover: String? = null

    constructor(parcel: Parcel) : this() {
        regular = parcel.readString()
        thumb = parcel.readString()
        cover = parcel.readString()
    }

    constructor()

    constructor(regular: String?, thumb: String?, cover: String?) {
        this.regular = regular
        this.thumb = thumb
        this.cover = cover
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(regular)
        parcel.writeString(thumb)
        parcel.writeString(cover)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Profile(regular=$regular, thumb=$thumb, cover=$cover)"
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }
}
