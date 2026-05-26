package org.square;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class EnvParser {
    public EnvParser() {

    }
    public Map<String, String> getEnvVars() throws IOException {
        Path envPath = Paths.get(".env");
        Map<String, String> envVars = Files.readAllLines(envPath)
                .stream()
                .map(line -> line.split("="))
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

        return envVars;
    }
}
