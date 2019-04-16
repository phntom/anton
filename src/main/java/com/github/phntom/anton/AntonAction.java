package com.github.phntom.anton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import static com.github.phntom.anton.Utils.RunAndCollectOutput;

public class AntonAction extends AnAction {
    public AntonAction() {
        super("Call Anton!");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            AntonService.getInstance(project).checkNow();
        }

        String[] gitEmailCmd = {"git", "config", "--get", "user.email"};
        String email = RunAndCollectOutput(gitEmailCmd, null, null);
        Messages.showMessageDialog(project,
                String.format("Hey %s, Anton will implement this feature in the future :)", email),
                "Anton's IntelliJ Plugin", Messages.getInformationIcon());
    }
}
