package io.microconfig.server.vault;

import com.fasterxml.jackson.databind.JsonNode;
import io.microconfig.server.vault.exceptions.VaultAuthException;
import io.microconfig.server.vault.exceptions.VaultSecretNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

import static io.microconfig.server.util.HttpUtil.httpSend;
import static io.microconfig.server.util.JsonUtil.parseJson;

@Slf4j
@RequiredArgsConstructor
@Service
public class VaultClientImpl implements VaultClient {
    private final VaultConfig config;

    @Override
    public String fetchKV(String token, String property) {
        int dotIndex = property.lastIndexOf('.');
        String path = property.substring(0, dotIndex);
        String key = property.substring(dotIndex + 1);
        log.debug("Fetching {} {}", path, key);

        var node = readPath(path, token);
        var value = node.path("data").path("data").get(key);
        if (value == null) throw new VaultSecretNotFound(property);
        return value.asText();
    }

    private JsonNode readPath(String path, String token) {
        var splitPath = splitPath(path);
        var request = HttpRequest.newBuilder(URI.create(config.getAddress() + "/v1/" + splitPath[0] + "/data" + splitPath[1]))
            .setHeader("X-Vault-Token", token)
            .timeout(Duration.ofSeconds(2))
            .build();
        var response = httpSend(request);
        var node = parseJson(response.body());
        if (node.get("errors") != null) {
            throw new VaultAuthException();
        }
        return node;
    }

    private String[] splitPath(String path) {
        int slash = path.indexOf('/');
        return new String[]{path.substring(0, slash), path.substring(slash)};
    }
}
