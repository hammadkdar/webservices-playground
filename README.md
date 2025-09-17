# CIS Pull Service

A Spring Boot 3.x application with Java 21 for CIS (Customer Information System) pull operations.

## Features

- REST API endpoint for CIS pull requests
- SOAP web service client integration
- Comprehensive logging interceptor for SOAP requests/responses
- Integration with sem-cbcm-client jar for generated SOAP classes

## API Endpoints

### POST /api/cis/pull
Pulls customer information from CIS system using the same structure as the SOAP web service but with JSON input/output.

**Request Body:**
The request accepts the same structure as `GetSubscriptionDtls` SOAP class but in JSON format. Example:
```json
{
  // Structure matches GetSubscriptionDtls fields
  // Add actual field structure based on your SOAP class
}
```

**Response:**
The response returns the same structure as `GetSubscriptionDtlsResponse` SOAP class but in JSON format:
```json
{
  // Structure matches GetSubscriptionDtlsResponse fields  
  // Add actual field structure based on your SOAP class
}
```

## Configuration

### Application Properties
- `webservice.cis.url`: SOAP service endpoint URL
- `webservice.cis.context-path`: Package path for generated SOAP classes
- Logging levels are configured for detailed SOAP request/response logging

### Dependencies
- Spring Boot 3.2.0
- Spring Web Services
- JAXB for XML marshalling/unmarshalling
- sem-cbcm-client jar (ae.etisalat.client group)

## Architecture

1. **CisPullController**: REST controller accepting `GetSubscriptionDtls` as JSON and returning `GetSubscriptionDtlsResponse` as JSON
2. **CisPullService**: Business logic layer calling SOAP web service with the same request/response objects
3. **WebServiceConfig**: Configuration for WebServiceTemplate and marshalling
4. **LoggingWebServiceInterceptor**: Intercepts and logs all SOAP communications

The controller now directly uses the SOAP request/response classes with JSON serialization, eliminating the need for DTO conversion layers.

## Logging

The application includes comprehensive logging of:
- HTTP headers (request and response)
- SOAP headers and body (request and response)
- Error handling and fault logging
- Debug level logging for detailed troubleshooting

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080 with context path `/cis-pull`.

## Testing

Test the endpoint:
```bash
curl -X POST http://localhost:8080/cis-pull/api/cis/pull \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST12345",
    "serviceType": "MOBILE"
  }'
```

## Notes

- Replace placeholder SOAP request/response processing in `CisPullService` with actual generated classes from sem-cbcm-client jar
- Update the web service URL in application.properties to point to your actual SOAP endpoint
- The logging interceptor will capture all SOAP communication for debugging purposes
