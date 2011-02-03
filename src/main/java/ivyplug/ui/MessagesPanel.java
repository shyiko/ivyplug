package ivyplug.ui;

import com.intellij.ide.CopyProvider;
import com.intellij.ide.ExporterToTextFile;
import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.ide.actions.ExportToTextFileToolbarAction;
import com.intellij.ide.errorTreeView.*;
import com.intellij.ide.errorTreeView.impl.ErrorViewTextExporter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.MessageView;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class MessagesPanel extends JPanel implements DataProvider, CopyProvider {

    protected Project project;

    private ErrorViewStructure errorViewStructure;
    private ErrorViewTreeBuilder errorViewTreeBuilder;
    private ExporterToTextFile exporterToTextFile;
    protected Tree messageTree;

    public MessagesPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        JPanel rootPanel = new JPanel(new BorderLayout());

        errorViewStructure = new ErrorViewStructure(project, true);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        root.setUserObject(errorViewStructure.createDescriptor(errorViewStructure.getRootElement(), null));
        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        messageTree = new Tree(treeModel) {
            public void setRowHeight(int i) {
                super.setRowHeight(0);
                // this is needed in order to make UI calculate the height for each particular row
            }
        };
        errorViewTreeBuilder = new ErrorViewTreeBuilder(messageTree, treeModel, errorViewStructure);
        exporterToTextFile = new ErrorViewTextExporter(errorViewStructure);

        TreeUtil.installActions(messageTree);
        UIUtil.setLineStyleAngled(messageTree);
        messageTree.setRootVisible(false);
        messageTree.setShowsRootHandles(true);
        messageTree.setLargeModel(true);
        JScrollPane scrollPane = NewErrorTreeRenderer.install(messageTree);
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        add(createToolbarPanel(), BorderLayout.WEST);
        add(rootPanel, BorderLayout.CENTER);

        messageTree.addMouseListener(new PopupHandler() {
            public void invokePopup(Component component, int x, int y) {
                final TreePath path = messageTree.getLeadSelectionPath();
                if (path == null) {
                    return;
                }
                DefaultActionGroup group = new DefaultActionGroup();
                group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_COPY));
                ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.COMPILER_MESSAGES_POPUP, group);
                menu.getComponent().show(component, x, y);
            }
        });
    }

    public void performCopy(DataContext dataContext) {
        final ErrorTreeNodeDescriptor descriptor = getSelectedNodeDescriptor();
        if (descriptor != null) {
            final String[] lines = descriptor.getElement().getText();
            CopyPasteManager.getInstance().setContents(new StringSelection(StringUtil.join(lines, "\n")));
        }
    }

    public boolean isCopyEnabled(DataContext dataContext) {
        return getSelectedNodeDescriptor() != null;
    }

    public boolean isCopyVisible(DataContext dataContext) {
        return true;
    }

    public Object getData(String dataId) {
        if (PlatformDataKeys.COPY_PROVIDER.is(dataId)) {
            return this;
        } else if (PlatformDataKeys.EXPORTER_TO_TEXT_FILE.is(dataId)) {
            return exporterToTextFile;
        }
        return null;
    }

    public void close() {
        MessageView messageView = MessageView.SERVICE.getInstance(project);
        Content content = messageView.getContentManager().getContent(this);
        if (content != null) {
            messageView.getContentManager().removeContent(content, true);
        }
    }

    public JComponent getComponent() {
        return this;
    }

    public void addMessage(ErrorTreeElementKind type, String[] text) {
        errorViewStructure.addMessage(type, text, null, -1, -1, null);
        errorViewTreeBuilder.updateTree();
    }

    public void clearMessages() {
        errorViewStructure.clear();
        errorViewTreeBuilder.updateTree();
    }

    private NavigatableMessageElement getSelectedMessageElement() {
        final ErrorTreeElement selectedElement = getSelectedErrorTreeElement();
        return selectedElement instanceof NavigatableMessageElement ? (NavigatableMessageElement) selectedElement : null;
    }

    public ErrorTreeElement getSelectedErrorTreeElement() {
        ErrorTreeNodeDescriptor treeNodeDescriptor = getSelectedNodeDescriptor();
        if (treeNodeDescriptor == null) return null;

        return treeNodeDescriptor.getElement();
    }

    public ErrorTreeNodeDescriptor getSelectedNodeDescriptor() {
        TreePath path = messageTree.getSelectionPath();
        if (path == null) {
            return null;
        }
        DefaultMutableTreeNode lastPathNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = lastPathNode.getUserObject();
        if (!(userObject instanceof ErrorTreeNodeDescriptor)) {
            return null;
        }
        return (ErrorTreeNodeDescriptor) userObject;
    }

    private JPanel createToolbarPanel() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new CloseTabToolbarAction() {
            public void actionPerformed(AnActionEvent e) {
                close();
            }
        });
        actionGroup.add(new ExportToTextFileToolbarAction(exporterToTextFile));
        JPanel result = new JPanel(new GridLayout(1, 1));
        ActionToolbar myLeftToolbar = ActionManager.getInstance().
                createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, actionGroup, false);
        result.add(myLeftToolbar.getComponent());
        return result;
    }
}

