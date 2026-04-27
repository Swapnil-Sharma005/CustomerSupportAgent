package org.example.customersupportagent.model;

import java.util.List;

public class Customer{
    private String name;
    private String customerId;
    private String email;
    private boolean verified;
    private List<Order> orders;

    public Customer(String name, String id, String email, boolean verified) {
        this.name = name;
        customerId = id;
        this.email = email;
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
