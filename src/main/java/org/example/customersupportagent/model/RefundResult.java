package org.example.customersupportagent.model;

public class RefundResult {

    private boolean success;
    private String refundId;
    private Double amount;
    private String customerId;
    private String message;

    public RefundResult(boolean success, String refundId, Double amount, String customerId, String message) {
        this.success = success;
        this.refundId = refundId;
        this.amount = amount;
        this.customerId = customerId;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}