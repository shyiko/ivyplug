package ivyplug;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
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
import ivyplug.ui.IvyModuleMessagesPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class MessagesProjectComponent extends ProjectComponentAdapter {

    private MessageView messageView;
    private ToolWindowManager toolWindowManager;
    private ContentFactory contentFactory;

    public MessagesProjectComponent(Project project) {
        super(project);
        messageView = MessageView.SERVICE.getInstance(project);
        toolWindowManager = ToolWindowManager.getInstance(project);
        contentFactory = ContentFactory.SERVICE.getInstance();
    }

    @NotNull
    public String getComponentName() {
        return "MessagesProjectComponent";
    }

    public void open(Module module, ErrorTreeElementKind messageType, String[] message) {
        List<Map.Entry<ErrorTreeElementKind, String[]>> messageList = new LinkedList<Map.Entry<ErrorTreeElementKind, String[]>>();
        messageList.add(new AbstractMap.SimpleEntry<ErrorTreeElementKind, String[]>(messageType, message));
        open(module, messageList);
    }

    public void open(final Module module, final List<Map.Entry<ErrorTreeElementKind, String[]>> messages) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                IvyModuleMessagesPanel result = new IvyModuleMessagesPanel(project);
                for (Map.Entry<ErrorTreeElementKind, String[]> entry : messages) {
                    result.addMessage(entry.getKey(), entry.getValue());
                }
                Content content = contentFactory.createContent(result, getTabTitle(module), true);
                ContentManager contentManager = messageView.getContentManager();
                contentManager.addContent(content);
                contentManager.setSelectedContent(content);
                ToolWindow toolWindow = toolWindowManager.getToolWindow(ToolWindowId.MESSAGES_WINDOW);
                if (toolWindow != null) {
                    toolWindow.activate(null);
                }
            }
        });
    }

    public void close(final Module module) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ContentManager contentManager = messageView.getContentManager();
                Content content = contentManager.findContent(getTabTitle(module));
                if (content != null)
                    contentManager.removeContent(content, true);
            }
        });
    }

    private String getTabTitle(Module module) {
        return "Ivy: \"" + module.getName() + "\"";
    }
}
