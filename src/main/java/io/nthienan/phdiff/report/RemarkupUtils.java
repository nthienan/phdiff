package io.nthienan.phdiff.report;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Settings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Support Remarkup format <br\>
 * References: {@code https://secure.phabricator.com/book/phabricator/article/remarkup}
 *
 * @author nthienan
 */
@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class RemarkupUtils {

    private final String ruleUrlPrefix;
    private final String projectKey;

    public RemarkupUtils(Settings settings) {
        // If server base URL was not configured in SQ server then is is better to take URL configured on batch side
        String baseUrl = settings.hasKey(CoreProperties.SERVER_BASE_URL) ? settings.getString(CoreProperties.SERVER_BASE_URL) : settings.getString("sonar.host.url");
        this.projectKey = settings.getString(CoreProperties.PROJECT_KEY_PROPERTY);
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        this.ruleUrlPrefix = baseUrl;
    }

    public static String bold(String str) {
        return String.format("**%s**", str);
    }

    public static String italics(String str) {
        return String.format("//%s//", str);
    }

    public static String code(String str) {
        return String.format("`%s`", str);
    }

    public static String icon(String icon, String color) {
        String colorStr = "";
        if (StringUtils.isNotBlank(color)) {
            colorStr = "color=" + color;
        }
        return String.format("{icon %s %s}", icon, colorStr);
    }

    public static String link(String url, String title) {
        if (StringUtils.isNotBlank(title)) {
            return String.format("[[%s|%s]]", url, title);
        }
        return url;
    }

    public static String encodeForUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Encoding not supported", e);
        }
    }

    public String icon(Severity severity) {
        String result;
        switch (severity) {
            case BLOCKER:
                result = icon("bug", "red");
                break;
            case CRITICAL:
                result = icon("arrow-circle-up", "red");
                break;
            case MINOR:
                result = icon("info-circle", "green");
                break;
            case INFO:
                result = icon("chevron-circle-down", "green");
                break;
            default:
                // Major
                result = icon("chevron-circle-up", "red");
        }
        return result;
    }

    public String source(String componentKey, int line) {
        return String.format("%s - %s:",
            italics(String.valueOf(String.format("Line %s", line))),
            code(componentKey.replace(projectKey, "").substring(1)));
    }

    public String message(String message) {
        return message;
    }

    public String rule(String ruleKey) {
        return link(String.format("%scoding_rules#rule_key=%s",
            this.ruleUrlPrefix, encodeForUrl(ruleKey)), "View rule");
    }

    public String issue(PostJobIssue issue) {
        return String.format("%s %s %s %s",
            icon(issue.severity()),
            source(issue.componentKey(), issue.line()),
            message(issue.message()),
            rule(issue.ruleKey().rule())
        );
    }

}
