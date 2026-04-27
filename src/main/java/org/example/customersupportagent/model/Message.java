package org.example.customersupportagent.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    private String role;
    private Object content; // String or List for tool results

    public Message(String role, Object content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() { return role; }
    public Object getContent() { return content; }
}