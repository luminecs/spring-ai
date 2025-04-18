package org.springframework.ai.mcp.client.autoconfigure.properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.ServerParameters;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(McpStdioClientProperties.CONFIG_PREFIX)
public class McpStdioClientProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mcp.client.stdio";

	private Resource serversConfiguration;

	private final Map<String, Parameters> connections = new HashMap<>();

	public Resource getServersConfiguration() {
		return this.serversConfiguration;
	}

	public void setServersConfiguration(Resource stdioConnectionResources) {
		this.serversConfiguration = stdioConnectionResources;
	}

	public Map<String, Parameters> getConnections() {
		return this.connections;
	}

	private Map<String, ServerParameters> resourceToServerParameters() {
		try {
			Map<String, Map<String, Parameters>> stdioConnection = new ObjectMapper().readValue(
					this.serversConfiguration.getInputStream(),
					new TypeReference<Map<String, Map<String, Parameters>>>() {
					});

			Map<String, Parameters> mcpServerJsonConfig = stdioConnection.entrySet().iterator().next().getValue();

			return mcpServerJsonConfig.entrySet().stream().collect(Collectors.toMap(kv -> kv.getKey(), kv -> {
				Parameters parameters = kv.getValue();
				return ServerParameters.builder(parameters.command())
					.args(parameters.args())
					.env(parameters.env())
					.build();
			}));
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to read stdio connection resource", e);
		}
	}

	public Map<String, ServerParameters> toServerParameters() {
		Map<String, ServerParameters> serverParameters = new HashMap<>();
		if (this.serversConfiguration != null) {
			serverParameters.putAll(resourceToServerParameters());
		}

		for (Map.Entry<String, Parameters> entry : this.connections.entrySet()) {
			serverParameters.put(entry.getKey(), entry.getValue().toServerParameters());
		}
		return serverParameters;
	}

	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	public record Parameters(

			@JsonProperty("command") String command,

			@JsonProperty("args") List<String> args,

			@JsonProperty("env") Map<String, String> env) {

		public ServerParameters toServerParameters() {
			return ServerParameters.builder(this.command()).args(this.args()).env(this.env()).build();
		}

	}

}
