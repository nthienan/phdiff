package io.nthienan.phdiff.conduit

import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.json.JSONObject
import java.net.URL
import java.util.ArrayList

/**
 * Created on 17-Jul-17.
 * @author nthienan
 */
class ConduitClient(var url: String, var token: String) {

    fun perform(action: String, params: JSONObject): JSONObject {
        val httpClient = HttpClientBuilder.create().build()
        val postRequest = makeRequest(action, params)
        val response = httpClient.execute(postRequest)
        val responseBody = IOUtils.toString(response.entity.content, Charsets.UTF_8)
        if (response.statusLine.statusCode != HttpStatus.SC_OK) {
            throw ConduitException(responseBody, response.statusLine.statusCode)
        }
        val result = JSONObject(responseBody)
        val errorInfo = result.get("error_info").toString()
        if (!(result.get("error_code").toString().equals("null")
            && errorInfo.equals("null"))) {
            throw ConduitException(errorInfo, response.statusLine.statusCode)
        }
        return result
    }

    private fun makeRequest(action: String, params: JSONObject): HttpPost {
        val postRequest = HttpPost(URL(URL(URL(url), "/api/"), action).toURI())

        val conduitMetadata = JSONObject()
        conduitMetadata.put("token", token)
        params.put("__conduit__", conduitMetadata)

        val formData = ArrayList<NameValuePair>()
        formData.add(BasicNameValuePair("params", params.toString()))
        val entity = UrlEncodedFormEntity(formData, "UTF-8")
        postRequest.setEntity(entity)

        return postRequest
    }
}
