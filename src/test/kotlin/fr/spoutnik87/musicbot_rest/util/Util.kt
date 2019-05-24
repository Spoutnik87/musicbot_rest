package fr.spoutnik87.musicbot_rest.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


abstract class Util {

    companion object {
        fun basicTest(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                expectedStatus: HttpStatus,
                expectedValue: String? = null) {
            basicTestWithBody(mockMvc, httpMethod, endpoint, params, null, expectedStatus, expectedValue)
        }

        fun basicTestWithBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                expectedStatus: HttpStatus,
                expectedValue: String? = null) {
            basicTestWithTokenAndBody(mockMvc, httpMethod, endpoint, params, body, null, expectedStatus, expectedValue)
        }

        fun basicTestWithTokenAndBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                token: String?,
                expectedStatus: HttpStatus,
                expectedValue: String? = null) {
            val result = mockMvc
                    .perform(buildRequest(httpMethod, endpoint, params, body, token))
                    .andExpect(status().`is`(expectedStatus.value()))
                    .andReturn()
            if (expectedValue != null) {
                JSONAssert.assertEquals(expectedValue, result.response.contentAsString, true)
            }
        }

        fun basicPrint(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>? = null,
                token: String? = null) {
            val result = mockMvc.perform(buildRequest(httpMethod, endpoint, params, body, token)).andReturn()
            println(
                    "######################################################################################################")
            println(
                    "######################################################################################################")
            println(result.response.status)
            println(result.response.contentAsString)
            println(
                    "######################################################################################################")
            println(
                    "######################################################################################################")
        }

        private fun buildRequest(
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                token: String?): MockHttpServletRequestBuilder {
            val requestBuilder: MockHttpServletRequestBuilder = when (httpMethod) {
                HttpMethod.PUT -> put(endpoint)
                HttpMethod.POST -> post(endpoint)
                HttpMethod.DELETE -> delete(endpoint)
                else -> get(endpoint)
            }

            requestBuilder.contentType(MediaType.APPLICATION_JSON)
            if (body != null) {
                requestBuilder.content(mapToJSON(body))
            }

            for ((key, value) in params) {
                requestBuilder.param(key, value)
            }

            if (token != null) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            return requestBuilder
        }

        fun mapToJSON(params: Map<String, Any>): String {
            val objectMapper = ObjectMapper()
            return objectMapper.writeValueAsString(params)
        }
    }
}