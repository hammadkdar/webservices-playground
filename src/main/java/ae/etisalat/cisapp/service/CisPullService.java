package ae.etisalat.cisapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import sem.cis.pull.cbcm.GetSubscriptionDtls;
import sem.cis.pull.cbcm.GetSubscriptionDtlsResponse;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;

@Service
public class CisPullService {

    private static final Logger logger = LoggerFactory.getLogger(CisPullService.class);

    private final WebServiceTemplate webServiceTemplate;

    @Autowired
    public CisPullService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public GetSubscriptionDtlsResponse processCisPullRequest(GetSubscriptionDtls request) {
        logger.info("Processing CIS pull request: {}", request);

        try {
            // Wrap the request in JAXBElement to provide root element information
            QName requestQName = new QName("http://cbcm.pull.cis.sem/", "getSubscriptionDtls");
            JAXBElement<GetSubscriptionDtls> requestElement = new JAXBElement<>(
                requestQName,
                GetSubscriptionDtls.class,
                request
            );

            // Call SOAP web service with wrapped request
            JAXBElement<GetSubscriptionDtlsResponse> responseElement =
                (JAXBElement<GetSubscriptionDtlsResponse>) webServiceTemplate.marshalSendAndReceive(requestElement);

            if (responseElement != null && responseElement.getValue() != null) {
                GetSubscriptionDtlsResponse soapResponse = responseElement.getValue();
                logger.info("Successfully processed CIS pull request");
                return soapResponse;
            } else {
                logger.warn("Received null SOAP response");
                GetSubscriptionDtlsResponse errorResponse = new GetSubscriptionDtlsResponse();
                // Set error fields based on actual response structure
                // Note: Replace these with actual field setters based on your SOAP response class
                try {
                    // Attempt to set common error fields - adjust based on actual GetSubscriptionDtlsResponse structure
                    // errorResponse.setStatus("ERROR");
                    // errorResponse.setErrorCode("SOAP_NULL_RESPONSE");
                    // errorResponse.setErrorMessage("Received empty response from SOAP service");
                    // errorResponse.setTimestamp(new Date());
                    logger.info("Created error response for null SOAP response");
                } catch (Exception ex) {
                    logger.warn("Could not set error fields on response object: {}", ex.getMessage());
                }
                return errorResponse;
            }

        } catch (Exception e) {
            logger.error("Error processing CIS pull request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CIS pull request", e);
        }
    }
}
