package io.nthienan.phdiff.differential

import java.util.*

/**
 * Created on 17-Jul-17.
 * @author nthienan
 */
class Differential(var id: String, var phid: String, var revisionID: String) {
    var title: String = ""
    var uri: String = ""
    var dateCreated: Date? = null
    var dateModified: Date? = null
    var authorPHID: String = ""
    var status: Int = 0
    var StatusName: String = ""
    var branch: String = ""
}
