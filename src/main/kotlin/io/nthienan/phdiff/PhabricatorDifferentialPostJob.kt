package io.nthienan.phdiff

import io.nthienan.phdiff.conduit.ConduitClient
import io.nthienan.phdiff.conduit.ConduitException
import io.nthienan.phdiff.conduit.DifferentialClient
import io.nthienan.phdiff.issue.IssueComparator
import io.nthienan.phdiff.report.GlobalReportBuilder
import io.nthienan.phdiff.report.InlineReportBuilder
import org.sonar.api.batch.postjob.PostJob
import org.sonar.api.batch.postjob.PostJobContext
import org.sonar.api.batch.postjob.PostJobDescriptor
import org.sonar.api.utils.log.Loggers
import java.util.stream.StreamSupport

/**
 * Compute comments to be added on the differential.
 *
 * @author nthienan
 */
class PhabricatorDifferentialPostJob(
  val globalReportBuilder: GlobalReportBuilder,
  val inlineReportBuilder: InlineReportBuilder,
  val configuration: Configuration
) : PostJob {
  val differentialClient: DifferentialClient
  val projectKey: String

  init {
    val url = configuration.phabricatorUrl()
    val token = configuration.conduitToken()
    this.differentialClient = DifferentialClient(ConduitClient(url, token))
    this.projectKey = configuration.projectKey()
  }

  companion object {
    private val issueComparator = IssueComparator()
    private val log = Loggers.get(PhabricatorDifferentialPostJob::class.java)
  }

  override fun describe(descriptor: PostJobDescriptor?) {
    descriptor
      ?.name("Publish issues")
      ?.requireProperty(PhabricatorDifferentialPlugin.DIFF_ID)
  }

  override fun execute(context: PostJobContext?) {
    val diffID = configuration.diffId()
    try {
      val diff = differentialClient.fetchDiff(diffID)
      if (context?.analysisMode()?.isIssues ?: false) {
        StreamSupport.stream(context?.issues()?.spliterator(), false)
          .filter { it.isNew }
          .filter { it.inputComponent()?.isFile ?: false }
          .sorted(issueComparator)
          .forEach { i ->
            run {
              globalReportBuilder.add(i)
              val ic = inlineReportBuilder.issue(i).build()
              val filePath = i.componentKey().replace(projectKey, "").substring(1)
              try {
                differentialClient.postInlineComment(diffID, filePath, i.line()!!, ic)
                log.debug("Comment $ic has been published")
              } catch (e: ConduitException) {
                if (e.message.equals("Requested file doesn't exist in this revision.")) {
                  val message = "Unmodified file $filePath  on line ${i.line()}\n\n $ic"
                  differentialClient.postComment(diff.revisionId, message, false)
                } else {
                  log.error(e.message, e)
                }
              }
          }
        }
      }
      differentialClient.postComment(diff.revisionId, globalReportBuilder.summarize())
      log.info("Analysis result has been published to your differential revision")
    } catch (e: ConduitException) {
      log.error(e.message, e)
    }
  }
}
