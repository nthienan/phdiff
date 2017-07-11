package io.nthienan.phdiff;

import io.nthienan.phdiff.comment.GlobalCommenter;
import io.nthienan.phdiff.comment.CommentBuilder;
import io.nthienan.phdiff.issue.IssueComparator;
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
public class NotificationPostJob implements PostJob {

    private static final Logger LOG = Loggers.get(NotificationPostJob.class);

    private static final Comparator<PostJobIssue> ISSUE_COMPARATOR = new IssueComparator();

    private final CommentBuilder reportBuilder;

    public NotificationPostJob(CommentBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }

    @Override
    public void describe(PostJobDescriptor descriptor) {
        descriptor
            .name("Phabricator Differential Issue Publisher")
            .requireProperty(PhabricatorDifferentialPlugin.PHID);
    }

    @Override
    public void execute(PostJobContext context) {
        GlobalCommenter commenter = new GlobalCommenter(this.reportBuilder);
        // issues are not accessible when the mode "issues" is not enabled
        if (context.analysisMode().isIssues()) {
            StreamSupport.stream(context.issues().spliterator(), false)
                .filter(PostJobIssue::isNew)
                .sorted(ISSUE_COMPARATOR)
                .forEach(commenter::process);
        }
        if(commenter.hasNewIssue()) {
            String markdown = commenter.formatForMarkdown();
            LOG.error(String.format("Markdown: %s", markdown));
        }
    }
}
