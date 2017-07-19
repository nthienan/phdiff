package io.nthienan.phdiff;

import io.nthienan.phdiff.issue.IssueComparator;
import io.nthienan.phdiff.report.GlobalReportBuilder;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * Compute comments to be added on the differential.
 *
 * @author nthienan
 */
public class PhabricatorDifferentialBot implements PostJob {

    private static final Logger LOG = Loggers.get(PhabricatorDifferentialBot.class);

    private static final Comparator<PostJobIssue> ISSUE_COMPARATOR = new IssueComparator();

    private final GlobalReportBuilder reportBuilder;
    private Settings settings;

    public PhabricatorDifferentialBot(GlobalReportBuilder reportBuilder, Settings settings) {
        this.reportBuilder = reportBuilder;
        this.settings = settings;
    }

    @Override
    public void describe(PostJobDescriptor descriptor) {
        descriptor
            .name("Phabricator Differential Issue Publisher")
            .requireProperty(PhabricatorDifferentialPlugin.DIFF_ID);
    }

    @Override
    public void execute(PostJobContext context) {
        // issues are not accessible when the mode "issues" is not enabled
        if (context.analysisMode().isIssues()) {
            StreamSupport.stream(context.issues().spliterator(), false)
                .filter(PostJobIssue::isNew)
                .sorted(ISSUE_COMPARATOR)
                .forEach(reportBuilder::add);
        }
        LOG.error(reportBuilder.buildReport());
    }
}
