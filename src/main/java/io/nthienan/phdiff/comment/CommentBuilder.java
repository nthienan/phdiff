package io.nthienan.phdiff.comment;

import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;

public interface CommentBuilder {
    /**
     * Append an object to the report, using its toString() method.
     *
     * @param o object to append
     * @return a reference to this object
     */
    CommentBuilder append(Object o);

    /**
     * Append a severity image.
     *
     * @param severity the severity to display
     * @return a reference to this object
     */
    CommentBuilder append(Severity severity);

    /**
     * Register an "extra issue" (not reported on a diff), without appending.
     * Note that extra issues are not always included in the final rendered report.
     *
     * @param issue     the extra issue to append
     * @return a reference to this object
     */
    CommentBuilder registerExtraIssue(PostJobIssue issue);

    /**
     * Append the registered extra issues.
     *
     * @return a reference to this object
     */
    CommentBuilder appendExtraIssues();
}
