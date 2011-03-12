/*
 * Copyright 2011 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ivyplug;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.ui.messages.MessagesProjectComponent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class ReimportAllIvyModulesAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = e.getData(LangDataKeys.PROJECT);
        presentation.setVisible(project != null);
        if (presentation.isVisible()) {
            IvyProjectComponent ivyPlugProjectComponent = project.getComponent(IvyProjectComponent.class);
            presentation.setEnabled(!ivyPlugProjectComponent.isSyncInProgress());
        }
    }

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
        messagesProjectComponent.closeIvyPlugMessageTabs();
        List<Module> ivyModules = getIvyModules(project);
        if (ivyModules.isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Messages.showInfoMessage(IvyPlugBundle.message("no.ivy.modules.found.message.description"),
                            IvyPlugBundle.message("no.ivy.modules.found.message.title"));
                }
            });
            return;
        }
        IvyProjectComponent ivyProjectComponent = project.getComponent(IvyProjectComponent.class);
        ivyProjectComponent.scheduleReimport(ivyModules);
    }

    private List<Module> getIvyModules(Project project) {
        List<Module> result = new ArrayList<Module>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getModules();
        for (Module module : modules) {
            IvyModuleComponent moduleComponent = module.getComponent(IvyModuleComponent.class);
            if (moduleComponent.isIvyModule()) {
                result.add(module);
            }
        }
        return result;
    }
}
