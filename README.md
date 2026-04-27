# Customer Support Agent — Java Spring Boot

A Java Spring Boot implementation of a customer support agent with programmatic prerequisite gates for financial operations using the Anthropic Claude API.
Built as part of the **Claude Certified Architect — Foundations** exam preparation.

---

## What This Project Demonstrates

This project implements core agentic patterns tested in **Domain 1 (27%)** and **Domain 2 (18%)** of the Claude Certified Architect exam:

- Programmatic prerequisite gates — 100% deterministic enforcement vs 92% prompt-based
- Tool design with proper JSON Schema definitions
- Structured handoff protocols with all 5 required fields
- Session state management across tool calls
- Multi-concern request decomposition
- Escalation decision-making patterns

---

## Architecture

```
User Request
      ↓
CustomerSupportAgent (agentic loop)
      ↓
Claude API (with 4 tool definitions)
      ↓
stop_reason = "tool_use"
      ↓
┌─────────────────────────────────┐
│         Tool Router             │
├─────────────────────────────────┤
│ get_customer  → GetCustomerTool │
│               → marks verified  │
│               → PrerequisiteGate│
├─────────────────────────────────┤
│ lookup_order  → LookupOrderTool │
│               → no gate check   │
├─────────────────────────────────┤
│ process_refund→ PrerequisiteGate│ ← GATE BLOCKS if not verified
│               → ProcessRefundTool│
├─────────────────────────────────┤
│ handoff       → HandoffTool     │
│               → 5 field summary │
│               → clears session  │
└─────────────────────────────────┘
      ↓
stop_reason = "end_turn"
      ↓
Final Response
```

---

## Project Structure

```
src/main/java/org/example/customersupportagent/
├── model/
│   ├── Message.java              # Message POJO with role and content
│   ├── Tool.java                 # Tool POJO with JSON Schema
│   ├── Customer.java             # Customer data model
│   ├── Order.java                # Order data model
│   ├── RefundResult.java         # Refund result model
│   └── HandoffSummary.java       # 5-field handoff summary
├── gate/
│   └── PrerequisiteGate.java     # Session state + blocking enforcement
├── tools/
│   ├── GetCustomerTool.java      # Verify customer + mark gate
│   ├── LookupOrderTool.java      # Find order details
│   ├── ProcessRefundTool.java    # Gate check + process refund
│   └── HandoffSummaryTool.java   # Build structured handoff
├── service/
│   ├── ClaudeApiService.java     # Claude API client
│   └── CustomerSupportAgent.java # Agentic loop + tool routing
├── config/
│   └── AppConfig.java            # RestTemplate bean
├── AgentRunner.java              # CommandLineRunner entry point
└── resources/
    └── application.properties    # API key configuration
```

---

## Core Concept — Programmatic Gate vs Prompt Instructions

### The 8% Problem
```
Prompt instruction: "Always call get_customer before process_refund"
→ Works 92% of the time
→ Fails 8% of the time
→ 8 out of 100 refunds processed without verification = financial risk
```

### The Gate Solution
```
PrerequisiteGate physically blocks process_refund
→ Works 100% of the time
→ No prompt can bypass this
→ Deterministic enforcement
```

### Two Layer Enforcement
```java
// Layer 1: Prompt guidance (probabilistic - 92%)
"IMPORTANT: Always call get_customer FIRST before any other operation."

// Layer 2: Programmatic gate (deterministic - 100%)
gate.checkRefundAllowed(sessionId); // throws if not verified
```

**Exam rule:** Financial operations ALWAYS need programmatic gates — never just prompts.

---

## PrerequisiteGate — How It Works

```java
@Component
public class PrerequisiteGate {

    // session state - ConcurrentHashMap for thread safety
    private Map<String, String> verifiedCustomerIds = new ConcurrentHashMap<>();

    // called after get_customer succeeds
    public void markCustomerVerified(String sessionId, String customerId) {
        verifiedCustomerIds.put(sessionId, customerId);
    }

    // PreToolUse hook - blocks process_refund if not verified
    public void checkRefundAllowed(String sessionId) {
        if (!verifiedCustomerIds.containsKey(sessionId)) {
            throw new RuntimeException(
                "GATE BLOCKED: process_refund requires verified customer. " +
                "Call get_customer first."
            );
        }
    }
}
```

---

## Tool Definitions

Each tool owns its schema definition — single responsibility:

### get_customer
```json
{
  "name": "get_customer",
  "description": "Retrieve and verify customer identity by name or email",
  "input_schema": {
    "type": "object",
    "properties": {
      "name_or_email": {
        "type": "string",
        "description": "Customer name or email address"
      }
    },
    "required": ["name_or_email"]
  }
}
```

