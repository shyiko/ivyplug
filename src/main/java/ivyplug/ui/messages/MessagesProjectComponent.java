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
package ivyplug.ui.messages;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.MessageView;
import ivyplug.adapters.ProjectComponentAdapter;
import ivyplug.bundles.IvyPlugBundle;

import javax.swing.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class MessagesProjectComponent extends ProjectComponentAdapter {

    private static final String MODULE_TAB_PREFIX = "Ivy:";
    private MessageView messageView;
    private ToolWindowManager toolWindowManager;
    private ContentFactory contentFactory;

    public MessagesProjectComponent(Project project) {
        super(project);
        messageView = MessageView.SERVICE.getInstance(project);
        toolWindowManager = ToolWindowManager.getInstance(project);
        contentFactory = ContentFactory.SERVICE.getInstance();
    }

    public void show(final Module module, final Message... messages) {
        show(getTabTitle(module), messages);
    }

    public void show(final String tabTitle, final Message... messages) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                closeTab(tabTitle);
                showTab(tabTitle, messages);
            }
        });
    }

    public void showInNewTab(final Module module, final Message... messages) {
        showInNewTab(getTabTitle(module), messages);
    }

    public void showInNewTab(final String tabTitle, final Message... messages) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                showTab(tabTitle, messages);
            }
        });
    }

    public void addToTab(final String tabTitle, final Message... messages) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ContentManager contentManager = messageView.getContentManager();
                Content content = contentManager.findContent(tabTitle);
                if (content == null) {
                    MessagesPanel result = new MessagesPanel(project);
                    content = contentFactory.createContent(result, tabTitle, true);
                    contentManager.addContent(content);
                }
                MessagesPanel messagesPanel = (MessagesPanel) content.getComponent();
                for (Message message : messages) {
                    messagesPanel.addMessage(message.getType().getInternalType(), message.getMessage());
                }
                if (!contentManager.isSelected(content))
                    contentManager.setSelectedContent(content);
                ToolWindow toolWindow = toolWindowManager.getToolWindow(ToolWindowId.MESSAGES_WINDOW);
                if (toolWindow != null && !toolWindow.isActive()) {
                    toolWindow.activate(null);
                }
            }
        });
    }

    public void close(final Module module) {
        close(getTabTitle(module));
    }

    public void close(final String module) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                closeTab(module);
            }
        });
    }

    public void closeOurMessageTabs() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ContentManager contentManager = messageView.getContentManager();
                Content[] contents = contentManager.getContents();
                String ivyPlugBundle = IvyPlugBundle.message("general.message.tab");
                for (int i = contents.length - 1; i > -1; i--) {
                    Content content = contents[i];
                    String tabName = content.getTabName();
                    if (tabName.startsWith(MODULE_TAB_PREFIX) || tabName.equals(ivyPlugBundle))
                        contentManager.removeContent(content, true);
                }
            }
        });
    }

    private void showTab(String tabTitle, Message... messages) {
        MessagesPanel result = new MessagesPanel(project);
        for (Message message : messages) {
            result.addMessage(message.getType().getInternalType(), message.getMessage());
        }
        Content content = contentFactory.createContent(result, tabTitle, true);
        ContentManager contentManager = messageView.getContentManager();
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(ToolWindowId.MESSAGES_WINDOW);
        if (toolWindow != null && !toolWindow.isActive()) {
            toolWindow.activate(null);
        }
    }

    private void closeTab(String tabTitle) {
        ContentManager contentManager = messageView.getContentManager();
        Content content = contentManager.findContent(tabTitle);
        if (content != null)
            contentManager.removeContent(content, true);
    }

    private String getTabTitle(Module module) {
        return IvyPlugBundle.message("messages.toolwindow.tab", MODULE_TAB_PREFIX, module.getName());
    }
}
