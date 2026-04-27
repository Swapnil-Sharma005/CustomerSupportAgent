package org.example.customersupportagent.tools;

import jakarta.annotation.PostConstruct;
import org.example.customersupportagent.model.Order;
import org.example.customersupportagent.model.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LookupOrderTool{

    Map<String, Order> orders = new HashMap<>();
    //mock data for now
    @PostConstruct
    public void init(){
        orders.put("ORD001", new Order("ORD001", "CUST001", 150.00, "DELIVERED", "iPhone Case"));
        orders.put("ORD002", new Order("ORD002", "CUST002", 299.99, "PENDING", "Laptop Stand"));
        orders.put("ORD003", new Order("ORD003", "CUST001", 49.99, "DELIVERED", "USB Cable"));
    }

    public static Tool getToolDefinition(){
        Map<String,Object> schema = new HashMap<>();
        schema.put("type","object");

        Map<String,Object> properties = new HashMap<>();

        Map<String,Object> orderId = new HashMap<>();
        orderId.put("type","string");
        orderId.put("description","The id of the Order");
        properties.put("order_id",orderId);

        schema.put("properties",properties);
        schema.put("required", List.of("order_id"));
        return new Tool("lookup_order","Find the order",schema);
    }

    public Order execute(String orderId){
        Order order = orders.get(orderId);
        if(order==null){
            throw new RuntimeException("Order not found:" + orderId);
        }
        return order;
    }
}