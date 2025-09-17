package ae.etisalat.cisapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Custom message sender that provides enhanced logging capabilities including HTTP headers
 */
public class LoggingHttpUrlConnectionMessageSender extends HttpUrlConnectionMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(LoggingHttpUrlConnectionMessageSender.class);

    @Override
    public WebServiceConnection createConnection(URI uri) throws IOException {
        WebServiceConnection connection = super.createConnection(uri);

        // Wrap the connection to enable header logging
        return new LoggingWebServiceConnectionWrapper(connection);
    }

    private static class LoggingWebServiceConnectionWrapper implements WebServiceConnection {
        private final WebServiceConnection delegate;
        private final Logger wrapperLogger = LoggerFactory.getLogger(LoggingWebServiceConnectionWrapper.class);

        public LoggingWebServiceConnectionWrapper(WebServiceConnection delegate) {
            this.delegate = delegate;
        }

        @Override
        public void send(org.springframework.ws.WebServiceMessage message) throws IOException {
            wrapperLogger.debug("=== SENDING HTTP REQUEST ===");

            // Try to extract and log HTTP headers before sending
            logOutgoingHeaders();

            delegate.send(message);

            wrapperLogger.debug("=== HTTP REQUEST SENT ===");
        }

        @Override
        public org.springframework.ws.WebServiceMessage receive(org.springframework.ws.WebServiceMessageFactory messageFactory) throws IOException {
            wrapperLogger.debug("=== RECEIVING HTTP RESPONSE ===");

            org.springframework.ws.WebServiceMessage response = delegate.receive(messageFactory);

            // Log incoming headers after receiving
            logIncomingHeaders();

            wrapperLogger.debug("=== HTTP RESPONSE RECEIVED ===");

            return response;
        }

        private void logOutgoingHeaders() {
            try {
                // Access the underlying HttpURLConnection if possible
                if (delegate instanceof org.springframework.ws.transport.http.HttpUrlConnection) {
                    org.springframework.ws.transport.http.HttpUrlConnection httpConn =
                        (org.springframework.ws.transport.http.HttpUrlConnection) delegate;

                    // Use reflection to access the underlying connection
                    java.lang.reflect.Field connectionField = httpConn.getClass().getDeclaredField("connection");
                    connectionField.setAccessible(true);
                    Object underlyingConn = connectionField.get(httpConn);

                    if (underlyingConn instanceof HttpURLConnection) {
                        HttpURLConnection urlConn = (HttpURLConnection) underlyingConn;

                        wrapperLogger.debug("Outgoing HTTP Method: {}", urlConn.getRequestMethod());
                        wrapperLogger.debug("Outgoing HTTP URL: {}", urlConn.getURL());

                        // Log request properties (headers)
                        var requestProperties = urlConn.getRequestProperties();
                        if (!requestProperties.isEmpty()) {
                            wrapperLogger.debug("Outgoing HTTP Headers:");
                            requestProperties.forEach((key, values) ->
                                values.forEach(value -> wrapperLogger.debug("  {}: {}", key, value))
                            );
                        }
                    }
                }
            } catch (Exception e) {
                wrapperLogger.debug("Could not log outgoing headers: {}", e.getMessage());
            }
        }

        private void logIncomingHeaders() {
            try {
                // Access the underlying HttpURLConnection if possible
                if (delegate instanceof org.springframework.ws.transport.http.HttpUrlConnection) {
                    org.springframework.ws.transport.http.HttpUrlConnection httpConn =
                        (org.springframework.ws.transport.http.HttpUrlConnection) delegate;

                    // Use reflection to access the underlying connection
                    java.lang.reflect.Field connectionField = httpConn.getClass().getDeclaredField("connection");
                    connectionField.setAccessible(true);
                    Object underlyingConn = connectionField.get(httpConn);

                    if (underlyingConn instanceof HttpURLConnection) {
                        HttpURLConnection urlConn = (HttpURLConnection) underlyingConn;

                        wrapperLogger.debug("Incoming HTTP Response Code: {}", urlConn.getResponseCode());
                        wrapperLogger.debug("Incoming HTTP Response Message: {}", urlConn.getResponseMessage());

                        // Log response headers
                        var headerFields = urlConn.getHeaderFields();
                        if (!headerFields.isEmpty()) {
                            wrapperLogger.debug("Incoming HTTP Headers:");
                            headerFields.forEach((key, values) -> {
                                if (key != null) {
                                    values.forEach(value -> wrapperLogger.debug("  {}: {}", key, value));
                                } else {
                                    // Status line
                                    wrapperLogger.debug("  Status: {}", values.get(0));
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                wrapperLogger.debug("Could not log incoming headers: {}", e.getMessage());
            }
        }

        @Override
        public URI getUri() {
            try {
                return delegate.getUri();
            } catch (Exception e) {
                wrapperLogger.debug("Error getting URI: {}", e.getMessage());
                return null;
            }
        }

        @Override
        public boolean hasError() throws IOException {
            return delegate.hasError();
        }

        @Override
        public String getErrorMessage() throws IOException {
            return delegate.getErrorMessage();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
