package org.example.customersupportagent.tools;

import org.example.customersupportagent.gate.PrerequisiteGate;
import org.example.customersupportagent.model.HandoffSummary;
import org.example.customersupportagent.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HandoffSummaryTool {

    @Autowired
    private PrerequisiteGate gate;

    public static Tool getToolDefinition() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> conversationSummaryProp = new HashMap<>();
        conversationSummaryProp.put("type", "string");
        conversationSummaryProp.put("description", "Conversation summary till now");
        properties.put("conversation_summary", conversationSummaryProp);

        Map<String,Object> rootCauseAnalysis = new HashMap<>();
        rootCauseAnalysis.put("type", "string");
        rootCauseAnalysis.put("description" ,"The root cause of customer query");
        properties.put("root_cause_analysis", rootCauseAnalysis);

        Map<String,Object> refundAmount = new HashMap<>();
        refundAmount.put("type","number");
        refundAmount.put("description","The total amound that needs to be refunded");
        properties.put("refund_amount", refundAmount);

        Map<String,Object> recommendedAction = new HashMap<>();
        recommendedAction.put("type","string");
        recommendedAction.put("description","The recommended Action for the customer query that you handoff to the Human");
        properties.put("recommended_action", recommendedAction);

        schema.put("properties", properties);
        schema.put("required", List.of(
                "conversation_summary",
                "root_cause_analysis",
                "refund_amount",
                "recommended_action"));

        return new Tool("handoff_summary",
                "Escalate to human agent with structured summary",
                schema);
    }

    public HandoffSummary execute(String sessionId,
                                  String conversationSummary,
                                  String rootCauseAnalysis,
                                  Double refundAmount,
                                  String recommendedAction) {
        // 1. get verified customerId from gate
        String customerId = gate.getVerifiedCustomerId(sessionId);
        if (customerId == null) {
            customerId = "UNVERIFIED"; // handoff even without verification
        }
        HandoffSummary handoffSummary = new HandoffSummary(recommendedAction,refundAmount,conversationSummary,rootCauseAnalysis,customerId);
        gate.clearSession(sessionId);
        return handoffSummary;
    }
}