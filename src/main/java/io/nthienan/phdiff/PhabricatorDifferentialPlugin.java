package io.nthienan.phdiff;

import io.nthienan.phdiff.report.RemarkGlobalReportBuilder;
import io.nthienan.phdiff.report.RemarkupInlineReportBuilder;
import io.nthienan.phdiff.report.RemarkupUtils;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;

/**
 * Created on 06-Jul-17.
 *
 * @author nthienan
 */
@Properties({
    @Property(
        key = PhabricatorDifferentialPlugin.PHABRICATOR_URL,
        name = "Phabricator URL",
        defaultValue = "http://localhost",
        description = "URL to access Phabricator.",
        project = true
    ),
    @Property(
        key = PhabricatorDifferentialPlugin.CONDUIT_TOKEN,
        name = "Conduit token",
        description = "Conduit token",
        project = true,
        type = PropertyType.PASSWORD
    ),
    @Property(
        key = PhabricatorDifferentialPlugin.DIFF_ID,
        name = "DIFF_ID",
        description = "Diff ID",
        global = false
    )
})
public class PhabricatorDifferentialPlugin implements Plugin {

    public static final String PHABRICATOR_URL = "sonar.phdiff.phabricatorUrl";
    public static final String CONDUIT_TOKEN = "sonar.phdiff.conduitToken";
    public static final String DIFF_ID = "sonar.phdiff.diffId";

    @Override
    public void define(Context context) {
        context.addExtensions(
            PhabricatorDifferentialPostJob.class,
            Configuration.class,
            RemarkGlobalReportBuilder.class,
            RemarkupUtils.class,
            RemarkupInlineReportBuilder.class
        );
    }
}
