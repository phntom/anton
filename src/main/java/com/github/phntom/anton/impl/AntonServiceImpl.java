package com.github.phntom.anton.impl;

import com.github.phntom.anton.AntonService;
import com.github.phntom.anton.GitSubmoduleBackgroundTask;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.concurrent.atomic.AtomicBoolean;

public class AntonServiceImpl implements AntonService {
    private final Project project;
    private AtomicBoolean running = new AtomicBoolean(false);

    public AntonServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public synchronized void checkNow() {
        running.set(true);

        ProgressManager.getInstance().run(new GitSubmoduleBackgroundTask(project, running));
    }
}
