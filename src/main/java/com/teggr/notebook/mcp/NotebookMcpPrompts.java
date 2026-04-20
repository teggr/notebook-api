package com.teggr.notebook.mcp;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

@Component
public class NotebookMcpPrompts {

    @McpPrompt(
        name = "Search notes",
        description = "Search for notes using the Notebook MCP tool")
    public GetPromptResult searchPrompt() {

    var message = "Search my notes using the query ";

        return new GetPromptResult(
            "Search",
            List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message)))
        );
    }

}
