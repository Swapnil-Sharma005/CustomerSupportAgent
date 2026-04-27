package org.example.customersupportagent.service;

import org.example.customersupportagent.gate.PrerequisiteGate;
import org.example.customersupportagent.model.Message;
import org.example.customersupportagent.model.Tool;
import org.example.customersupportagent.tools.GetCustomerTool;
import org.example.customersupportagent.tools.HandoffSummaryTool;
import org.example.customersupportagent.tools.LookupOrderTool;
import org.example.customersupportagent.tools.ProcessRefundTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomerSupportAgent {

    @Autowired private ClaudeApiService claudeApiService;
    @Autowired private GetCustomerTool getCustomerTool;
    @Autowired private LookupOrderTool lookupOrderTool;
    @Autowired private ProcessRefundTool processRefundTool;
    @Autowired private HandoffSummaryTool handoffTool;
    @Autowired private PrerequisiteGate gate;

    private static final String SYSTEM_PROMPT =
            "You are a customer support agent. " +

                    // role clarity
                    "Your job is to resolve customer queries about orders, refunds, and account issues. " +

                    // verification first — prompt guidance (gate handles enforcement)
                    "IMPORTANT: Always call get_customer FIRST before any other operation. " +
                    "Never process a refund without verifying the customer identity first. " +

                    // tool usage guidance
                    "Available tools and when to use them: " +
                    "- get_customer: call this FIRST with customer name or email to verify identity. " +
                    "- lookup_order: call this to find order details using order ID. " +
                    "- process_refund: call this ONLY after get_customer has verified the customer. " +
                    "- handoff: call this when you cannot resolve the issue or customer requests human. " +

                    // escalation criteria — explicit not vague
                    "Escalate to human via handoff tool when: " +
                    "1. Customer explicitly requests a human agent. " +
                    "2. Refund amount exceeds $500. " +
                    "3. Policy does not cover the customer's specific request. " +
                    "4. You cannot make meaningful progress after two attempts. " +

                    // output format
                    "Always be polite, concise, and solution-focused.";

    private static final int MAX_ITERATIONS = 20;

    private List<Tool> buildTools() {
        return List.of(
                GetCustomerTool.getToolDefinition(),
                LookupOrderTool.getToolDefinition(),
                ProcessRefundTool.getToolDefinition(),
                HandoffSummaryTool.getToolDefinition()
        );
    }

    public String handleRequest(String userMessage) {
        int iterationCount = 0;
        String sessionId = UUID.randomUUID().toString();
        // agent loop here
        List<Message> messages = new ArrayList<>();
        Message message = new Message("user", userMessage);
        messages.add(message);
        StringBuilder finalResponse = new StringBuilder();
        while (true) {

            if (iterationCount >= MAX_ITERATIONS) {
                System.out.println("WARNING : Max iterations reached");
                break;
            }
            iterationCount++;

            Map<String, Object> response;
            try {
                response = claudeApiService.sendMessage(messages,SYSTEM_PROMPT,buildTools());
            } catch (Exception e) {
                System.err.println("ERROR: Failed to call Claude API: " + e.getMessage());
                e.printStackTrace();
                break;
            }

            String stop_reason = response.get("stop_reason").toString();
            List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.get("content");

            if (contentList == null || contentList.isEmpty()) {
                System.err.println("ERROR: Content list is null or empty");
                break;
            }

            if (stop_reason.equals("end_turn")) {
                for (Map<String, Object> block : contentList) {
                    if ("text".equals(block.get("type"))) {
                        finalResponse.append(block.get("text"));
                    }
                }
                break;
            }

            if (stop_reason.equals("tool_use")) {
                // Find the tool_use block (Claude may send text blocks first)
                Map<String, Object> toolUseBlock = null;
                for (Map<String, Object> block : contentList) {
                    if (block != null && "tool_use".equals(block.get("type"))) {
                        toolUseBlock = block;
                        break;
                    }
                }

                if (toolUseBlock != null) {
                    Object nameObj = toolUseBlock.get("name");
                    Object inputObj = toolUseBlock.get("input");
                    Object idObj = toolUseBlock.get("id");

                    if (nameObj == null || inputObj == null || idObj == null) {
                        System.err.println("ERROR: Tool use block missing required fields");
                        continue;
                    }

                    String toolName = nameObj.toString();
                    Map<String, Object> input = (Map<String, Object>) inputObj;
                    String toolResult = "";

                    try {
                        if (toolName.equals("get_customer")) {
                            String nameOrEmail = input.get("name_or_email").toString();
                            if (nameOrEmail != null) {
                                toolResult = String.valueOf(getCustomerTool.execute(sessionId, nameOrEmail));
                            }
                        } else if (toolName.equals("handoff_summary")) {
                            String conversationSummary = input.get("conversation_summary").toString();
                            String rootCauseAnalysis = input.get("root_cause_analysis").toString();
                            Double refundAmount = Double.valueOf(input.get("refund_amount").toString());
                            String recommendedAction = input.get("recommended_action").toString();
                            if (conversationSummary != null) {
                                toolResult = String.valueOf(handoffTool.execute(sessionId, conversationSummary, rootCauseAnalysis, refundAmount, recommendedAction));
                            }
                        } else if (toolName.equals("lookup_order")) {
                            String orderId = input.get("order_id").toString();
                            if (orderId != null)
                                toolResult = String.valueOf(lookupOrderTool.execute(orderId));
                        } else if (toolName.equals("process_refund")) {
                            String customerId = input.get("customer_id").toString();
                            Double amount = Double.valueOf(input.get("amount").toString());
                            if (customerId != null) {
                                toolResult = String.valueOf(processRefundTool.execute(sessionId, customerId, amount));
                            }
                        }
                    } catch (Exception e) {
                        toolResult = "Error executing tool: " + e.getMessage();
                        System.err.println("ERROR executing tool '" + toolName + "': " + e.getMessage());
                    }

                    messages.add(new Message("assistant", contentList));

                    Map<String, Object> toolResultBlock = new HashMap<>();
                    toolResultBlock.put("type", "tool_result");
                    toolResultBlock.put("tool_use_id", idObj);
                    toolResultBlock.put("content", toolResult);
                    messages.add(new Message("user", List.of(toolResultBlock)));
                }
            }
        }
        return finalResponse.toString();
    }
}
