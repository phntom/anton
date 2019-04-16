package com.github.phntom.anton;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface AntonService {
    static AntonService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, AntonService.class);
    }

    void checkNow();
}
