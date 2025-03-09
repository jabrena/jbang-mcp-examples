package src.main.java;
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.17.6@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0.Alpha2
//DEPS io.quarkus:quarkus-qute
//FILES application.properties

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class MCPCalculator {

    @Tool(description = "Performs basic arithmetic operations (add, subtract, multiply, divide)")
    String operation(@ToolArg(description = "The operation to perform - add, subtract, multiply, divide") String operation, 
                    @ToolArg(description = "The first operand") double a, 
                    @ToolArg(description = "The second operand") double b) {
                        
        return switch (operation.toLowerCase()) {
            case "add" -> String.valueOf(a + b);
            case "subtract" -> String.valueOf(a - b);
            case "multiply" -> String.valueOf(a * b);
            case "divide" -> {
                if (b == 0) {
                    yield "Division by zero is not allowed!";
                }
                yield String.valueOf(a / b);
            }
            default -> "Unknown operation!";
        };
    }
}
