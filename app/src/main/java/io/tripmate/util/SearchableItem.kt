package io.tripmate.util

import android.os.Parcelable

/**
 * Base class for all [SearchableItem]. helps with querying
 * for objects that extend this class
 */
abstract class SearchableItem : Parcelable {
    //For Bus Only
    open var number: String? = null
    open var type: String? = null

    //For Bus, Trip and Ticket
    open var key: String? = null
    open var origin: String? = null
    open var destination: String? = null

}