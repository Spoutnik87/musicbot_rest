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
                expectedStatus: HttpStatus) {
            basicTestWithBody(mockMvc, httpMethod, endpoint, params, null, expectedStatus, null)
        }

        fun basicTest(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                expectedStatus: HttpStatus,
                expectedValue: String) {
            basicTestWithBody(mockMvc, httpMethod, endpoint, params, null, expectedStatus, expectedValue)
        }

        fun basicTestWithBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>,
                expectedStatus: HttpStatus) {
            basicTestWithBody(mockMvc, httpMethod, endpoint, params, body, expectedStatus, null)
        }

        fun basicTestWithBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                expectedStatus: HttpStatus,
                expectedValue: String?) {
            basicTestWithTokenAndBody(mockMvc, httpMethod, endpoint, params, body, null, expectedStatus, expectedValue)
        }

        fun basicTestWithTokenAndBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                token: String?,
                expectedStatus: HttpStatus) {
            basicTestWithTokenAndBody(mockMvc, httpMethod, endpoint, params, body, null, expectedStatus, null)
        }

        fun basicTestWithTokenAndBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                token: String?,
                expectedStatus: HttpStatus,
                expectedValue: String?) {
            val result = mockMvc
                    .perform(buildRequest(httpMethod, endpoint, params, body, token))
                    .andExpect(status().`is`(expectedStatus.value()))
                    .andReturn()
            if (expectedValue != null) {
                JSONAssert.assertEquals(expectedValue, result.response.contentAsString, true)
            }
        }

        fun basicPrint(
                mockMvc: MockMvc, httpMethod: HttpMethod, endpoint: String, params: HashMap<String, String>) {
            basicPrintWithBody(mockMvc, httpMethod, endpoint, params, null)
        }

        fun basicPrintWithBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?) {
            val result = mockMvc.perform(buildRequest(httpMethod, endpoint, params, body, null)).andReturn()
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

        fun basicPrintWithTokenAndBody(
                mockMvc: MockMvc,
                httpMethod: HttpMethod,
                endpoint: String,
                params: HashMap<String, String>,
                body: Map<String, Any>?,
                token: String?) {
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
            val requestBuilder: MockHttpServletRequestBuilder

            when (httpMethod) {
                HttpMethod.PUT -> requestBuilder = put(endpoint)
                HttpMethod.POST -> requestBuilder = post(endpoint)
                HttpMethod.DELETE -> requestBuilder = delete(endpoint)
                else -> requestBuilder = get(endpoint)
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