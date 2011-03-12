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

import com.intellij.openapi.project.Project;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.ui.configuration.Configuration;
import ivyplug.ui.messages.Message;
import ivyplug.ui.messages.MessagesProjectComponent;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 03.02.2011
 */
public class IvyProjectConfiguration extends Configuration {

    private Project project;
    private MessagesProjectComponent messagesProjectComponent;
    private File ivyHome;
    private boolean autoCleanup = true;

    public IvyProjectConfiguration(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public File getIvyHome() {
        return ivyHome;
    }

    public void setIvyHome(File ivyHome) {
        this.ivyHome = ivyHome;
    }

    public boolean isAutoCleanup() {
        return autoCleanup;
    }

    public void setAutoCleanup(boolean autoCleanup) {
        this.autoCleanup = autoCleanup;
    }

    @Override
    protected void warn(String message) {
        if (messagesProjectComponent == null)
            messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
        messagesProjectComponent.addToTab(IvyPlugBundle.message("ui.general.message.tab"), new Message(Message.Type.WARNING, message));
    }
}
