package com.github.phntom.anton;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;

public class AntonProjectComponent implements ProjectComponent {
    private final Project myProject;

    protected AntonProjectComponent(Project project) {
        myProject = project;
    }

    @Override
    public void projectOpened() {
        myProject.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new AntonModuleListener());

        AntonService.getInstance(myProject).checkNow();
    }
}
