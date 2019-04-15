package com.github.phntom.anton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import static com.github.phntom.anton.Utils.RunAndCollectOutput;

public class AntonAction extends AnAction {
    public AntonAction() {
        super("Anton");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        String[] gitEmailCmd = {"/usr/bin/env", "git", "config", "--get", "user.email"};
        String email = RunAndCollectOutput(gitEmailCmd, null, null);
        Messages.showMessageDialog(project, "Hey! Your email is "+ email,
                "Anton's IntelliJ Plugin", Messages.getInformationIcon());
    }
}
