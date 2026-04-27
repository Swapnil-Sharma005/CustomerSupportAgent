package org.example.customersupportagent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Tool {

    private String name;
    private String description;

    @JsonProperty("input_schema")
    private Map<String, Object> inputSchema;

    public Tool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getInputSchema() { return inputSchema; }
}