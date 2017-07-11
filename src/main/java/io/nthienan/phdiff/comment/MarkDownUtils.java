package io.nthienan.phdiff.comment;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Settings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Pattern;

@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class MarkDownUtils {

    private static final String IMAGES_ROOT_URL = "https://sonarsource.github.io/sonar-github/";
    private final String ruleUrlPrefix;

    public MarkDownUtils(Settings settings) {
        // If server base URL was not configured in SQ server then is is better to take URL configured on batch side
        String baseUrl = settings.hasKey(CoreProperties.SERVER_BASE_URL) ? settings.getString(CoreProperties.SERVER_BASE_URL) : settings.getString("sonar.host.url");
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        this.ruleUrlPrefix = baseUrl;
    }

    private static String getLocation(String url) {
        String filename = Pattern.compile(".*/", Pattern.DOTALL).matcher(url).replaceAll(StringUtils.EMPTY);
        if (filename.length() <= 0) {
            filename = "Project";
        }

        return filename;
    }

    static String encodeForUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Encoding not supported", e);
        }
    }

    static String getImageUrl(Severity severity) {
        return IMAGES_ROOT_URL + "severity-" + severity.name().toLowerCase(Locale.ENGLISH) + ".png";
    }

    static String formatImageLink(Severity severity) {
        return String.format("![%s](%s 'Severity: %s')", severity.name(), getImageUrl(severity), severity.name());
    }

    public String inlineIssue(Severity severity, String message, String ruleKey) {
        String ruleLink = getRuleLink(ruleKey);
        StringBuilder sb = new StringBuilder();
        sb.append(formatImageLink(severity))
            .append(" ")
            .append(message)
            .append(" ")
            .append(ruleLink);
        return sb.toString();
    }

    public String globalIssue(String message, String ruleKey, String componentKey) {
        StringBuilder sb = new StringBuilder();
        sb.append(componentKey);
        String ruleLink = getRuleLink(ruleKey);
        sb.append(": ").append(message).append(" ").append(ruleLink);
        return sb.toString();
    }

    String getRuleLink(String ruleKey) {
        return "[![rule](" + IMAGES_ROOT_URL + "rule.png)](" + ruleUrlPrefix + "coding_rules#rule_key=" + encodeForUrl(ruleKey) + ")";
    }
}
