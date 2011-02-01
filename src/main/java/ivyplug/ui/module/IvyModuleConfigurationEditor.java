package ivyplug.ui.module;

import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
public class IvyModuleConfigurationEditor implements ModuleConfigurationEditor {

    private final JPanel rootPanel = new JPanel(new GridBagLayout());
    private final JCheckBox autoDiscoveryCheckBox  = new JCheckBox("Determine settings automatically", true);
    private final JLabel ivyXMLLabel = new JLabel("Path to ivy.xml file:");
    private final TextFieldWithBrowseButton ivyXML = new TextFieldWithBrowseButton();
    private final JLabel ivySettingsXMLLabel = new JLabel("Path to ivysettings.xml file:");
    private final TextFieldWithBrowseButton ivySettingsXML = new TextFieldWithBrowseButton();

    private final IvyModuleConfigurationModuleComponent ivyModuleConfigurationModuleComponent;

    public IvyModuleConfigurationEditor(ModuleConfigurationState state) {
        Module module = state.getRootModel().getModule();
        ivyModuleConfigurationModuleComponent = module.getComponent(IvyModuleConfigurationModuleComponent.class);
    }

    public JComponent createComponent() {
        autoDiscoveryCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                onAutoDiscoveryModeChange();
            }
        });
        ivyXML.addActionListener(new BrowseFilesListener(ivyXML.getTextField(),
                "Select ivy.xml file location",
                "Selected file will be used for dependencies resolving",
                BrowseFilesListener.SINGLE_FILE_DESCRIPTOR));
        ivySettingsXML.addActionListener(new BrowseFilesListener(ivySettingsXML.getTextField(),
                "Select ivysettings.xml file location",
                "Selected file will be used for Ivy configuration",
                BrowseFilesListener.SINGLE_FILE_DESCRIPTOR));
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
        rootPanel.add(autoDiscoveryCheckBox, gc);
        rootPanel.add(ivyXMLLabel, gc);
        rootPanel.add(ivyXML, gc);
        rootPanel.add(ivySettingsXMLLabel, gc);
        rootPanel.add(ivySettingsXML, gc);
        gc.weighty = 1.0;
        rootPanel.add(new JPanel(new GridBagLayout()), gc);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return rootPanel;
    }

    public void saveData() {
    }

    public void moduleStateChanged() {
    }

    @Nls
    public String getDisplayName() {
        return "IvyPlug Configuration";
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public boolean isModified() {
        IvyModuleConfigurationModuleComponent.Configuration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        return !(configuration.isUseAutoDiscovery() == autoDiscoveryCheckBox.isSelected() &&
                 equals(getPath(configuration.getIvyXMlFile()), ivyXML.getText()) &&
                 equals(getPath(configuration.getIvySettingsXMlFile()), ivySettingsXML.getText()));
    }

    public void apply() throws ConfigurationException {
        IvyModuleConfigurationModuleComponent.Configuration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        configuration.setUseAutoDiscovery(autoDiscoveryCheckBox.isSelected());
        configuration.setIvyXMlFile(getFile(ivyXML.getText()));
        configuration.setIvySettingsXMlFile(getFile(ivySettingsXML.getText()));
    }

    public void reset() {
        IvyModuleConfigurationModuleComponent.Configuration state = ivyModuleConfigurationModuleComponent.getConfiguration();
        autoDiscoveryCheckBox.setSelected(state.isUseAutoDiscovery());
        ivyXML.setText(getPath(state.getIvyXMlFile()));
        ivySettingsXML.setText(getPath(state.getIvySettingsXMlFile()));
        onAutoDiscoveryModeChange();
    }

    public void disposeUIResources() {
    }

    private void onAutoDiscoveryModeChange() {
        boolean useAutoDiscoveryMode = autoDiscoveryCheckBox.isSelected();
        ivyXML.setEnabled(!useAutoDiscoveryMode);
        ivySettingsXML.setEnabled(!useAutoDiscoveryMode);
    }

    private boolean equals(@Nullable String first, @Nullable String second) {
        if (first == null) {
            return second == null || second.trim().isEmpty();
        }
        if (second == null) {
            return first.trim().isEmpty();
        }
        return first.equals(second);
    }

    private File getFile(String filePath) throws ConfigurationException {
        File result = null;
        if (filePath != null && !filePath.trim().isEmpty()) {
            result = new File(filePath);
            if (!result.exists())
                throw new ConfigurationException("File " + filePath + " doesn't exist.");
        }
        return result;
    }

    private String getPath(File file) {
        String result = null;
        if (file != null) {
            result = file.getAbsolutePath();
        }
        return result;
    }

}
