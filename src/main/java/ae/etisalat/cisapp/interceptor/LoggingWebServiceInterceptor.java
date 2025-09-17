package ae.etisalat.cisapp.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Component
public class LoggingWebServiceInterceptor implements ClientInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingWebServiceInterceptor.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        if (logger.isDebugEnabled()) {
            logRequest(messageContext);
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        if (logger.isDebugEnabled()) {
            logResponse(messageContext);
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        if (logger.isErrorEnabled()) {
            logger.error("SOAP Fault occurred during web service call");
            logResponse(messageContext);
        }
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        if (ex != null) {
            logger.error("Exception occurred during web service call: {}", ex.getMessage(), ex);
        }
    }

    private void logRequest(MessageContext messageContext) {
        try {
            logger.debug("=== OUTBOUND SOAP REQUEST ===");

            // Log HTTP headers - simplified approach for Spring WS
            TransportContext transportContext = TransportContextHolder.getTransportContext();
            if (transportContext != null && transportContext.getConnection() instanceof HttpUrlConnection) {
                HttpUrlConnection connection = (HttpUrlConnection) transportContext.getConnection();
                // Note: HTTP headers are not directly accessible in Spring WS client interceptors
                // Headers would be logged at the HTTP transport level
                logger.debug("HTTP Connection established for SOAP request");
            }

            // Log SOAP body
            if (messageContext.getRequest() instanceof SoapMessage) {
                SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
                String soapBody = extractSoapContent(soapMessage.getPayloadSource());
                logger.debug("Request SOAP Body:\n{}", soapBody);

                // Log SOAP headers
                if (soapMessage.getSoapHeader() != null) {
                    String soapHeaders = extractSoapContent(soapMessage.getSoapHeader().getSource());
                    logger.debug("Request SOAP Headers:\n{}", soapHeaders);
                }
            }

            logger.debug("=== END OUTBOUND SOAP REQUEST ===");

        } catch (Exception e) {
            logger.warn("Failed to log SOAP request: {}", e.getMessage());
        }
    }

    private void logResponse(MessageContext messageContext) {
        try {
            logger.debug("=== INBOUND SOAP RESPONSE ===");

            // Log HTTP connection info - simplified approach
            TransportContext transportContext = TransportContextHolder.getTransportContext();
            if (transportContext != null && transportContext.getConnection() instanceof HttpUrlConnection) {
                HttpUrlConnection connection = (HttpUrlConnection) transportContext.getConnection();
                logger.debug("HTTP Connection used for SOAP response");
            }

            // Log SOAP body
            if (messageContext.hasResponse() && messageContext.getResponse() instanceof SoapMessage) {
                SoapMessage soapMessage = (SoapMessage) messageContext.getResponse();
                String soapBody = extractSoapContent(soapMessage.getPayloadSource());
                logger.debug("Response SOAP Body:\n{}", soapBody);

                // Log SOAP headers
                if (soapMessage.getSoapHeader() != null) {
                    String soapHeaders = extractSoapContent(soapMessage.getSoapHeader().getSource());
                    logger.debug("Response SOAP Headers:\n{}", soapHeaders);
                }
            }

            logger.debug("=== END INBOUND SOAP RESPONSE ===");

        } catch (Exception e) {
            logger.warn("Failed to log SOAP response: {}", e.getMessage());
        }
    }

    private String extractSoapContent(Source source) {
        try {
            if (source == null) {
                return "No content";
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(outputStream);

            transformer.transform(source, result);
            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.warn("Failed to extract SOAP content: {}", e.getMessage());
            return "Failed to extract content: " + e.getMessage();
        }
    }
}
