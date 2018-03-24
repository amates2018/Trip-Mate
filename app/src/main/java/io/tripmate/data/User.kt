package io.tripmate.data

/**
 * Base class for all users of the application (This is a list of their commonly shared properties)
 */
interface User {
    var username: String?
    var email: String?
    var key: String?
    var phone: String?
    var profile: Profile?
}
