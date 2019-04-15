package com.github.phntom.anton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class Utils {
    private Utils() {
    }

    @NotNull
    public static String RunAndCollectOutput(@NotNull String[] cmdArr, @Nullable String[] env, @Nullable File dir) {
        try {
            Runtime rt = Runtime.getRuntime();
            java.util.Scanner s = new java.util.Scanner(rt.exec(cmdArr, env, dir).getInputStream()).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            return "";
        }
    }

}
