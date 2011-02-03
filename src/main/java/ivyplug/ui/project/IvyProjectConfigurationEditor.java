package ivyplug.ui.project;

import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.ui.PropertiesCompositePanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
public class IvyProjectConfigurationEditor implements Configurable {

    private final JPanel rootPanel = new JPanel(new GridBagLayout());
    private final JLabel ivySettingsXMLLabel = new JLabel(IvyPlugBundle.message("path.to.ivysettings.xml.file"));
    private final TextFieldWithBrowseButton ivySettingsXML = new TextFieldWithBrowseButton();
    private final PropertiesCompositePanel propertiesCompositePanel = new PropertiesCompositePanel();

    private final IvyProjectConfigurationProjectComponent configurationProjectComponent;

    public IvyProjectConfigurationEditor(Project project) {
        configurationProjectComponent = project.getComponent(IvyProjectConfigurationProjectComponent.class);
    }

    public JComponent createComponent() {
        ivySettingsXML.addActionListener(new BrowseFilesListener(ivySettingsXML.getTextField(),
                IvyPlugBundle.message("select.ivysettings.xml.file.location"),
                IvyPlugBundle.message("selected.file.will.be.used.for.ivy.configuration"),
                BrowseFilesListener.SINGLE_FILE_DESCRIPTOR));
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
        rootPanel.add(ivySettingsXMLLabel, gc);
        rootPanel.add(ivySettingsXML, gc);
        rootPanel.add(propertiesCompositePanel, gc);
        gc.weighty = 1.0;
        rootPanel.add(new JPanel(new GridBagLayout()), gc);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return rootPanel;
    }

    @Nls
    public String getDisplayName() {
        return IvyPlugBundle.message("project.ivyplug.configuration");
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public boolean isModified() {
        IvyProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        return !(equals(getPath(configuration.getIvySettingsXMlFile()), ivySettingsXML.getText())) ||
               propertiesCompositePanel.isModified();
    }

    public void apply() throws ConfigurationException {
        IvyProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        configuration.setIvySettingsXMlFile(getFile(ivySettingsXML.getText()));
        List<String> propertyFilesOriginList = propertiesCompositePanel.getPropertyFiles();
        List<File> propertyFiles = new ArrayList<File>(propertyFilesOriginList.size());
        for (String propertyFile : propertyFilesOriginList) {
            File file = new File(propertyFile);
            if (!file.exists())
                throw new ConfigurationException(IvyPlugBundle.message("file.doesnt.exist", file.getAbsolutePath()));
            propertyFiles.add(file);
        }
        configuration.setPropertyFiles(propertyFiles);
        configuration.setCustomProperties(propertiesCompositePanel.getCustomProperties());
        propertiesCompositePanel.setUnModified();
    }

    public void reset() {
        IvyProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        ivySettingsXML.setText(getPath(configuration.getIvySettingsXMlFile()));
        List<File> propertyFilesOriginList = configuration.getPropertyFiles();
        List<String> propertyFiles = new ArrayList<String>(propertyFilesOriginList.size());
        for (File file : propertyFilesOriginList) {
            propertyFiles.add(file.getAbsolutePath());
        }
        propertiesCompositePanel.setPropertyFiles(propertyFiles);
        propertiesCompositePanel.setCustomProperties(configuration.getCustomProperties());
    }

    public void disposeUIResources() {
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
                throw new ConfigurationException(IvyPlugBundle.message("file.doesnt.exist", filePath));
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
