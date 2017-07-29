package io.nthienan.phdiff.issue

import org.sonar.api.batch.postjob.issue.PostJobIssue
import org.sonar.api.batch.rule.Severity
import java.util.Objects

/**
 * Created on 30-Jul-17.
 * @author nthienan
 */
class IssueComparator : Comparator<PostJobIssue> {

  companion object {
    fun compareInt(left: Int?, right: Int?): Int = when {
      left == right -> 0
      left == null -> -1
      right == null -> 1
      else -> left.compareTo(right)
    }

    fun compareComponentKeyAndLine(left: PostJobIssue, right: PostJobIssue): Int {
      return if (left.componentKey() != right.componentKey()) {
        left.componentKey().compareTo(right.componentKey())
      } else {
        compareInt(left.line(), right.line())
      }
    }

    fun compareSeverity(leftSeverity: Severity, rightSeverity: Severity): Int {
      // Display higher severity first. Relies on Severity.ALL to be sorted by severity.
      return if (leftSeverity.ordinal > rightSeverity.ordinal) {
        -1
      } else {
        1
      }
    }
  }

  override fun compare(left: PostJobIssue?, right: PostJobIssue?): Int = when {
    left == right -> 0
    left == null -> 1
    right == null -> -1
    Objects.equals(left.severity(), right.severity()) -> {
      // When severity is the same, sort by component key to at least group issues from
      // the same file together.
      compareComponentKeyAndLine(left, right)
    }
    else -> compareSeverity(left.severity(), right.severity())
  }
}
