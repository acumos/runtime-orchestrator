package org.acumos.bporchestrator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractControllerTest {
	protected MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    protected MockMvc mockMvc;
    
    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
     void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }
    
    @Before
    public  void setup() throws Exception {
        if (this.mockMvc == null) {
            this.mockMvc = webAppContextSetup(webApplicationContext).build();
        }
   }

    @After
    public  void teardown() throws Exception {
        
    }

    protected  ResultActions doGet(String urlTemplate, Object... urlVariables) throws Exception {
        MockHttpServletRequestBuilder getRequest = get(urlTemplate, urlVariables);
        
        return mockMvc.perform(getRequest);
    }
    
    protected <T>  T doGet(String urlTemplate, Class<T> responseClass, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(status().isOk()), responseClass);
    }
    
    protected <T>  T doGetTyped(String urlTemplate, TypeReference<T> responseType, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(status().isOk()), responseType);
    }
    
    protected <T>  T doPost(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, params).andExpect(status().isOk()), responseClass);
    }
    
    protected <T>  T doPost(String urlTemplate, T content, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }
    
    
    protected <T>  T doDelete(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doDelete(urlTemplate, params).andExpect(status().isOk()), responseClass);
    }
     
    protected  ResultActions doPost(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate);
        
        populateParams(postRequest, params);
        return mockMvc.perform(postRequest);
    }
    
    protected <T>  ResultActions doPost(String urlTemplate, T content, String... params)  throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate);
       
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        populateParams(postRequest, params);
        return mockMvc.perform(postRequest);
    }
    
    protected  ResultActions doDelete(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder deleteRequest = delete(urlTemplate);
     
        populateParams(deleteRequest, params);
        return mockMvc.perform(deleteRequest);
    }
    
    protected <T>  T doPut(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doPut(urlTemplate, params).andExpect(status().isOk()), responseClass);
    }
    
    protected <T>  T doPut(String urlTemplate, T content, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doPut(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }
    
    protected  ResultActions doPut(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder putRequest = put(urlTemplate);
        
        populateParams(putRequest, params);
        return mockMvc.perform(putRequest);
    }
    
    protected <T>  ResultActions doPut(String urlTemplate, T content, String... params)  throws Exception {
        MockHttpServletRequestBuilder putRequest = put(urlTemplate);
       
        String json = json(content);
        putRequest.contentType(contentType).content(json);
        populateParams(putRequest, params);
        return mockMvc.perform(putRequest);
    }
    
    protected  void populateParams(MockHttpServletRequestBuilder request, String... params) {
        if (params != null && params.length > 0) {
            Assert.assertEquals(params.length % 2, 0);
            MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<String, String>();
            for (int i=0;i<params.length;i+=2) {
                paramsMap.add(params[i], params[i+1]);
            }
            request.params(paramsMap);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected  String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
    
    @SuppressWarnings("unchecked")
    protected <T>  T readResponse(ResultActions result, Class<T> responseClass) throws Exception {
        byte[] content = result.andReturn().getResponse().getContentAsByteArray();
        MockHttpInputMessage mockHttpInputMessage = new MockHttpInputMessage(content);
        return (T) this.mappingJackson2HttpMessageConverter.read(responseClass, mockHttpInputMessage);
    }
    
    protected <T>  T readResponse(ResultActions result, TypeReference<T> type) throws Exception {
        byte[] content = result.andReturn().getResponse().getContentAsByteArray();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readerFor(type).readValue(content);
    }
     
    
    protected static <T>  ResultMatcher statusReason(Matcher<T> matcher) {
        return jsonPath("$.message", matcher);
    }

}