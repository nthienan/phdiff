package io.nthienan.phdiff;

import io.nthienan.phdiff.conduit.ConduitClient;
import io.nthienan.phdiff.conduit.ConduitException;
import io.nthienan.phdiff.conduit.DifferentialClient;
import io.nthienan.phdiff.differential.Diff;
import io.nthienan.phdiff.issue.IssueComparator;
import io.nthienan.phdiff.report.GlobalReportBuilder;
import io.nthienan.phdiff.report.InlineReportBuilder;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Comparator;
import java.util.stream.StreamSupport;

/**
 * Compute comments to be added on the differential.
 *
 * @author nthienan
 */
public class PhabricatorDifferentialPostJob implements PostJob {

    private static final Logger LOG = Loggers.get(PhabricatorDifferentialPostJob.class);

    private static final Comparator<PostJobIssue> ISSUE_COMPARATOR = new IssueComparator();

    private final GlobalReportBuilder reportBuilder;
    private final InlineReportBuilder inlineReportBuilder;
    private final String projectKey;
    private final Configuration configuration;
    private DifferentialClient differentialClient;

    public PhabricatorDifferentialPostJob(GlobalReportBuilder reportBuilder, InlineReportBuilder inlineReportBuilder, Configuration configuration) {
        this.reportBuilder = reportBuilder;
        this.inlineReportBuilder = inlineReportBuilder;
        this.configuration = configuration;
        String url = this.configuration.phabricatorUrl();
        String token = this.configuration.conduitToken();
        this.differentialClient = new DifferentialClient(new ConduitClient(url, token));
        this.projectKey = configuration.projectKey();
    }

    @Override
    public void describe(PostJobDescriptor descriptor) {
        descriptor
            .name("Phabricator Differential Issue Publisher")
            .requireProperty(PhabricatorDifferentialPlugin.DIFF_ID);
    }

    @Override
    public void execute(PostJobContext context) {
        try {
            String diffID = configuration.diffId();
            Diff diff = differentialClient.fetchDiff(diffID);
            // issues are not accessible when the mode "issues" is not enabled
            if (context.analysisMode().isIssues()) {
                StreamSupport.stream(context.issues().spliterator(), false)
                    .filter(PostJobIssue::isNew)
                    .filter(i -> i.inputComponent().isFile())
                    .sorted(ISSUE_COMPARATOR)
                    .forEach(i -> {
                        reportBuilder.add(i);
                        String ic = inlineReportBuilder.issue(i).build();
                        LOG.error(ic);
                        String filePath = i.componentKey().replace(projectKey, "").substring(1);
                        LOG.error(filePath);
                        try {
                            differentialClient.postInlineComment(diffID, filePath, i.line(), ic);
                        } catch (ConduitException e) {
                            LOG.error(e.getMessage());
                        }
                    });
            }
            LOG.error(reportBuilder.buildReport());
            differentialClient.postComment(diff.getRevisionId(), reportBuilder.buildReport());
        } catch (ConduitException e) {
            LOG.error(e.getMessage());
        }
    }
}
