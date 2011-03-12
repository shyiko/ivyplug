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
import ivyplug.adapters.ModuleComponentAdapter;
import ivyplug.ui.configuration.module.IvyModuleConfiguration;
import ivyplug.ui.configuration.module.IvyModuleConfigurationModuleComponent;
import org.jdom.Element;

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
    private String org;

    public IvyModuleComponent(Module module) {
        super(module);
        ivyModuleConfigurationModuleComponent = module.getComponent(IvyModuleConfigurationModuleComponent.class);
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

    public void setOrg(String org) {
        this.org = org;
    }

    public boolean isIvyModule() {
        IvyModuleConfiguration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        return configuration.getIvyXMlFile() != null;
    }
}
