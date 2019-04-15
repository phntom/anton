package com.github.phntom.anton;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.impl.ModuleImpl;
import com.intellij.openapi.module.impl.scopes.ModuleScopeProviderImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import org.jetbrains.annotations.NotNull;


public class AntonModuleComponent extends ModuleImpl {
    AntonModuleComponent(@NotNull String name, @NotNull Project project, @NotNull String filePath) {
        super(project, "Module " + name);

        getPicoContainer().registerComponentInstance(Module.class, this);

        myProject = project;
        myModuleScopeProvider = new ModuleScopeProviderImpl(this);

        myName = name;
        myImlFilePointer = VirtualFilePointerManager.getInstance().create(VfsUtilCore.pathToUrl(filePath), this, null);
    }

    @Override
    public void moduleAdded() {
        for (ModuleComponent component : getComponentInstancesOfType(ModuleComponent.class)) {
            component.moduleAdded();
        }

        Messages.showMessageDialog(project, "Module added", "a module has been loaded", Messages.getInformationIcon());

    }
}
