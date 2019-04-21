package com.github.phntom.anton;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.phntom.anton.Utils.RunAndCollectOutput;

public class GitSubmoduleBackgroundTask extends Task.Backgroundable {
    private static final Pattern submodulePattern = Pattern.compile("\\[submodule[ \\t]+\"(.+)\"][ \\t]*$");
    private static final Pattern pathPattern = Pattern.compile("[ \\t]*path[ \\t]*=[ \\t]*([^ \\t]+)[ \\t]*");
    private static final Pattern branchPattern = Pattern.compile("[ \\t]*branch[ \\t]*=[ \\t]*([^ \\t]+)[ \\t]*");
    private static final Pattern gitDirPattern = Pattern.compile("[ \\t]*gitdir[ \\t]*:[ \\t]*([^ \\t]+)[ \\t]*");
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

        boolean reattachSubmodules = false;

        try (Stream<String> stream = Files.lines(gitModulesPath)) {
            List<String> paths = stream
                    .map(GitSubmoduleBackgroundTask::lineToPath)
                    .filter(Objects::nonNull)
                    .filter(path -> isUninitializedGitPath(path, projectDir))
                    .collect(Collectors.toList());

            if (!paths.isEmpty()) {
                indicator.setText("git submodule update --init --recursive");

                String[] gitSubmoduleInit = {"git", "submodule", "update", "--init", "--recursive", "--progress"};

                Utils.RunWithGitProgress(gitSubmoduleInit, null, Paths.get(projectDir).toFile(), 0.1, 0.95, indicator);

                reattachSubmodules = true;
            }

        } catch (IOException ignored) {
        }

        indicator.setFraction(0.95);

        if (reattachSubmodules || Paths.get(projectDir, ".anton").toFile().exists()) {

            reattachSubmodules(indicator, projectDir, gitModulesPath);

        }

        indicator.setFraction(1);
        return true;
    }

    private static void reattachSubmodules(@NotNull ProgressIndicator indicator, @SystemIndependent String projectDir, Path gitModulesPath) {
        final Map<String, String> submoduleBranch = new HashMap<>();
        final Map<String, String> submodulePath = new HashMap<>();
        detectBranchPath(gitModulesPath, submoduleBranch, submodulePath);

        indicator.setFraction(0.96);
        indicator.setText("Reattaching submodules");
        for (Map.Entry<String, String> entry: submodulePath.entrySet()) {
            final String submodule = entry.getKey();
            indicator.setText2(submodule);
            final Path gitPath = Paths.get(projectDir, entry.getValue(), ".git");
            if (gitPath.toFile().exists()) {
                try (Stream<String> stream = Files.lines(gitPath)) {
                    Optional<String> gitDirPath = stream.map(line -> {
                        Matcher gitDirMatcher = gitDirPattern.matcher(line);
                        if (gitDirMatcher.matches()) {
                            return gitDirMatcher.group(1);
                        } else {
                            return null;
                        }
                    }).filter(Objects::nonNull).findAny();

                    if (gitDirPath.isPresent()) {
                        checkoutIfDetached(projectDir, submoduleBranch, entry, submodule, gitDirPath.get());
                    }

                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void checkoutIfDetached(@SystemIndependent String projectDir, Map<String, String> submoduleBranch, Map.Entry<String, String> entry, String submodule, String gitDirPath) throws IOException {
        final Path actualGitPath = Paths.get(projectDir, entry.getValue(), gitDirPath, "HEAD");
        if (actualGitPath.toFile().exists()) {
            String gitContent = new String(Files.readAllBytes(actualGitPath), StandardCharsets.UTF_8);
            if (!gitContent.startsWith("ref: ")) {
                final String branchName = submoduleBranch.get(submodule);
                final String[] gitCheckoutCmd = {"git", "checkout", "-q", "--merge", branchName};
                final File workDir = Paths.get(projectDir, entry.getValue()).toFile();
                RunAndCollectOutput(gitCheckoutCmd, null, workDir);
            }
        }
    }

    private static void detectBranchPath(Path gitModulesPath, Map<String, String> submoduleBranch, Map<String, String> submodulePath) {
        final String[] currentModule = {null};
        try (Stream<String> stream = Files.lines(gitModulesPath)) {
            stream.forEachOrdered(line -> {
                {
                    Matcher submoduleMatcher = submodulePattern.matcher(line);
                    if (submoduleMatcher.matches()) {
                        currentModule[0] = submoduleMatcher.group(1);
                        submoduleBranch.putIfAbsent(currentModule[0], "master");
                        submodulePath.putIfAbsent(currentModule[0], currentModule[0]);
                    }
                }
                {
                    Matcher branchMatcher = branchPattern.matcher(line);
                    if (branchMatcher.matches()) {
                        submoduleBranch.putIfAbsent(currentModule[0], branchMatcher.group(1));
                    }
                }
                {
                    Matcher pathMatcher = pathPattern.matcher(line);
                    if (pathMatcher.matches()) {
                        submodulePath.putIfAbsent(currentModule[0], pathMatcher.group(1));
                    }
                }
            });
        }
             catch (IOException ignored) {
        }
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
