package io.nthienan.phdiff.differential

import java.util.Date

/**
 * Created on 18/07/2017.
 * @author nthienan
 */
class Diff {
  var id: String = ""
  var revisionId: String = ""
  var dateCreated: Date? = null
  var dateModified: Date? = null
  var branch: String = ""
  var unitStatus: Int = 0
  var lintStatus: Int = 0

  fun getFormatedRevisionID(): String {
    return "D$revisionId"
  }
}
