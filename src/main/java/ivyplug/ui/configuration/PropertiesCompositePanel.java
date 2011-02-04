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
package ivyplug.ui.configuration;

import ivyplug.bundles.IvyPlugBundle;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 02.02.2011
 */
public class PropertiesCompositePanel extends JPanel {

    private final JLabel propertyFilesLabel = new JLabel(IvyPlugBundle.message("composite.panel.property.files"));
    private final PropertyFilesPanel propertyFilesPanel = new PropertyFilesPanel();
    private final JLabel customPropertiesLabel = new JLabel(IvyPlugBundle.message("composite.panel.custom.properties"));
    private final PropertiesPanel customPropertiesPanel = new PropertiesPanel();

    public PropertiesCompositePanel() {
        setLayout(new GridBagLayout());
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 0), 0, 0);
        add(propertyFilesLabel, gc);
        add(propertyFilesPanel, gc);
        add(customPropertiesLabel, gc);
        add(customPropertiesPanel, gc);
    }

    public List<String> getPropertyFiles() {
        return propertyFilesPanel.getData();
    }

    public void setPropertyFiles(List<String> data) {
        propertyFilesPanel.setData(data);
    }

    public Map<String, String> getCustomProperties() {
        return customPropertiesPanel.getData();
    }

    public void setCustomProperties(Map<String, String> data) {
        customPropertiesPanel.setData(data);
    }

    public boolean isModified() {
        return propertyFilesPanel.isModified() || customPropertiesPanel.isModified();
    }

    public void setUnModified() {
        propertyFilesPanel.setUnModified();
        customPropertiesPanel.setUnModified();
    }
}
