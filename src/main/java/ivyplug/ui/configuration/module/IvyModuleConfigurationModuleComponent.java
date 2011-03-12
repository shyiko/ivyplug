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
package ivyplug.ui.configuration.module;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import ivyplug.ui.configuration.ConfigurationComponent;
import org.jdom.Element;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
@State(name = "IvyModuleConfigurationModuleComponent",
       storages = {@Storage(id = "other", file = "$MODULE_FILE$")}
)
public class IvyModuleConfigurationModuleComponent extends ConfigurationComponent implements ModuleComponent {

    private static final String USE_AUTO_DISCOVERY = "useAutoDiscovery";

    public IvyModuleConfigurationModuleComponent(Module module) {
        super(new IvyModuleConfiguration(module));
    }

    public void loadState(Element state) {
        super.loadState(state);
        String useAutoDiscovery = state.getAttributeValue(USE_AUTO_DISCOVERY);
        getConfiguration().setUseAutoDiscovery(useAutoDiscovery == null || useAutoDiscovery.equalsIgnoreCase("true"));
    }

    public Element getState() {
        Element result = super.getState();
        result.setAttribute(USE_AUTO_DISCOVERY, ((Boolean) getConfiguration().isUseAutoDiscovery()).toString());
        return result;
    }

    public IvyModuleConfiguration getConfiguration() {
        return (IvyModuleConfiguration) super.getConfiguration();
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void moduleAdded() {
    }
}
