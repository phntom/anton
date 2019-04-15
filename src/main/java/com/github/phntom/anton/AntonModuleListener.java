package com.github.phntom.anton;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

class AntonChangesListener implements ModuleListener {
    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        @SystemIndependent String basePath = project.getBasePath();
        if (basePath != null) {
            GitSubmodulesHelper.gitSubmoduleHelper(basePath);
        }
    }
}