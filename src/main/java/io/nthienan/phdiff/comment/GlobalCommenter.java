package io.nthienan.phdiff.comment;

import io.nthienan.phdiff.Configuration;
import io.nthienan.phdiff.differential.Result;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;

import java.util.Locale;

public class GlobalCommenter {
    private final CommentBuilder reportBuilder;
    private int[] newIssuesBySeverity = new int[Severity.values().length];
    private int extraIssueCount = 0;
    private int maxGlobalReportedIssues = Configuration.MAX_GLOBAL_ISSUES;

    public GlobalCommenter(CommentBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }

    private void increment(Severity severity) {
        this.newIssuesBySeverity[severity.ordinal()]++;
    }

    public String formatForMarkdown() {
        int newIssues = numberOfIssues(Severity.BLOCKER) + numberOfIssues(Severity.CRITICAL) + numberOfIssues(Severity.MAJOR) + numberOfIssues(Severity.MINOR) + numberOfIssues(Severity.INFO);
        if (newIssues == 0) {
            return "SonarQube analysis reported no issues.";
        }

        boolean hasInlineIssues = newIssues > extraIssueCount;
        boolean extraIssuesTruncated = extraIssueCount > maxGlobalReportedIssues;
        reportBuilder.append("SonarQube analysis reported ").append(newIssues).append(" issue").append(newIssues > 1 ? "s" : "").append("\n");
        if (hasInlineIssues || extraIssuesTruncated) {
            appendSummaryBySeverity(reportBuilder);
        }

        if (extraIssueCount > 0) {
            appendExtraIssues(reportBuilder, hasInlineIssues, extraIssuesTruncated);
        }

        return reportBuilder.toString();
    }

    private void appendExtraIssues(CommentBuilder builder, boolean hasInlineIssues, boolean extraIssuesTruncated) {
        if (extraIssuesTruncated) {
            builder.append("\n#### Top ").append(maxGlobalReportedIssues).append(" issues\n");
        }
        builder.appendExtraIssues();
    }

    public String getStatusDescription() {
        StringBuilder sb = new StringBuilder();
        appendNewIssuesInline(sb);
        return sb.toString();
    }

    public Result getStatus() {
        return (numberOfIssues(Severity.BLOCKER) > 0 || numberOfIssues(Severity.CRITICAL) > 0) ? Result.UNSTABLE : Result.SUCCESS;
    }

    private int numberOfIssues(Severity s) {
        return newIssuesBySeverity[s.ordinal()];
    }

    private void appendSummaryBySeverity(CommentBuilder builder) {
        appendNewIssues(builder, Severity.BLOCKER);
        appendNewIssues(builder, Severity.CRITICAL);
        appendNewIssues(builder, Severity.MAJOR);
        appendNewIssues(builder, Severity.MINOR);
        appendNewIssues(builder, Severity.INFO);
    }

    private void appendNewIssuesInline(StringBuilder sb) {
        sb.append("SonarQube reported ");
        int newIssues = numberOfIssues(Severity.BLOCKER) + numberOfIssues(Severity.CRITICAL) + numberOfIssues(Severity.MAJOR) + numberOfIssues(Severity.MINOR) + numberOfIssues(Severity.INFO);
        if (newIssues > 0) {
            sb.append(newIssues).append(" issue" + (newIssues > 1 ? "s" : "")).append(",");
            int newCriticalOrBlockerIssues = numberOfIssues(Severity.BLOCKER) + numberOfIssues(Severity.CRITICAL);
            if (newCriticalOrBlockerIssues > 0) {
                appendNewIssuesInline(sb, Severity.CRITICAL);
                appendNewIssuesInline(sb, Severity.BLOCKER);
            } else {
                sb.append(" no criticals or blockers");
            }
        } else {
            sb.append("no issues");
        }
    }

    private void appendNewIssuesInline(StringBuilder sb, Severity severity) {
        int issueCount = numberOfIssues(severity);
        if (issueCount > 0) {
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.append(" with ");
            } else {
                sb.append(" and ");
            }
            sb.append(issueCount).append(" ").append(severity.name().toLowerCase(Locale.ENGLISH));
        }
    }

    private void appendNewIssues(CommentBuilder builder, Severity severity) {
        int issueCount = numberOfIssues(severity);
        if (issueCount > 0) {
            builder
                .append("* ").append(severity)
                .append(" ").append(issueCount)
                .append(" ").append(severity.name().toLowerCase(Locale.ENGLISH))
                .append("\n");
        }
    }

    public void process(PostJobIssue issue) {
        increment(issue.severity());
        if (extraIssueCount < maxGlobalReportedIssues) {
            reportBuilder.registerExtraIssue(issue);
        }
        extraIssueCount++;
    }

    public boolean hasNewIssue() {
        return numberOfIssues(Severity.BLOCKER) + numberOfIssues(Severity.CRITICAL) + numberOfIssues(Severity.MAJOR) + numberOfIssues(Severity.MINOR) + numberOfIssues(Severity.INFO) > 0;
    }
}
