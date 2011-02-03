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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import ivyplug.adapters.ModuleComponentAdapter;
import ivyplug.ui.module.IvyModuleConfiguration;
import ivyplug.ui.module.IvyModuleConfigurationModuleComponent;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
@State(name = "IvyModuleComponent",
       storages = {@Storage(id = "other", file = "$MODULE_FILE$")}
)
public class IvyModuleComponent extends ModuleComponentAdapter implements PersistentStateComponent<Element> {

    private static final String STATE_ORG = "org";
    private IvyModuleConfigurationModuleComponent ivyModuleConfigurationModuleComponent;
    private IvyProjectComponent ivyProjectComponent;
    private String org;

    public IvyModuleComponent(Module module) {
        super(module);
        ivyModuleConfigurationModuleComponent = module.getComponent(IvyModuleConfigurationModuleComponent.class);
        ivyProjectComponent = module.getProject().getComponent(IvyProjectComponent.class);
    }

    @NotNull
    public String getComponentName() {
        return "IvyModuleComponent";
    }

    public void loadState(Element state) {
        org = state.getAttributeValue(STATE_ORG);
    }

    public Element getState() {
        Element result = new Element(getComponentName());
        if (org != null)
            result.setAttribute(STATE_ORG, org);
        return result;
    }

    public String getOrg() {
        return org;
    }

    public boolean isIvyModule() {
        IvyModuleConfiguration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        return configuration.getIvyXMlFile() != null;
    }

    public ResolveReport resolve() throws IvyException {
        return resolve(null);
    }

    public ResolveReport resolve(ProgressIndicator indicator) throws IvyException {
        ResolveReport result = null;
        IvyModuleConfiguration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        File ivyXMl = configuration.getIvyXMlFile();
        if (ivyXMl != null) {
            ivyProjectComponent.setVariables(module.getName(), configuration.getResolvedProperties());
            ivyProjectComponent.bindWatcher(module.getName(), indicator);
            File ivySettingsXML = configuration.getIvySettingsXMlFile();
            if (ivySettingsXML != null)
                ivyProjectComponent.configure(module.getName(), ivySettingsXML);
            result = ivyProjectComponent.resolve(module.getName(), ivyXMl);
            ModuleDescriptor moduleDescriptor = result.getModuleDescriptor();
            ModuleRevisionId moduleRevisionId = moduleDescriptor.getModuleRevisionId();
            org = moduleRevisionId.getOrganisation();
        }
        return result;
    }
}
