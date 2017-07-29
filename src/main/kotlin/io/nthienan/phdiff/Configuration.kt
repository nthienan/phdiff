package io.nthienan.phdiff

import org.sonar.api.CoreProperties
import org.sonar.api.batch.BatchSide
import org.sonar.api.batch.InstantiationStrategy
import org.sonar.api.config.Settings
import org.sonar.api.utils.System2
import org.sonar.api.utils.log.Loggers
import java.net.*

/**
 * Created on 29-Jul-17.
 * @author nthienan
 */
@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
class Configuration(val settings: Settings, val system2: System2) {
  companion object {
    const val HTTP_PROXY_HOST = "http.proxyHost"
    const val HTTPS_PROXY_HOST = "https.proxyHost"
    const val PROXY_SOCKS_HOST = "socksProxyHost"
    const val HTTP_PROXY_PORT = "http.proxyPort"
    const val HTTPS_PROXY_PORT = "https.proxyPort"
    const val HTTP_PROXY_USER = "http.proxyUser"
    const val HTTP_PROXY_PASS = "http.proxyPassword"
    val LOG = Loggers.get(Configuration::class.java)
  }

  fun diffId(): String = settings.getString(PhabricatorDifferentialPlugin.DIFF_ID) ?: ""

  fun conduitToken(): String = settings.getString(PhabricatorDifferentialPlugin.CONDUIT_TOKEN) ?: ""

  fun phabricatorUrl(): String = settings.getString(PhabricatorDifferentialPlugin.PHABRICATOR_URL) ?: ""

  fun projectKey(): String = settings.getString(CoreProperties.PROJECT_KEY_PROPERTY) ?: ""

  fun isProxyEnabled(): Boolean =
    system2.property(HTTP_PROXY_HOST) != null
      || system2.property(HTTPS_PROXY_HOST) != null
      || system2.property(PROXY_SOCKS_HOST) != null

  fun getHttpProxy(): Proxy {
    try {
      if (system2.property(HTTP_PROXY_HOST) != null && system2.property(HTTPS_PROXY_HOST) == null) {
        System.setProperty(HTTPS_PROXY_HOST, system2.property(HTTP_PROXY_HOST))
        System.setProperty(HTTPS_PROXY_PORT, system2.property(HTTP_PROXY_PORT))
      }
      val proxyUser = system2.property(HTTP_PROXY_USER)
      val proxyPass = system2.property(HTTP_PROXY_PASS)
      if (proxyUser != null && proxyPass != null) {
        Authenticator.setDefault(
          object : Authenticator() {
            public override fun getPasswordAuthentication(): PasswordAuthentication {
              return PasswordAuthentication(
                proxyUser, proxyPass.toCharArray())
            }
          })
      }
      val selectedProxy = ProxySelector.getDefault().select(URI(phabricatorUrl()))[0]
      if (selectedProxy.type() == Proxy.Type.DIRECT) {
        LOG.debug("There was no suitable proxy found to connect to Phabricator - direct connection is used ")
      }
      LOG.info("A proxy has been configured - {}", selectedProxy.toString())
      return selectedProxy
    } catch (e: URISyntaxException) {
      throw IllegalArgumentException("Unable to perform Phabricator WS operation - phabricatorUrl in wrong format: " + phabricatorUrl(), e)
    }

  }
}
