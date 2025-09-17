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
import java.net.URI;
import java.nio.charset.StandardCharsets;

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

            // Enhanced transport context handling with fallbacks
            logTransportContext("REQUEST");
            logConnectionDetails(messageContext, "REQUEST");

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

            // Enhanced transport context handling with fallbacks
            logTransportContext("RESPONSE");
            logConnectionDetails(messageContext, "RESPONSE");

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

    private void logTransportContext(String phase) {
        try {
            TransportContext transportContext = TransportContextHolder.getTransportContext();

            if (transportContext == null) {
                logger.debug("{} Transport Context: NULL - This is common in test environments or when using mock clients", phase);
                return;
            }

            logger.debug("{} Transport Context available: {}", phase, transportContext.getClass().getSimpleName());

            Object connection = transportContext.getConnection();
            if (connection == null) {
                logger.debug("{} Transport Connection: NULL", phase);
                return;
            }

            logger.debug("{} Transport Connection Type: {}", phase, connection.getClass().getName());
            logger.debug("{} Transport Connection Details: {}", phase, connection.toString());

            // Enhanced connection type handling
            if (connection instanceof HttpUrlConnection) {
                HttpUrlConnection httpConnection = (HttpUrlConnection) connection;
                logHttpUrlConnection(httpConnection, phase);
            } else if (connection instanceof org.springframework.ws.transport.http.HttpUrlConnection) {
                // Try the Spring WS HttpUrlConnection directly
                org.springframework.ws.transport.http.HttpUrlConnection springHttpConnection =
                        (org.springframework.ws.transport.http.HttpUrlConnection) connection;
                logSpringHttpUrlConnection(springHttpConnection, phase);
            } else {
                logger.debug("{} Connection is not a recognized HttpUrlConnection type: {}", phase, connection.getClass().getName());
                logger.debug("{} Attempting generic connection analysis...", phase);
                // Try to extract what information we can
                logGenericConnection(connection, phase);
                // Also try reflection-based HTTP analysis
                tryReflectionBasedHttpAnalysis(connection, phase);
            }

        } catch (Exception e) {
            logger.debug("Error accessing transport context for {}: {}", phase, e.getMessage());
        }
    }

    private void logHttpUrlConnection(HttpUrlConnection connection, String phase) {
        try {
            // Try to get URI information
            URI uri = connection.getUri();
            if (uri != null) {
                logger.debug("{} HTTP URI: {}", phase, uri.toString());
                logger.debug("{} HTTP Host: {}", phase, uri.getHost());
                logger.debug("{} HTTP Port: {}", phase, uri.getPort());
                logger.debug("{} HTTP Path: {}", phase, uri.getPath());
            } else {
                logger.debug("{} HTTP URI: Not available", phase);
            }

            // Enhanced header logging attempts
            logHttpHeaders(connection, phase);

        } catch (Exception e) {
            logger.debug("Error extracting HTTP connection details for {}: {}", phase, e.getMessage());
        }
    }

    private void logSpringHttpUrlConnection(org.springframework.ws.transport.http.HttpUrlConnection connection, String phase) {
        try {
            logger.debug("{} Spring WS HttpUrlConnection detected", phase);

            // Try to get URI information
            URI uri = connection.getUri();
            if (uri != null) {
                logger.debug("{} HTTP URI: {}", phase, uri.toString());
                logger.debug("{} HTTP Host: {}", phase, uri.getHost());
                logger.debug("{} HTTP Port: {}", phase, uri.getPort());
                logger.debug("{} HTTP Path: {}", phase, uri.getPath());
            } else {
                logger.debug("{} HTTP URI: Not available", phase);
            }

            // Enhanced header logging for Spring WS connection
            logSpringHttpHeaders(connection, phase);

        } catch (Exception e) {
            logger.debug("Error extracting Spring HTTP connection details for {}: {}", phase, e.getMessage());
        }
    }

    private void logHttpHeaders(HttpUrlConnection connection, String phase) {
        try {
            // Method 1: Try direct header access (may not work in all Spring WS versions)
            try {
                if (connection instanceof org.springframework.ws.transport.http.HttpUrlConnection) {
                    // Cast to Spring WS HttpUrlConnection and try reflection
                    java.lang.reflect.Method getHeadersMethod = connection.getClass().getMethod("getHeaders");
                    if (getHeadersMethod != null) {
                        Object headers = getHeadersMethod.invoke(connection);
                        logger.debug("{} HTTP Headers (via reflection): {}", phase, headers);
                        return;
                    }
                }
            } catch (Exception reflectionEx) {
                logger.trace("Direct header access failed: {}", reflectionEx.getMessage());
            }

            // Method 2: Try to access underlying HttpURLConnection if available
            try {
                java.lang.reflect.Field connectionField = connection.getClass().getDeclaredField("connection");
                if (connectionField != null) {
                    connectionField.setAccessible(true);
                    Object underlyingConnection = connectionField.get(connection);

                    if (underlyingConnection instanceof java.net.HttpURLConnection) {
                        java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) underlyingConnection;
                        logger.debug("{} HTTP Method: {}", phase, httpConn.getRequestMethod());
                        logger.debug("{} HTTP Response Code: {}", phase,
                                "REQUEST".equals(phase) ? "Not available yet" : httpConn.getResponseCode());

                        // Log request headers
                        if ("REQUEST".equals(phase)) {
                            logRequestHeaders(httpConn, phase);
                        } else {
                            logResponseHeaders(httpConn, phase);
                        }
                        return;
                    }
                }
            } catch (Exception fieldEx) {
                logger.trace("Underlying connection access failed: {}", fieldEx.getMessage());
            }

            // Method 3: Fallback - log what we know
            logger.debug("{} HTTP Headers: Unable to access at interceptor level", phase);
            logger.debug("{} Note: Consider using WebServiceMessageSender customization for header logging", phase);

        } catch (Exception e) {
            logger.debug("Error logging HTTP headers for {}: {}", phase, e.getMessage());
        }
    }

    private void logSpringHttpHeaders(org.springframework.ws.transport.http.HttpUrlConnection connection, String phase) {
        try {
            // Method 1: Try to access underlying HttpURLConnection
            try {
                java.lang.reflect.Field connectionField = connection.getClass().getDeclaredField("connection");
                if (connectionField != null) {
                    connectionField.setAccessible(true);
                    Object underlyingConnection = connectionField.get(connection);

                    if (underlyingConnection instanceof java.net.HttpURLConnection) {
                        java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) underlyingConnection;
                        logger.debug("{} HTTP Method: {}", phase, httpConn.getRequestMethod());
                        logger.debug("{} HTTP Response Code: {}", phase,
                                "REQUEST".equals(phase) ? "Not available yet" : httpConn.getResponseCode());

                        // Log request headers
                        if ("REQUEST".equals(phase)) {
                            logRequestHeaders(httpConn, phase);
                        } else {
                            logResponseHeaders(httpConn, phase);
                        }
                        return;
                    } else {
                        logger.debug("{} Underlying connection is not HttpURLConnection: {}", phase,
                                underlyingConnection != null ? underlyingConnection.getClass().getName() : "null");
                    }
                }
            } catch (Exception fieldEx) {
                logger.debug("Underlying connection access failed: {}", fieldEx.getMessage());
            }

            // Method 2: Try other field names that might contain the HTTP connection
            String[] possibleFieldNames = {"httpConnection", "urlConnection", "conn", "httpConn"};
            for (String fieldName : possibleFieldNames) {
                try {
                    java.lang.reflect.Field field = connection.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object fieldValue = field.get(connection);

                    if (fieldValue instanceof java.net.HttpURLConnection) {
                        java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) fieldValue;
                        logger.debug("{} Found HTTP connection via field '{}': {}", phase, fieldName, httpConn.getClass().getName());

                        if ("REQUEST".equals(phase)) {
                            logRequestHeaders(httpConn, phase);
                        } else {
                            logResponseHeaders(httpConn, phase);
                        }
                        return;
                    }
                } catch (NoSuchFieldException nsfe) {
                    // Continue trying other field names
                } catch (Exception e) {
                    logger.debug("Error accessing field '{}': {}", fieldName, e.getMessage());
                }
            }

            // Method 3: List all available fields for debugging
            logger.debug("{} Available fields in connection class:", phase);
            java.lang.reflect.Field[] fields = connection.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                logger.debug("  - Field: {} (type: {})", field.getName(), field.getType().getSimpleName());
            }

            logger.debug("{} HTTP Headers: Unable to access through reflection", phase);

        } catch (Exception e) {
            logger.debug("Error logging Spring HTTP headers for {}: {}", phase, e.getMessage());
        }
    }

    private void logRequestHeaders(java.net.HttpURLConnection connection, String phase) {
        try {
            java.util.Map<String, java.util.List<String>> requestProperties = connection.getRequestProperties();
            if (requestProperties != null && !requestProperties.isEmpty()) {
                logger.debug("{} HTTP Request Headers:", phase);
                requestProperties.forEach((key, values) ->
                        values.forEach(value -> logger.debug("  {}: {}", key, value))
                );
            } else {
                logger.debug("{} HTTP Request Headers: None or not accessible", phase);
            }
        } catch (Exception e) {
            logger.debug("Error logging request headers: {}", e.getMessage());
        }
    }

    private void logResponseHeaders(java.net.HttpURLConnection connection, String phase) {
        try {
            java.util.Map<String, java.util.List<String>> headerFields = connection.getHeaderFields();
            if (headerFields != null && !headerFields.isEmpty()) {
                logger.debug("{} HTTP Response Headers:", phase);
                headerFields.forEach((key, values) -> {
                    if (key != null) { // Status line has null key
                        values.forEach(value -> logger.debug("  {}: {}", key, value));
                    } else {
                        logger.debug("  Status: {}", values.get(0));
                    }
                });
            } else {
                logger.debug("{} HTTP Response Headers: None or not accessible", phase);
            }
        } catch (Exception e) {
            logger.debug("Error logging response headers: {}", e.getMessage());
        }
    }

    private void logGenericConnection(Object connection, String phase) {
        try {
            logger.debug("{} Generic Connection Info:", phase);
            logger.debug("  - Class: {}", connection.getClass().getName());
            logger.debug("  - toString(): {}", connection.toString());

            // Try to extract any useful information using reflection if available
            try {
                if (connection.getClass().getMethod("getUri") != null) {
                    Object uri = connection.getClass().getMethod("getUri").invoke(connection, (Object[]) null);
                    logger.debug("  - URI (via reflection): {}", uri);
                }
            } catch (Exception reflectionEx) {
                logger.debug("  - URI extraction failed: {}", reflectionEx.getMessage());
            }

        } catch (Exception e) {
            logger.debug("Error extracting generic connection details for {}: {}", phase, e.getMessage());
        }
    }

    private void logConnectionDetails(MessageContext messageContext, String phase) {
        try {
            // Alternative approach: Try to extract connection info from message context properties
            logger.debug("{} Message Context Properties:", phase);

            // Check for common WebServiceTemplate properties that might contain connection info
            Object endpointUri = messageContext.getProperty("org.springframework.ws.client.core.WebServiceTemplate.URI_TEMPLATE_VARIABLES");
            if (endpointUri != null) {
                logger.debug("  - Endpoint URI Template Variables: {}", endpointUri);
            }

            // Log available property names for debugging
            if (logger.isTraceEnabled()) {
                logger.trace("  - Available properties: {}", messageContext.getPropertyNames());
            }

        } catch (Exception e) {
            logger.debug("Error extracting connection details from message context for {}: {}", phase, e.getMessage());
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

    private void tryReflectionBasedHttpAnalysis(Object connection, String phase) {
        try {
            logger.debug("{} Attempting reflection-based HTTP analysis...", phase);

            Class<?> connectionClass = connection.getClass();

            // Try to find methods that might give us HTTP information
            java.lang.reflect.Method[] methods = connectionClass.getDeclaredMethods();
            logger.debug("{} Available methods in {}:", phase, connectionClass.getSimpleName());

            for (java.lang.reflect.Method method : methods) {
                if (method.getParameterCount() == 0) { // Only no-arg methods
                    String methodName = method.getName();
                    if (methodName.toLowerCase().contains("uri") ||
                        methodName.toLowerCase().contains("url") ||
                        methodName.toLowerCase().contains("header") ||
                        methodName.toLowerCase().contains("connection")) {

                        logger.debug("  - Method: {} -> {}", methodName, method.getReturnType().getSimpleName());

                        try {
                            method.setAccessible(true);
                            Object result = method.invoke(connection);
                            if (result != null) {
                                logger.debug("    Result: {}", result.toString());

                                // If we got an HttpURLConnection, try to log its headers
                                if (result instanceof java.net.HttpURLConnection) {
                                    java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) result;
                                    logger.debug("{} Found HttpURLConnection via method '{}': {}", phase, methodName, httpConn.getClass().getName());

                                    if ("REQUEST".equals(phase)) {
                                        logRequestHeaders(httpConn, phase);
                                    } else {
                                        logResponseHeaders(httpConn, phase);
                                    }
                                    return;
                                }
                            }
                        } catch (Exception methodEx) {
                            logger.debug("    Error invoking {}: {}", methodName, methodEx.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.debug("Error in reflection-based HTTP analysis for {}: {}", phase, e.getMessage());
        }
    }
}
