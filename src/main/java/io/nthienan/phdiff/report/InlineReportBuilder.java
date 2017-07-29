package io.nthienan.phdiff.report;

import org.sonar.api.batch.postjob.issue.PostJobIssue;

/**
 * Created on 12-Jul-17.
 *
 * @author nthienan
 */
public interface InlineReportBuilder {

  InlineReportBuilder issue(PostJobIssue issue);

  String build();
}
