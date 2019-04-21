package com.github.phntom.anton;

import com.github.phntom.anton.vendored.apache.lang3.StringUtils;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {
    private static Pattern cloneStatement = Pattern.compile("\\W*(?:Cloning into '([^ ]+)'|Submodule '([^ ]+)' [^ ]+ registered for path ).*");
    private static Pattern remoteLineCount = Pattern.compile("\\W*remote: Counting objects: +([0-9]{1,3})% .*");
    private static Pattern remoteLineCompress = Pattern.compile("\\W*remote: Compressing objects: +([0-9]{1,3})% .*");
    private static Pattern receivingObjects = Pattern.compile("\\W*Receiving objects: +([0-9]{1,3})% .*");
    private static Pattern resolvingDeltas = Pattern.compile("\\W*Resolving deltas: +([0-9]{1,3})% .*");

    private Utils() {
    }

    @NotNull
    public static String RunAndCollectOutput(@NotNull String[] cmdArr, @Nullable String[] env, @Nullable File dir) {
        try {
            Runtime rt = Runtime.getRuntime();
            java.util.Scanner s = new java.util.Scanner(rt.exec(cmdArr, env, dir).getInputStream()).useDelimiter("\\A");
            return s.hasNext() ? StringUtils.trim(s.next()) : "";
        } catch (IOException e) {
            return "";
        }
    }

    public static void RunWithGitProgress(@NotNull String[] cmdArr, @Nullable String[] env, @Nullable File dir,
                                          double progressMin, double progressMax, ProgressIndicator indicator) {
        try {
            Runtime rt = Runtime.getRuntime();
            java.util.Scanner scanner = new java.util.Scanner(rt.exec(cmdArr, env, dir).getErrorStream()).useDelimiter("[\n\r]");
            Set<String> cloneRepositories = new HashSet<>();
            int clonesComplete = 0;
            double completeFraction;
            double progressWithinClone = 0;
            boolean wasResolvingDeltas = false;

            while (scanner.hasNext()) {
                int numberOfClones = Math.max(1, cloneRepositories.size());
                completeFraction = progressMin + ((clonesComplete + progressWithinClone) / numberOfClones) * (progressMax - progressMin);
                indicator.setFraction(completeFraction);

                String line = scanner.next();

                {
                    Matcher cloneStatementMatcher = cloneStatement.matcher(line);
                    if (cloneStatementMatcher.matches()) {
                        String target = StringUtils.defaultIfEmpty(cloneStatementMatcher.group(1), cloneStatementMatcher.group(2));
                        if (StringUtils.contains(target, "/")) {
                            target = StringUtils.substringAfterLast(target, "/");
                        }

                        indicator.setText2(target);

                        cloneRepositories.add(target);
                        if (wasResolvingDeltas) {
                            clonesComplete++;
                            progressWithinClone = 0;
                            wasResolvingDeltas = false;
                        }
                        continue;
                    }
                }

                {
                    Matcher remoteLineCountMatcher = remoteLineCount.matcher(line);
                    if (remoteLineCountMatcher.matches()) {
                        int percent = Integer.parseInt(remoteLineCountMatcher.group(1));
                        progressWithinClone = 0.04 + (percent * 0.0013);
                        continue;
                    }
                }

                {
                    Matcher remoteLineCompressMatcher = remoteLineCompress.matcher(line);
                    if (remoteLineCompressMatcher.matches()) {
                        int percent = Integer.parseInt(remoteLineCompressMatcher.group(1));
                        progressWithinClone = 0.17 + (percent * 0.0013);
                        continue;
                    }
                }

                {
                    Matcher receivingObjectsMatcher = receivingObjects.matcher(line);
                    if (receivingObjectsMatcher.matches()) {
                        int percent = Integer.parseInt(receivingObjectsMatcher.group(1));
                        progressWithinClone = 0.3 + (percent * 0.004);
                        continue;
                    }
                }

                {
                    Matcher resolvingDeltasMatcher = resolvingDeltas.matcher(line);
                    if (resolvingDeltasMatcher.matches()) {
                        int percent = Integer.parseInt(resolvingDeltasMatcher.group(1));
                        progressWithinClone = 0.7 + (percent * 0.0028);

                        if (percent == 100) {
                            wasResolvingDeltas = true;
                        }
                    }
                }
            }

            completeFraction = progressMax;
            indicator.setFraction(completeFraction);

        } catch (IOException ignored) {
        }
    }

//    public static void main(String[] args) {
////        String[] gitSubmoduleInit = {"git", "submodule", "update", "--init", "--recursive", "--progress"};
////        String[] gitSubmoduleInit = {"git", "clone", "git@gitlab.oracledatacloud.com:audplat/audplat.git", "--recursive", "--progress"};
////        RunWithGitProgress(gitSubmoduleInit, null, Paths.get("/Users/anton.wolkov/development/private").toFile(), 0, 1);
//        //null, 0, 0
//    }

}
