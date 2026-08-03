package com.SwiftSort.MLTicketClassifier.dto;

public class TicketCreateRequest {
    private String subject;
    private String message;
    private String customerReference;
    private String product;
    private String source;
    private String category;
    private String priority;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCustomerReference() { return customerReference; }
    public void setCustomerReference(String customerReference) { this.customerReference = customerReference; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}