package io.nthienan.phdiff.report;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;

import java.util.Arrays;

import static io.nthienan.phdiff.report.RemarkupUtils.bold;

/**
 * Created on 12-Jul-17.
 *
 * @author nthienan
 */
@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class RemarkupGlobalReportBuilder implements GlobalReportBuilder {

    private RemarkupUtils remarkupUtils;
    private StringBuilder sb = new StringBuilder();
    private int[] numberOfIssuesBySeverity = new int[Severity.values().length];

    public RemarkupGlobalReportBuilder(RemarkupUtils remarkupUtils) {
        this.remarkupUtils = remarkupUtils;
    }

    @Override
    public GlobalReportBuilder append(Object o) {
        this.sb.append(o);
        return this;
    }

    @Override
    public GlobalReportBuilder add(PostJobIssue issue) {
        numberOfIssuesBySeverity[(issue.severity().ordinal())] += 1;
        sb.append(remarkupUtils.issue(issue));
        return this;
    }

    @Override
    public String build() {
        return new StringBuilder(summarize()).append(sb).toString();
    }

    @Override
    public String summarize() {
        StringBuilder sum = new StringBuilder();
        if (hasIssues()) {
            int totalIssues = totalIssues();
            sum.append(bold("SonarQube"))
                .append(" reported ")
                .append(bold(String.valueOf(totalIssues)))
                .append(" issue")
                .append(totalIssues > 1 ? "s" : "")
                .append("\n");
            // Blocker
            int blockerIssues = numberOfIssues(Severity.BLOCKER);
            if (blockerIssues > 0) {
                sum.append("  - ")
                    .append(String.valueOf(blockerIssues))
                    .append(" Blocker issue")
                    .append(blockerIssues > 1 ? "s" : "")
                    .append(" (").append(remarkupUtils.icon(Severity.BLOCKER)).append(")\n");
            }
            // Critical
            int criticalIssues = numberOfIssues(Severity.CRITICAL);
            if (criticalIssues > 0) {
                sum.append("  - ")
                    .append(String.valueOf(criticalIssues))
                    .append(" Critical issue")
                    .append(criticalIssues > 1 ? "s" : "")
                    .append(" (").append(remarkupUtils.icon(Severity.CRITICAL)).append(")\n");
            }
            // Major
            int majorIssues = numberOfIssues(Severity.MAJOR);
            if (majorIssues > 0) {
                sum.append("  - ")
                    .append(String.valueOf(majorIssues))
                    .append(" Major issue")
                    .append(majorIssues > 1 ? "s" : "")
                    .append(" (").append(remarkupUtils.icon(Severity.MAJOR)).append(")\n");
            }
            // Minor
            int minorIssues = numberOfIssues(Severity.MINOR);
            if (minorIssues > 0) {
                sum.append("  - ")
                    .append(String.valueOf(minorIssues))
                    .append(" Minor issue")
                    .append(minorIssues > 1 ? "s" : "")
                    .append(" (").append(remarkupUtils.icon(Severity.MINOR)).append(")\n");
            }
            // Info
            int infoIssues = numberOfIssues(Severity.INFO);
            if (infoIssues > 0) {
                sum.append("  - ")
                    .append(String.valueOf(infoIssues))
                    .append(" Info issue")
                    .append(infoIssues > 1 ? "s" : "")
                    .append(" (").append(remarkupUtils.icon(Severity.INFO)).append(")\n");
            }
            sum.append("See inline comments for more detail.");
        } else {
            sum.append(bold("SonarQube"))
                .append(" has found no issue.")
                .append(" You are great developer.");
        }
        return sum.toString();
    }

    private boolean hasIssues() {
        return totalIssues() > 0;
    }

    private int numberOfIssues(Severity s) {
        return numberOfIssuesBySeverity[s.ordinal()];
    }

    private int totalIssues() {
        final int[] total = {0};
        Arrays.stream(Severity.values())
            .forEach(severity -> total[0] += numberOfIssues(severity));
        return total[0];
    }

    @Override
    public String toString() {
        return build();
    }
}
