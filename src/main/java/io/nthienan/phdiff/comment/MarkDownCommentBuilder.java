package io.nthienan.phdiff.comment;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class MarkDownCommentBuilder implements CommentBuilder {

    private final MarkDownUtils markDownUtils;
    private final StringBuilder sb = new StringBuilder();

    // note: ordered implementation for consistent user experience and testability
    private final Set<String> links = new TreeSet<>();

    private final List<PostJobIssue> extraIssues = new ArrayList<>();

    public MarkDownCommentBuilder(MarkDownUtils markDownUtils) {
        this.markDownUtils = markDownUtils;
    }

    private static String formatImageLinkDefinition(Severity severity) {
        return String.format("[%s]: %s 'Severity: %s'", severity.name(), MarkDownUtils.getImageUrl(severity), severity.name());
    }

    private static String formatImageLinkReference(Severity severity) {
        return String.format("![%s][%s]", severity.name(), severity.name());
    }

    @Override
    public CommentBuilder append(Object o) {
        sb.append(o);
        return this;
    }

    @Override
    public CommentBuilder append(Severity severity) {
        links.add(formatImageLinkDefinition(severity));
        sb.append(formatImageLinkReference(severity));
        return this;
    }

    @Override
    public CommentBuilder registerExtraIssue(PostJobIssue issue) {
        extraIssues.add(issue);
        return this;
    }

    @Override
    public CommentBuilder appendExtraIssues() {
        // need a blank line before lists to be displayed correctly
        sb.append("\n");
        for (PostJobIssue issue : extraIssues) {
            links.add(formatImageLinkDefinition(issue.severity()));
            String image = formatImageLinkReference(issue.severity());
            String text = markDownUtils.globalIssue(issue.message(), issue.ruleKey().toString(), issue.componentKey());
            sb.append("1. ").append(image).append(" ").append(text).append("\n");
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder copy = new StringBuilder(sb);
        for (String link : links) {
            copy.append("\n").append(link);
        }
        return copy.toString();
    }
}
