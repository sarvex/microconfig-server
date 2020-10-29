package io.microconfig.cli;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

@RequiredArgsConstructor
public class CliFlags {
    private final String[] args;

    public Optional<String> env() {
        return findFlag("-e", "--env");
    }

    public Optional<String> dir() {
        return findFlag("-d", "--dir");
    }

    public Map<String, String> vars() {
        return findVars("-s", "--set");
    }

    public Optional<String> branch() {
        return findFlag("--branch");
    }

    public Optional<String> tag() {
        return findFlag("--tag");
    }

    public int timeout() {
        return findFlag("--timeout").map(Integer::parseInt).orElse(10);
    }

    public Optional<String> server() {
        return findFlag("--server");
    }

    public boolean skipTls() {
        return asList(args).contains("--skip-tls");
    }

    public Optional<String> rootCa() {
        return findFlag("--tls-root-ca");
    }

    public Optional<String> type() {
        return findFlag("-t", "--type");
    }

    public Optional<String> findFlag(String... flag) {
        var flags = asList(flag);
        for (int i = 2; i < args.length; i++) {
            if (flags.contains(args[i])) {
                if (i == args.length - 1) throw new CliException(args[i] + " value not provided", 4);
                return Optional.of(args[i + 1]);
            }
        }
        return empty();
    }

    public Map<String, String> findVars(String... flag) {
        var flags = asList(flag);
        return range(0, args.length)
                .filter(i -> flags.contains(args[i]))
                .peek(this::validate)
                .mapToObj(i -> args[i + 1])
                .map(a -> splitFirst(a, '='))
                .collect(toMap(a -> a[0], a -> a[1]));
    }

    private void validate(int i) {
        if (i == args.length - 1) throw new CliException("--set doesn't have a value", 4);
        if (args[i + 1].startsWith("-")) throw new CliException("--set doesn't have a value", 4);
        if (!args[i + 1].contains("=")) throw new CliException("--set value should contain '=' " + args[i + 1], 4);
    }

    private String[] splitFirst(String str, char c) {
        int idx = str.indexOf(c);
        if (idx == str.length() - 1) throw new CliException("No value after '=' in " + str, 4);
        return new String[]{str.substring(0, idx), str.substring(idx + 1)};
    }
}
