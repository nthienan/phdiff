package io.nthienan.phdiff;

import org.sonar.api.CoreProperties;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class Configuration {

    public static final int MAX_GLOBAL_ISSUES = 10;
    public static final String HTTP_PROXY_HOSTNAME = "http.proxyHost";
    public static final String HTTPS_PROXY_HOSTNAME = "https.proxyHost";
    public static final String PROXY_SOCKS_HOSTNAME = "socksProxyHost";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";
    public static final String HTTP_PROXY_USER = "http.proxyUser";
    public static final String HTTP_PROXY_PASS = "http.proxyPassword";
    private static final Logger LOG = Loggers.get(Configuration.class);
    private final Settings settings;
    private final System2 system2;
    private final Pattern gitSshPattern;
    private final Pattern gitHttpPattern;

    public Configuration(Settings settings, System2 system2) {
        this.settings = settings;
        this.system2 = system2;
        this.gitSshPattern = Pattern.compile(".*\\.com:(.*/.*)\\.git");
        this.gitHttpPattern = Pattern.compile("https?://*\\.*/(.*/.*)\\.git");
    }

    public String diffId() {
        return settings.getString(PhabricatorDifferentialPlugin.DIFF_ID);
    }

    public String conduitToken() {
        return settings.getString(PhabricatorDifferentialPlugin.CONDUIT_TOKEN);
    }

    public boolean isEnabled() {
        return settings.hasKey(PhabricatorDifferentialPlugin.DIFF_ID);
    }

    public String phabricatorUrl() {
        return settings.getString(PhabricatorDifferentialPlugin.PHABRICATOR_URL);
    }

    public String projectKey() {
        return settings.getString(CoreProperties.PROJECT_KEY_PROPERTY);
    }

    /**
     * Checks if a proxy was passed with command line parameters or configured in the system.
     * If only an HTTP proxy was configured then it's properties are copied to the HTTPS proxy (like SonarQube configuration)
     *
     * @return True if a proxy was configured to be used in the plugin.
     */
    public boolean isProxyConnectionEnabled() {
        return system2.property(HTTP_PROXY_HOSTNAME) != null
            || system2.property(HTTPS_PROXY_HOSTNAME) != null
            || system2.property(PROXY_SOCKS_HOSTNAME) != null;
    }

    public Proxy getHttpProxy() {
        try {
            if (system2.property(HTTP_PROXY_HOSTNAME) != null && system2.property(HTTPS_PROXY_HOSTNAME) == null) {
                System.setProperty(HTTPS_PROXY_HOSTNAME, system2.property(HTTP_PROXY_HOSTNAME));
                System.setProperty(HTTPS_PROXY_PORT, system2.property(HTTP_PROXY_PORT));
            }
            String proxyUser = system2.property(HTTP_PROXY_USER);
            String proxyPass = system2.property(HTTP_PROXY_PASS);
            if (proxyUser != null && proxyPass != null) {
                Authenticator.setDefault(
                    new Authenticator() {
                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                proxyUser, proxyPass.toCharArray());
                        }
                    });
            }
            Proxy selectedProxy = ProxySelector.getDefault().select(new URI(phabricatorUrl())).get(0);
            if (selectedProxy.type() == Proxy.Type.DIRECT) {
                LOG.debug("There was no suitable proxy found to connect to Phabricator - direct connection is used ");
            }
            LOG.info("A proxy has been configured - {}", selectedProxy.toString());
            return selectedProxy;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to perform Phabricator WS operation - phabricatorUrl in wrong format: " + phabricatorUrl(), e);
        }
    }

}
