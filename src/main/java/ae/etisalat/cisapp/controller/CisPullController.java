package ae.etisalat.cisapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sem.cis.pull.cbcm.GetSubscriptionDtls;
import sem.cis.pull.cbcm.GetSubscriptionDtlsResponse;
import ae.etisalat.cisapp.service.CisPullService;

@RestController
@RequestMapping("/api/cis")
public class CisPullController {

    private static final Logger logger = LoggerFactory.getLogger(CisPullController.class);

    private final CisPullService cisPullService;

    @Autowired
    public CisPullController(CisPullService cisPullService) {
        this.cisPullService = cisPullService;
    }

    @PostMapping("/pull")
    public ResponseEntity<GetSubscriptionDtlsResponse> pullCustomerInfo(@RequestBody GetSubscriptionDtls request) {
        logger.info("Received CIS pull request: {}", request);

        try {
            GetSubscriptionDtlsResponse response = cisPullService.processCisPullRequest(request);
            logger.info("Successfully processed CIS pull request");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing CIS pull request: {}", e.getMessage(), e);
            // Return error response in the same format as the SOAP response
            GetSubscriptionDtlsResponse errorResponse = new GetSubscriptionDtlsResponse();
            // You may need to set error fields based on the actual response structure
            // errorResponse.setStatus("ERROR");
            // errorResponse.setMessage("Failed to process request: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
