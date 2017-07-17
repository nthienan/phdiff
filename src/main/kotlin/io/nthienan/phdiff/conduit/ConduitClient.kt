package io.nthienan.phdiff.conduit

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.json.JSONObject
import org.sonar.api.internal.google.gson.JsonObject
import java.net.URL
import java.util.*

/**
 * Created on 17-Jul-17.
 * @author nthienan
 */
class ConduitClient(var url: String, var token: String) {

    fun perform(action: String, params: JSONObject): JsonObject? {
        val httpClient = HttpClientBuilder.create().build()
        val postRequest = makeRequest(action, params)
        return null
    }

    private fun makeRequest(action: String, params: JSONObject): HttpPost {
        val postRequest = HttpPost(URL(URL(URL(url), "/api/"), action).toURI())

        val conduitMetadata = JSONObject()
        conduitMetadata.append("token", token)
        params.append("__conduit__", conduitMetadata)

        val formData = ArrayList<NameValuePair>()
        formData.add(BasicNameValuePair("params", params.toString()))
        val entity = UrlEncodedFormEntity(formData, "UTF-8")
        postRequest.setEntity(entity)

        return postRequest
    }
}
