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
        differentialClient.postInlineComment("2", "src/main/java/io/nthienan/ci/MyBadImpl.java",
            21, "{icon chevron-circle-up color=red} \"str\" is already a string, there's no need to call \"toString()\" on it. [[http://localhost:9000//coding_rules#rule_key=S1858|View rule]]")
        return differentialClient.fetchDiff("1")
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
