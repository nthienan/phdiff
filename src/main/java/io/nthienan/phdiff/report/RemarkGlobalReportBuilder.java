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
public class RemarkGlobalReportBuilder implements GlobalReportBuilder {

    private RemarkupUtils remarkupUtils;
    private StringBuilder sb = new StringBuilder();
    private int[] numberOfIssuesBySeverity = new int[Severity.values().length];

    public RemarkGlobalReportBuilder(RemarkupUtils remarkupUtils) {
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
    public String buildReport() {
        return summarize().append(sb).toString();
    }

    private StringBuilder summarize() {
        StringBuilder sum = new StringBuilder();
        if (hasIssues()) {
            int totalIssues = totalIssues();
            sum.append(bold("SonarQube")).append(" ")
                .append("analysis reported ")
                .append(bold(String.valueOf(totalIssues)))
                .append(totalIssues > 1 ? "s" : "")
                .append("\n");
            // Blocker
            sum.append("  - ")
                .append(String.valueOf(numberOfIssues(Severity.BLOCKER)))
                .append(" Blocker issue")
                .append(numberOfIssues(Severity.BLOCKER) > 1 ? "s" : "")
                .append("(").append(remarkupUtils.icon(Severity.BLOCKER)).append(")\n");
            // Critical
            sum.append("  - ")
                .append(String.valueOf(numberOfIssues(Severity.CRITICAL)))
                .append(" Critical issue")
                .append(numberOfIssues(Severity.CRITICAL) > 1 ? "s" : "")
                .append("(").append(remarkupUtils.icon(Severity.CRITICAL)).append(")\n");
            // Major
            sum.append("  - ")
                .append(String.valueOf(numberOfIssues(Severity.MAJOR)))
                .append(" Major issue")
                .append(numberOfIssues(Severity.MAJOR) > 1 ? "s" : "")
                .append("(").append(remarkupUtils.icon(Severity.MAJOR)).append(")\n");
            // Minor
            sum.append("  - ")
                .append(String.valueOf(numberOfIssues(Severity.MINOR)))
                .append(" Minor issue")
                .append(numberOfIssues(Severity.MINOR) > 1 ? "s" : "")
                .append("(").append(remarkupUtils.icon(Severity.MINOR)).append(")\n");
            // Info
            sum.append("  - ")
                .append(String.valueOf(numberOfIssues(Severity.INFO)))
                .append(" Info issue")
                .append(numberOfIssues(Severity.INFO) > 1 ? "s" : "")
                .append("(").append(remarkupUtils.icon(Severity.INFO)).append(")\n");
        } else {
            sum.append("You are great developer.\n")
                .append(bold("SonarQube"))
                .append(" analysis reported no issues.");
        }
        return sum;
    }

    public boolean hasIssues() {
        return totalIssues() > 0;
    }

    private int numberOfIssues(Severity s) {
        return numberOfIssuesBySeverity[s.ordinal()];
    }

    public int totalIssues() {
        final int[] total = {0};
        Arrays.stream(Severity.values())
            .forEach(severity -> total[0] += numberOfIssues(severity));
        return total[0];
    }

    @Override
    public String toString() {
        return buildReport();
    }
}
