package ivyplug.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 02.02.2011
 */
public class PropertiesCompositePanel extends JPanel {

    private final JLabel propertyFilesLabel = new JLabel("Property files:");
    private final PropertyFilesPanel propertyFilesPanel = new PropertyFilesPanel();
    private final JLabel customPropertiesLabel = new JLabel("Custom properties:");
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
