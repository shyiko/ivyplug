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
package ivyplug.ui.configuration.project;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import ivyplug.ui.configuration.ConfigurationComponent;
import org.jdom.Element;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
@State(name = "IvyProjectConfigurationProjectComponent",
       storages = {@Storage(id = "other", file = "$PROJECT_FILE$")}
)
public class IvyProjectConfigurationProjectComponent extends ConfigurationComponent implements ProjectComponent {

    private static final String AUTO_CLEANUP = "autoCleanup";

    public IvyProjectConfigurationProjectComponent(Project project) {
        super(new IvyProjectConfiguration(project));
    }

    public void loadState(Element state) {
        super.loadState(state);
        String autoCleanup = state.getAttributeValue(AUTO_CLEANUP);
        getConfiguration().setAutoCleanup(autoCleanup == null || autoCleanup.equalsIgnoreCase("true"));
    }

    public Element getState() {
        Element result = super.getState();
        result.setAttribute(AUTO_CLEANUP, ((Boolean) getConfiguration().isAutoCleanup()).toString());
        return result;
    }

    public IvyProjectConfiguration getConfiguration() {
        return (IvyProjectConfiguration) super.getConfiguration();
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }
}
