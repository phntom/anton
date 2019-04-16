package com.github.phntom.anton;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitSubmoduleBackgroundTask extends Task.Backgroundable {
    private static final Pattern pathPattern = Pattern.compile("[ \\t]*path[ \\t]*=[ \\t]*([^ \\t]+)[ \\t]*");
    private final Project project;
    private final AtomicBoolean runningIndicator;

    public GitSubmoduleBackgroundTask(@Nullable Project project, AtomicBoolean running) {
        super(project, "Git Submodule Helper", true);
        this.project = project;
        runningIndicator = running;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        if (project == null) {
            return;
        }

        if (checkGitSubmodule(project, indicator)) {
            //noinspection deprecation
            FileBasedIndex.getInstance().requestReindex(project.getBaseDir());
        }

        runningIndicator.set(false);
    }

    private boolean checkGitSubmodule(@NotNull Project project, @NotNull ProgressIndicator indicator) {
        indicator.setText("checking for missing git repositories");
        indicator.setFraction(0.0);

        @SystemIndependent String projectDir = project.getBasePath();
        if (projectDir == null || projectDir.isEmpty()) {
            return false;
        }

        Path gitModulesPath = Paths.get(projectDir, ".gitmodules");
        if (!gitModulesPath.toFile().exists()) {
            return false;
        }

        indicator.setText("checking for missing git submodules");
        indicator.setFraction(0.05);

        try (Stream<String> stream = Files.lines(gitModulesPath)) {
            List<String> paths = stream
                    .map(GitSubmoduleBackgroundTask::lineToPath)
                    .filter(Objects::nonNull)
                    .filter(path -> isUninitializedGitPath(path, projectDir))
                    .collect(Collectors.toList());

            if (paths.isEmpty()) {
                return false;
            }

            indicator.setText("git submodule update --init --recursive");

            String[] gitSubmoduleInit = {"git", "submodule", "update", "--init", "--recursive", "--progress"};

            Utils.RunWithGitProgress(gitSubmoduleInit, null, Paths.get(projectDir).toFile(), 0.1, 1.0, indicator);

        } catch (IOException ignored) {
        }

        indicator.setFraction(1);
        return true;
    }

    @Nullable
    private static String lineToPath(@NotNull String line) {
        Matcher matcher = pathPattern.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean isUninitializedGitPath(@NotNull String path, @NotNull String projectDir) {
        return !Paths.get(projectDir, path, ".git").toFile().exists();
    }
}
