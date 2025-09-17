package ae.etisalat.cisapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CisPullResponseDto {

    private String requestId;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private List<CustomerServiceInfo> serviceInfoList;

    public CisPullResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public CisPullResponseDto(String requestId, String status, String message) {
        this();
        this.requestId = requestId;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<CustomerServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<CustomerServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }

    @Override
    public String toString() {
        return "CisPullResponseDto{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", serviceInfoList=" + serviceInfoList +
                '}';
    }

    // Inner class for service information
    public static class CustomerServiceInfo {
        private String serviceId;
        private String serviceName;
        private String serviceStatus;
        private String description;

        public CustomerServiceInfo() {}

        public CustomerServiceInfo(String serviceId, String serviceName, String serviceStatus, String description) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.serviceStatus = serviceStatus;
            this.description = description;
        }

        // Getters and Setters
        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceStatus() {
            return serviceStatus;
        }

        public void setServiceStatus(String serviceStatus) {
            this.serviceStatus = serviceStatus;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "CustomerServiceInfo{" +
                    "serviceId='" + serviceId + '\'' +
                    ", serviceName='" + serviceName + '\'' +
                    ", serviceStatus='" + serviceStatus + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
