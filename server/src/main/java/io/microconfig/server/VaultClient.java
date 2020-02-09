package io.microconfig.server;

import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultClient {
    private final VaultConfig config;

    public String fetch(VaultCredentials credentials, String placeholder) {
        var dot = placeholder.lastIndexOf('.');
        var path = placeholder.substring(0, dot);
        var key = placeholder.substring(dot + 1);
        log.debug("Fetching {} {}", path, key);
        try {
            var vault = credentials.insert(config);
            return vault.logical().read(path).getData().get(key);
        } catch (VaultException e) {
            throw new RuntimeException(e);
        }
    }
}
