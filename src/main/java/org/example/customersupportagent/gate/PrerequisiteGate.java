package org.example.customersupportagent.gate;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PrerequisiteGate {

    private final Map<String, String> verifiedCustomerIds = new ConcurrentHashMap<>();

    public void markCustomerVerified(String sessionId, String customerId) {
        verifiedCustomerIds.put(sessionId, customerId);
    }

    public void checkRefundAllowed(String sessionId) {
        if (!verifiedCustomerIds.containsKey(sessionId)) {
            throw new RuntimeException(
                    "GATE BLOCKED: process_refund requires verified customer. " +
                            "Call get_customer first."
            );
        }
    }

    public String getVerifiedCustomerId(String sessionId) {
        return verifiedCustomerIds.get(sessionId);
    }

    public void clearSession(String sessionId) {
        verifiedCustomerIds.remove(sessionId);
    }
}