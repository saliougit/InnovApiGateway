// package com.innov4africa.api_gateway.model;

// public class ServiceStatus {
//     private String serviceName;
//     private boolean available;
//     private String message;

//     // Constructeurs, getters, setters
//     public ServiceStatus() {}

//     public ServiceStatus(String serviceName, boolean available, String message) {
//         this.serviceName = serviceName;
//         this.available = available;
//         this.message = message;
//     }

//     // Getters et setters
//     public String getServiceName() { return serviceName; }
//     public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
//     public boolean isAvailable() { return available; }
//     public void setAvailable(boolean available) { this.available = available; }
//     public String getMessage() { return message; }
//     public void setMessage(String message) { this.message = message; }
    
// }

package com.innov4africa.api_gateway.model;

public class ServiceStatus {
    private String serviceName;
    private boolean available;
    private String message;

    // Constructeurs, getters, setters
    public ServiceStatus() {}

    public ServiceStatus(String serviceName, boolean available, String message) {
        this.serviceName = serviceName;
        this.available = available;
        this.message = message;
    }

    // Getters et setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

}
