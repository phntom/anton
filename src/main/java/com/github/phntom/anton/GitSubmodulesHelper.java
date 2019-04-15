package com.github.phntom.anton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GitSubmodulesHelper {
    private GitSubmodulesHelper() {
    }

    private static final Pattern pathPattern = Pattern.compile("[ \\t]*path[ \\t]*=[ \\t]*([^ \\t]+)[ \\t]*");

    public static void gitSubmoduleHelper(@NotNull @SystemIndependent final String projectDir) {
        Path gitModulesPath = Paths.get(projectDir, ".gitmodules");
        if (!gitModulesPath.toFile().exists()) {
            return;
        }

        try (Stream<String> stream = Files.lines(gitModulesPath)) {
            boolean allInitialized = stream
                    .map(GitSubmodulesHelper::lineToPath)
                    .filter(Objects::nonNull)
                    .allMatch(path -> checkPathGitInitialized(path, projectDir));

            if (allInitialized) {
                return;
            }
        } catch (IOException ignored) {
            return;
        }

        String[] gitSubmoduleInit = {"/usr/bin/env", "git", "submodule", "update", "--init", "--recursive"};

        Utils.RunAndCollectOutput(gitSubmoduleInit, null, Paths.get(projectDir).toFile());
    }

    private static String lineToPath(String line) {
        Matcher matcher = pathPattern.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean checkPathGitInitialized(String path, String projectDir) {
        return Paths.get(projectDir, path, ".git").toFile().exists();
    }
}