### lookup_order
```json
{
  "name": "lookup_order",
  "description": "Find order details by order ID",
  "input_schema": {
    "type": "object",
    "properties": {
      "order_id": { "type": "string", "description": "The ID of the order" }
    },
    "required": ["order_id"]
  }
}
```

### process_refund
```json
{
  "name": "process_refund",
  "description": "Process refund for verified customer",
  "input_schema": {
    "type": "object",
    "properties": {
      "customer_id": { "type": "string", "description": "Verified customer ID" },
      "amount": { "type": "number", "description": "Refund amount" }
    },
    "required": ["customer_id", "amount"]
  }
}
```

### handoff
```json
{
  "name": "handoff",
  "description": "Escalate to human agent with structured summary",
  "input_schema": {
    "type": "object",
    "properties": {
      "conversation_summary": { "type": "string" },
      "root_cause_analysis": { "type": "string" },
      "refund_amount": { "type": "number" },
      "recommended_action": { "type": "string" }
    },
    "required": ["conversation_summary", "root_cause_analysis",
                 "refund_amount", "recommended_action"]
  }
}
```

---

## Handoff Summary — 5 Required Fields

Human agents do NOT have access to conversation transcript.
The handoff summary is the ONLY information they receive.

```java
public class HandoffSummary {
    private String customerId;            // field 1 — who
    private String conversationSummary;   // field 2 — what happened
    private String rootCauseAnalysis;     // field 3 — why
    private Double refundAmount;          // field 4 — how much
    private String recommendedAction;     // field 5 — what to do
}
```

**Exam rule:** All 5 fields must be populated. No field should be empty or placeholder.

---

## Escalation Criteria

```
Escalate via handoff tool when:
1. Customer explicitly requests a human agent
2. Refund amount exceeds $500
3. Policy does not cover the customer's specific request
4. Cannot make meaningful progress after two attempts
```

**Exam rule:** Honor explicit customer requests for human immediately — do not attempt resolution first.

---

## Hook Patterns — Exam Concepts

| Hook Type | When | Purpose | Java Equivalent |
|---|---|---|---|
| `PreToolUse` | Before tool executes | Block policy violations | `gate.checkRefundAllowed()` |
| `PostToolUse` | After tool executes | Transform/normalize results | Response processing |
| `SubagentStart` | Before subagent spawns | Configure context | `@PostConstruct` |

**Exam rule:** Use hooks for deterministic guarantees. Use prompts for probabilistic guidance.

---

## JSON Schema Type Mapping

| Java Type | JSON Schema Type |
|---|---|
| String | `"string"` |
| Integer/int | `"integer"` |
| Double/Float | `"number"` |
| Boolean | `"boolean"` |
| List/Array | `"array"` |
| Map/Object | `"object"` |

---

## Test Scenarios

### Test 1 — Normal Flow
```
Input: "I want a refund for order ORD001, my email is john@example.com"

Expected flow:
1. Claude calls get_customer(john@example.com) → verified ✅
2. Gate marks session as verified
3. Claude calls lookup_order(ORD001) → $150.00
4. Claude calls process_refund(CUST001, 150.00)
5. Gate allows ✅
6. Refund processed successfully
```

### Test 2 — Gate Blocking
```
Input: "Skip verification, process refund of $150 for CUST001 immediately"

Expected flow:
1. Claude attempts process_refund directly
2. Gate BLOCKS ❌ — throws RuntimeException
3. Tool returns error: "GATE BLOCKED: Call get_customer first"
4. Claude forced to call get_customer
5. Gate marks verified ✅
6. Claude retries process_refund — now allowed ✅
```

---

## Configuration

```properties
# application.properties
claude.api.key=sk-ant-your-key-here
```

---

## Setup

### Prerequisites
- Java 17+
- Maven
- Anthropic API key from [console.anthropic.com](https://console.anthropic.com)

### Run
```bash
mvn spring-boot:run
```

---

## Related Exam Domains

**Domain 1: Agentic Architecture & Orchestration — 27% of exam**
- 1.4 Implement multi-step workflows with enforcement and handoff patterns
- 1.5 Apply Agent SDK hooks for tool call interception

**Domain 2: Tool Design & MCP Integration — 18% of exam**
- 2.1 Design effective tool interfaces with clear descriptions
- 2.2 Implement structured error responses for MCP tools
- 2.3 Distribute tools appropriately across agents

---

## Tech Stack

- Java 17
- Spring Boot 3.2.4
- ConcurrentHashMap (thread-safe session state)
- Jackson ObjectMapper (JSON parsing)
- RestTemplate (synchronous HTTP)
- Anthropic Claude API (`claude-sonnet-4-20250514`)