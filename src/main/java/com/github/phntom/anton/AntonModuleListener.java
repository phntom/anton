package com.github.phntom.anton;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

class AntonModuleListener implements ModuleListener {
    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        Messages.showMessageDialog(project, "New Module", "Yay", Messages.getInformationIcon());
    }
}