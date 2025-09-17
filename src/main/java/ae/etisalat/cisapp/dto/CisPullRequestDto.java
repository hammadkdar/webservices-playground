package ae.etisalat.cisapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CisPullRequestDto {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Service type is required")
    private String serviceType;

    private String requestId;

    public CisPullRequestDto() {}

    public CisPullRequestDto(String customerId, String serviceType, String requestId) {
        this.customerId = customerId;
        this.serviceType = serviceType;
        this.requestId = requestId;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "CisPullRequestDto{" +
                "customerId='" + customerId + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
