package io.nthienan.phdiff.report;

import org.sonar.api.batch.postjob.issue.PostJobIssue;

public interface GlobalReportBuilder {

    /**
     * Append an object to the report, using its toString() method.
     *
     * @param o object to append
     * @return a reference to this object
     */
    GlobalReportBuilder append(Object o);

    /**
     * Register an "extra issue" (not reported on a diff).
     * Note that extra issues are not always included in the final rendered report.
     *
     * @param issue the extra issue to append
     * @return a reference to this object
     */
    GlobalReportBuilder add(PostJobIssue issue);

    /**
     * Get string that presents all issues in format
     *
     * @return String in format
     */
    String build();

    String summarize();

}
