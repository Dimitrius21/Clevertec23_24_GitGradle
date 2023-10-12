package tagplagin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class Util {
    public static int[] getLastVersion(Stream<String> strings) {
        Map<Integer, List<Integer>> versions = new HashMap<>();
        strings.forEach(str -> parseString(versions, str));
        if (versions.isEmpty()) {
            return new int[]{0, 0};
        }
        int majorMax = Collections.max(versions.keySet());
        int minorMax = Collections.max(versions.get(majorMax));
        return new int[]{majorMax, minorMax};
    }

    private static void parseString(Map<Integer, List<Integer>> version, String str) {
        String[] parts = str.split("\\.");
        Integer major = Integer.parseInt(parts[0].substring(1));
        Integer minor = Integer.parseInt(parts[1].split("-")[0]);
        if (!version.containsKey(major)) {
            version.put(major, new ArrayList<>());
        }
        version.get(major).add(minor);
    }

    public static BufferedReader executeShellCommand(String command) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec(command);
        } else {
            process = Runtime.getRuntime()
                    .exec(String.format("/bin/sh -c %s", command));
        }
        int exitCode = process.waitFor();
        return process.inputReader();
    }
}
