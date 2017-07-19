package io.nthienan.phdiff

import io.nthienan.phdiff.conduit.ConduitClient
import io.nthienan.phdiff.conduit.DifferentialClient
import io.nthienan.phdiff.differential.Diff
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created on 19-Jul-17.
 * @author nthienan
 */
@SpringBootApplication
@RestController
open class Application {

    private val conduitClient = ConduitClient("https://test-hn6cabpfte2a.phacility.com", "api-nyma35ds7tjetuz6rtcvuqvi3yro");
    private val differentialClient = DifferentialClient(conduitClient)

    @PostMapping
    fun test(): Diff {
        differentialClient.postComment("1", "**SonarQube** analysis reported **5** issues" +
        "- 1 Blocker issue ({icon bug color=red})\\n- 1 Critical issue ({icon arrow-circle-up color=red})\\n- 1 Major issue ({icon chevron-circle-up color=red})\\n- 1 Minor issue ({icon info-circle color=green})\\n- 1 Info issue ({icon chevron-circle-down color=green})\\n**Details as below**:\\n{icon bug color=red} //Line 15// - `src/main/java/com/tma/dc4b/ci/MyBadImpl.java`: A \"NullPointerException\" could be thrown; \"str\" is nullable here. [[ http://192.168.88.57:9000/coding_rules#rule_key=squid%3AS2259 | View rule ]]\\n{icon arrow-circle-up color=red} //Line 15// - `src/main/java/com/tma/dc4b/ci/MyBadImpl.java`: A \"NullPointerException\" could be thrown; \"str\" is nullable here. [[ http://192.168.88.57:9000/coding_rules#rule_key=squid%3AS2259 | View rule ]]\\n{icon chevron-circle-up color=red} //Line 15// - `src/main/java/com/tma/dc4b/ci/MyBadImpl.java`: A \"NullPointerException\" could be thrown; \"str\" is nullable here. [[ http://192.168.88.57:9000/coding_rules#rule_key=squid%3AS2259 | View rule ]]\\n{icon info-circle color=green} //Line 15// - `src/main/java/com/tma/dc4b/ci/MyBadImpl.java`: A \"NullPointerException\" could be thrown; \"str\" is nullable here. [[ http://192.168.88.57:9000/coding_rules#rule_key=squid%3AS2259 | View rule ]]\\n{icon chevron-circle-down color=green} //Line 15// - `src/main/java/com/tma/dc4b/ci/MyBadImpl.java`: A \"NullPointerException\" could be thrown; \"str\" is nullable here. [[ http://192.168.88.57:9000/coding_rules#rule_key=squid%3AS2259 | View rule ]]")
        return differentialClient.fetchDiff("1")
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
