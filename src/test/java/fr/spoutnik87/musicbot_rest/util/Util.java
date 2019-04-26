package fr.spoutnik87.musicbot_rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class Util {
  public static void basicTest(
          MockMvc mockMvc,
          HttpMethod httpMethod,
          String endpoint,
          HashMap<String, String> params,
          HttpStatus expectedStatus)
      throws Exception {
    basicTestWithBody(mockMvc, httpMethod, endpoint, params, null, expectedStatus, null);
  }

  public static void basicTest(
          MockMvc mockMvc,
          HttpMethod httpMethod,
          String endpoint,
          HashMap<String, String> params,
          HttpStatus expectedStatus,
          String expectedValue)
          throws Exception {
    basicTestWithBody(mockMvc, httpMethod, endpoint, params, null, expectedStatus, expectedValue);
  }

  public static void basicTestWithBody(
          MockMvc mockMvc,
          HttpMethod httpMethod,
          String endpoint,
          HashMap<String, String> params,
          Map<String, Object> body,
          HttpStatus expectedStatus)
          throws Exception {
    basicTestWithBody(mockMvc, httpMethod, endpoint, params, body, expectedStatus, null);
  }

  public static void basicTestWithBody(
          MockMvc mockMvc,
          HttpMethod httpMethod,
          String endpoint,
          HashMap<String, String> params,
          Map<String, Object> body,
          HttpStatus expectedStatus,
          String expectedValue)
          throws Exception {
    MvcResult result =
            mockMvc
                    .perform(buildRequest(httpMethod, endpoint, params, body))
                    .andExpect(status().is(expectedStatus.value()))
                    .andReturn();
    if (expectedValue != null) {
      JSONAssert.assertEquals(expectedValue, result.getResponse().getContentAsString(), true);
    }
  }

  public static void basicPrint(
      MockMvc mockMvc, HttpMethod httpMethod, String endpoint, HashMap<String, String> params)
      throws Exception {
    basicPrintWithBody(mockMvc, httpMethod, endpoint, params, null);
  }

  public static void basicPrintWithBody(
      MockMvc mockMvc,
      HttpMethod httpMethod,
      String endpoint,
      HashMap<String, String> params,
      Map<String, Object> body)
      throws Exception {
    MvcResult result =
        mockMvc.perform(buildRequest(httpMethod, endpoint, params, body)).andReturn();
    System.out.println(
        "######################################################################################################");
    System.out.println(
        "######################################################################################################");
    System.out.println(result.getResponse().getContentAsString());
    System.out.println(
        "######################################################################################################");
    System.out.println(
        "######################################################################################################");
  }

  private static MockHttpServletRequestBuilder buildRequest(
      HttpMethod httpMethod,
      String endpoint,
      HashMap<String, String> params,
      Map<String, Object> body)
      throws Exception {
    MockHttpServletRequestBuilder requestBuilder;

    switch (httpMethod) {
      case PUT:
        requestBuilder = put(endpoint);
        break;
      case POST:
        requestBuilder = post(endpoint);
        break;
      case DELETE:
        requestBuilder = delete(endpoint);
        break;
      default:
        requestBuilder = get(endpoint);
    }

    requestBuilder.contentType(MediaType.APPLICATION_JSON);
    if (body != null) {
      requestBuilder.content(mapToJSON(body));
    }

    for (Map.Entry<String, String> entry : params.entrySet()) {
      requestBuilder.param(entry.getKey(), entry.getValue());
    }
    return requestBuilder;
  }

  public static String mapToJSON(Map<String, Object> params) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(params);
  }
}
