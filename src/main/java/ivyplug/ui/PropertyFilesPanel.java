package ivyplug.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTypeDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 02.02.2011
 */
public class PropertyFilesPanel extends JPanel {

    private final JBList propertyFilesList = new JBList(new DefaultListModel());
    private final JButton addButton = new JButton("Add");
    private final JButton removeButton = new JButton("Remove");
    private final JButton moveUpButton = new JButton("Move Up");
    private final JButton moveDownButton = new JButton("Move Down");
    private boolean modified;

    public PropertyFilesPanel() {
        final FileChooserDescriptor fileChooserDescriptor = new FileTypeDescriptor("Add *.properties", "properties");
        final FileChooserFactory instance = FileChooserFactory.getInstance();
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) propertyFilesList.getModel();
                final FileChooserDialog fileChooser = instance.createFileChooser(fileChooserDescriptor, PropertyFilesPanel.this);
                VirtualFile[] files = fileChooser.choose(null, null);
                for (VirtualFile file : files) {
                    model.addElement(file.getPath());
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) propertyFilesList.getModel();
                int[] indicesAsc = propertyFilesList.getSelectedIndices();
                Arrays.sort(indicesAsc);
                for (int i = indicesAsc.length - 1; i > -1; i--) {
                    model.remove(indicesAsc[i]);
                }
            }
        });

        moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) propertyFilesList.getModel();
                int selectedIndex = propertyFilesList.getSelectedIndex();
                if (selectedIndex > 0) {
                    Object selectedItem = model.remove(selectedIndex);
                    model.add(selectedIndex - 1, selectedItem);
                    propertyFilesList.setSelectedIndex(selectedIndex - 1);
                }
            }
        });

        moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) propertyFilesList.getModel();
                int selectedIndex = propertyFilesList.getSelectedIndex();
                int size = model.getSize();
                if (selectedIndex + 1 < size) {
                    Object selectedItem = model.remove(selectedIndex);
                    model.add(selectedIndex + 1, selectedItem);
                    propertyFilesList.setSelectedIndex(selectedIndex + 1);
                }
            }
        });

        propertyFilesList.getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) { setModified(); }
            public void intervalRemoved(ListDataEvent e) {  setModified(); }
            public void contentsChanged(ListDataEvent e) { setModified(); }
        });

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 140));
        setMinimumSize(new Dimension(100, 140));
        propertyFilesList.setBorder(BorderFactory.createEtchedBorder());
        JPanel propertyFilesWrapperPanel = new JPanel(new BorderLayout());
        propertyFilesWrapperPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        propertyFilesWrapperPanel.add(ScrollPaneFactory.createScrollPane(propertyFilesList), BorderLayout.CENTER);
        add(propertyFilesWrapperPanel, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 0), 0, 0);
        addButton.setSize(new Dimension(104, 25));
        addButton.setPreferredSize(new Dimension(104, 25));
        controlPanel.add(addButton, gc);
        controlPanel.add(removeButton, gc);
        controlPanel.add(moveUpButton, gc);
        controlPanel.add(moveDownButton, gc);
        gc.weighty = 1.0;
        controlPanel.add(new JPanel(new GridBagLayout()), gc);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        add(controlPanel, BorderLayout.EAST);
    }

    public boolean isModified() {
        return modified;
    }

    public void setData(List<String> data) {
        DefaultListModel model = (DefaultListModel) propertyFilesList.getModel();
        model.clear();
        for (String element : data) {
            model.addElement(element);
        }
        setUnModified();
    }

    public List<String> getData() {
        DefaultListModel model = (DefaultListModel) propertyFilesList.getModel();
        List<String> result = new ArrayList<String>(model.getSize());
        Enumeration<?> enumeration = model.elements();
        while (enumeration.hasMoreElements()) {
            String value = (String) enumeration.nextElement();
            result.add(value);
        }
        return result;
    }

    public void setUnModified() {
        modified = false;
    }

    private void setModified() {
        modified = true;
    }
}
