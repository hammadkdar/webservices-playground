package ae.etisalat.cisapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ws.client.core.WebServiceTemplate;
import sem.cis.pull.cbcm.GetSubscriptionDtls;
import sem.cis.pull.cbcm.GetSubscriptionDtlsResponse;

import jakarta.xml.bind.JAXBElement;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CisPullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebServiceTemplate webServiceTemplate;

    private GetSubscriptionDtls sampleRequest;
    private GetSubscriptionDtlsResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // Create sample request object
        sampleRequest = new GetSubscriptionDtls();

        // Create sample response object
        sampleResponse = new GetSubscriptionDtlsResponse();
    }

    @Test
    public void testCisPullEndpoint_Success() throws Exception {
        // Mock the SOAP web service call to return a successful response
        JAXBElement<GetSubscriptionDtlsResponse> mockResponseElement = mock(JAXBElement.class);
        when(mockResponseElement.getValue()).thenReturn(sampleResponse);
        when(webServiceTemplate.marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class)))
                .thenReturn(mockResponseElement);

        // Convert request object to JSON
        String requestJson = objectMapper.writeValueAsString(sampleRequest);

        // Perform the REST API call
        mockMvc.perform(post("/api/cis/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());

        // Verify that the web service template was called
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class));
    }

    @Test
    public void testCisPullEndpoint_WithValidRequestStructure() throws Exception {
        // Mock successful SOAP response
        JAXBElement<GetSubscriptionDtlsResponse> mockResponseElement = mock(JAXBElement.class);
        when(mockResponseElement.getValue()).thenReturn(sampleResponse);
        when(webServiceTemplate.marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class)))
                .thenReturn(mockResponseElement);

        // Create a detailed request JSON
        String requestJson = "{\n" +
                "    \"customerId\": \"CUST12345\",\n" +
                "    \"serviceType\": \"MOBILE\"\n" +
                "}";

        mockMvc.perform(post("/api/cis/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify web service call was made
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class));
    }

    @Test
    public void testCisPullEndpoint_ServiceException() throws Exception {
        // Mock the SOAP web service to throw an exception
        when(webServiceTemplate.marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class)))
                .thenThrow(new RuntimeException("SOAP service unavailable"));

        String requestJson = objectMapper.writeValueAsString(sampleRequest);

        // Expect internal server error when service throws exception
        mockMvc.perform(post("/api/cis/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());

        // Verify web service call was attempted
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class));
    }

    @Test
    public void testCisPullEndpoint_NullResponse() throws Exception {
        // Mock the SOAP web service to return null
        when(webServiceTemplate.marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class)))
                .thenReturn(null);

        String requestJson = objectMapper.writeValueAsString(sampleRequest);

        mockMvc.perform(post("/api/cis/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Verify web service call was made
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class));
    }

    @Test
    public void testCisPullEndpoint_InvalidJson() throws Exception {
        // Send invalid JSON
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/cis/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        // Verify web service was not called due to invalid JSON
        verify(webServiceTemplate, never()).marshalSendAndReceive(ArgumentMatchers.any());
    }

    @Test
    public void testCisPullEndpoint_EmptyRequest() throws Exception {
        // Send empty JSON object
        String emptyJson = "{}";

        // Mock successful response for empty request
        JAXBElement<GetSubscriptionDtlsResponse> mockResponseElement = mock(JAXBElement.class);
        when(mockResponseElement.getValue()).thenReturn(sampleResponse);
        when(webServiceTemplate.marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class)))
                .thenReturn(mockResponseElement);

        mockMvc.perform(post("/api/cis/pull")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isOk());

        // Verify web service call was made even with empty request
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(ArgumentMatchers.any(JAXBElement.class));
    }

    @Test
    public void testCisPullEndpoint_WrongHttpMethod() throws Exception {
        mockMvc.perform(get("/api/cis/pull"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testCisPullEndpoint_MissingContentType() throws Exception {
        String requestJson = objectMapper.writeValueAsString(sampleRequest);

        // Send request without Content-Type header
        mockMvc.perform(post("/api/cis/pull")
                        .content(requestJson))
                .andExpect(status().isUnsupportedMediaType());
    }
}
