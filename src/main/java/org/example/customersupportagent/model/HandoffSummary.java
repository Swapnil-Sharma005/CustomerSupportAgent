package org.example.customersupportagent.model;

public class HandoffSummary {
    private String customerId;
    private String conversationSummary;
    private String rootCauseAnalysis;
    private Double refundAmount;
    private String recommendedAction;

    public HandoffSummary(String recommendedAction, Double refundAmount, String conversationSummary, String rootCauseAnalysis, String customerId) {
        this.recommendedAction = recommendedAction;
        this.refundAmount = refundAmount;
        this.conversationSummary = conversationSummary;
        this.rootCauseAnalysis = rootCauseAnalysis;
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getConversationSummary() {
        return conversationSummary;
    }

    public void setConversationSummary(String conversationSummary) {
        this.conversationSummary = conversationSummary;
    }

    public String getRootCauseAnalysis() {
        return rootCauseAnalysis;
    }

    public void setRootCauseAnalysis(String rootCauseAnalysis) {
        this.rootCauseAnalysis = rootCauseAnalysis;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
}