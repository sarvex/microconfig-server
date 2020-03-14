package io.microconfig.server.vault.credentials;

import io.microconfig.server.vault.VaultConfig;
import io.microconfig.server.vault.exceptions.VaultAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

import static io.microconfig.server.util.HttpUtil.httpSend;
import static io.microconfig.server.util.JsonUtil.objectNode;
import static io.microconfig.server.util.JsonUtil.parseJson;

@RequiredArgsConstructor
@Slf4j
public class KubernetesTokenCredentials implements VaultCredentials {
    private final String jwtToken;
    private String token;

    @Override
    public String getToken(VaultConfig config) {
        if (token != null) return token;
        var request = request(config);
        var response = httpSend(request);
        if (response.statusCode() != 200) throw new VaultAuthException();

        log.debug("Fetched token with k8sJWT");
        token = parseJson(response.body()).path("auth").path("client_token").asText();
        return token;
    }
    private HttpRequest request(VaultConfig config) {
        var body = objectNode().put("role", config.getKubeRole()).put("jwt", jwtToken).toString();
        return HttpRequest.newBuilder(URI.create(String.format("%s/v1/auth/%s/login", config.getAddress(), config.getKubePath())))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(2))
            .build();
    }
}