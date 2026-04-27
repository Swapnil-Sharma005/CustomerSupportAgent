package org.example.customersupportagent.tools;

import org.example.customersupportagent.gate.PrerequisiteGate;
import org.example.customersupportagent.model.RefundResult;
import org.example.customersupportagent.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessRefundTool {

    @Autowired
    private PrerequisiteGate gate;

    public static Tool getToolDefinition(){
        Map<String,Object> schema = new HashMap<>();
        schema.put("type","object");
        Map<String,Object> properties = new HashMap<>();
        Map<String,Object> customerId = new HashMap<>();
        customerId.put("type","string");
        customerId.put("description","Id of the customer");
        properties.put("customer_id",customerId);

        Map<String,Object> amount = new HashMap<>();
        amount.put("type","number");
        amount.put("description","the amount that customer paid for the order means the refund ammount");
        properties.put("amount",amount);

        schema.put("properties",properties);
        schema.put("required", List.of("customer_id","amount"));
        return new Tool("process_refund","Process the refund for verified customer",schema);

    }

    public RefundResult execute(String sessionId, String customerId, Double amount) {
        // 1. check gate first — blocks if not verified - will throw Exception if not verified.
        gate.checkRefundAllowed(sessionId);
        String refundId = "REF-" + customerId + "-" + System.currentTimeMillis();
        return new RefundResult(true, refundId, amount, customerId, "Refund processed successfully");
    }
}