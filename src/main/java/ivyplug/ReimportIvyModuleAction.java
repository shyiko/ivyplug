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
import com.intellij.openapi.project.Project;
import ivyplug.bundles.IvyPlugBundle;

import java.util.Arrays;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 28.01.2011
 */
public class ReimportIvyModuleAction extends AnAction {

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Module module = e.getData(LangDataKeys.MODULE);
        presentation.setVisible(module != null);
        if (presentation.isVisible()) {
            IvyProjectComponent ivyPlugProjectComponent = module.getProject().getComponent(IvyProjectComponent.class);
            presentation.setEnabled(!ivyPlugProjectComponent.isSyncInProgress());
            if (presentation.isEnabled()) {
                IvyModuleComponent ivyModuleComponent = module.getComponent(IvyModuleComponent.class);
                presentation.setEnabled(ivyModuleComponent.isIvyModule());
            }
            presentation.setText(IvyPlugBundle.message("ui.reimport.ivy.module.parametrized", module.getName()));
        }
    }

    public void actionPerformed(AnActionEvent e) {
        final Module module = e.getData(LangDataKeys.MODULE);
        if (module == null)
            return;
        Project project = module.getProject();
        IvyProjectComponent ivyProjectComponent = project.getComponent(IvyProjectComponent.class);
        ivyProjectComponent.scheduleReimport(Arrays.asList(module));
    }
}
