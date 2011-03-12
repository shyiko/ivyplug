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

import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.ui.configuration.PropertiesCompositePanel;
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
    private final JLabel ivyHomeLabel = new JLabel(IvyPlugBundle.message("ui.ivy.home.directory.label"));
    private final TextFieldWithBrowseButton ivyHomeTextField = new TextFieldWithBrowseButton();
    private final JCheckBox autoCleanupCheckbox = new JCheckBox(IvyPlugBundle.message("ui.auto.cleanup.checkbox"));
    private final JLabel ivySettingsXMLLabel = new JLabel(IvyPlugBundle.message("ui.project.path.to.ivysettings.xml.file"));
    private final TextFieldWithBrowseButton ivySettingsXML = new TextFieldWithBrowseButton();
    private final PropertiesCompositePanel propertiesCompositePanel = new PropertiesCompositePanel();

    private final IvyProjectConfigurationProjectComponent configurationProjectComponent;

    public IvyProjectConfigurationEditor(Project project) {
        configurationProjectComponent = project.getComponent(IvyProjectConfigurationProjectComponent.class);
    }

    public JComponent createComponent() {
        ivyHomeTextField.addActionListener(new BrowseFilesListener(ivyHomeTextField.getTextField(),
                IvyPlugBundle.message("ui.ivy.home.directory.filechooser.title"),
                IvyPlugBundle.message("ui.ivy.home.directory.filechooser.description"),
                BrowseFilesListener.SINGLE_DIRECTORY_DESCRIPTOR));
        ivySettingsXML.addActionListener(new BrowseFilesListener(ivySettingsXML.getTextField(),
                IvyPlugBundle.message("ui.select.ivysettings.xml.file.location"),
                IvyPlugBundle.message("ui.selected.file.will.be.used.for.ivy.configuration"),
                BrowseFilesListener.SINGLE_FILE_DESCRIPTOR));
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
        rootPanel.add(ivyHomeLabel, gc);
        rootPanel.add(ivyHomeTextField, gc);
        rootPanel.add(autoCleanupCheckbox, gc);
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
        return IvyPlugBundle.message("ivyplug.project.configuration.tab.title");
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public boolean isModified() {
        IvyProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        return !(equals(getPath(configuration.getIvyHome()), ivyHomeTextField.getText())) ||
               !(equals(getPath(configuration.getIvySettingsXMlFile()), ivySettingsXML.getText())) ||
               propertiesCompositePanel.isModified() || configuration.isAutoCleanup() != autoCleanupCheckbox.isSelected();
    }

    public void apply() throws ConfigurationException {
        IvyProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        configuration.setIvyHome(getFile(ivyHomeTextField.getText()));
        configuration.setIvySettingsXMlFile(getFile(ivySettingsXML.getText()));
        List<String> propertyFilesOriginList = propertiesCompositePanel.getPropertyFiles();
        List<File> propertyFiles = new ArrayList<File>(propertyFilesOriginList.size());
        for (String propertyFile : propertyFilesOriginList) {
            File file = new File(propertyFile);
            if (!file.exists())
                throw new ConfigurationException(IvyPlugBundle.message("file.doesnt.exist.exception", file.getAbsolutePath()));
            propertyFiles.add(file);
        }
        configuration.setPropertyFiles(propertyFiles);
        configuration.setCustomProperties(propertiesCompositePanel.getCustomProperties());
        configuration.setAutoCleanup(autoCleanupCheckbox.isSelected());
        propertiesCompositePanel.setUnModified();
    }

    public void reset() {
        IvyProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        ivyHomeTextField.setText(getPath(configuration.getIvyHome()));
        ivySettingsXML.setText(getPath(configuration.getIvySettingsXMlFile()));
        List<File> propertyFilesOriginList = configuration.getPropertyFiles();
        List<String> propertyFiles = new ArrayList<String>(propertyFilesOriginList.size());
        for (File file : propertyFilesOriginList) {
            propertyFiles.add(file.getAbsolutePath());
        }
        propertiesCompositePanel.setPropertyFiles(propertyFiles);
        propertiesCompositePanel.setCustomProperties(configuration.getCustomProperties());
        autoCleanupCheckbox.setSelected(configuration.isAutoCleanup());
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
                throw new ConfigurationException(IvyPlugBundle.message("file.doesnt.exist.exception", filePath));
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
