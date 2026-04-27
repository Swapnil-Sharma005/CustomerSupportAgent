package org.example.customersupportagent.tools;

import jakarta.annotation.PostConstruct;
import org.example.customersupportagent.gate.PrerequisiteGate;
import org.example.customersupportagent.model.Customer;
import org.example.customersupportagent.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetCustomerTool {

    @Autowired
    private PrerequisiteGate gate;
    Map<String, Customer> customers = new HashMap<>();

    //mock data for now
    @PostConstruct
    public void init() {
        customers.put("john@example.com", new Customer(
                "CUST001", "John Doe", "john@example.com", true));
        customers.put("jane@example.com", new Customer(
                "CUST002", "Jane Smith", "jane@example.com", true));
        customers.put("John Doe", new Customer(
                "CUST001", "John Doe", "john@example.com", true));
    }

    // schema lives here — tool owns its definition
    public static Tool getToolDefinition() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> nameOrEmailProp = new HashMap<>();
        nameOrEmailProp.put("type", "string");
        nameOrEmailProp.put("description", "Customer name or email address");
        properties.put("name_or_email", nameOrEmailProp);

        schema.put("properties", properties);
        schema.put("required", List.of("name_or_email"));

        return new Tool("get_customer",
                "Retrieve and verify customer identity by name or email",
                schema);
    }

    public Customer execute(String sessionId, String nameOrEmail) {

        Customer customer = customers.get(nameOrEmail);
        if(customer==null){
            throw new RuntimeException(
                    "Customer not found :" + nameOrEmail
            );
        }
        gate.markCustomerVerified(sessionId, customer.getCustomerId());
        return customer;
    }
}