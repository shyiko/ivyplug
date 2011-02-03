package ivyplug.ui.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import ivyplug.adapters.ProjectComponentAdapter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
@State(name = "IvyProjectConfigurationProjectComponent",
       storages = {@Storage(id = "other", file = "$PROJECT_FILE$")}
)
public class IvyProjectConfigurationProjectComponent extends ProjectComponentAdapter implements PersistentStateComponent<Element> {

    private static final String IVY_SETTINGS_XML_FILE = "ivySettingsXMLFile";
    private static final String SETTINGS_ELEMENT_NAME = "ivyPlugSettings";
    private static final String PROPERTY_FILES_ELEMENT_NAME = "propertyFiles";
    private static final String PROPERTY_FILES_ATTRIBUTE_NAME = "attribute";
    private static final String CUSTOM_PROPERTIES_ELEMENT_NAME = "customProperties";
    private static final String CUSTOM_PROPERTIES_ATTRIBUTE_NAME = "attribute";
    private static final String CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME = "name";

    private final IvyProjectConfiguration ivyProjectConfiguration;

    public IvyProjectConfigurationProjectComponent(Project project) {
        super(project);
        ivyProjectConfiguration = new IvyProjectConfiguration(project);
    }

    @NotNull
    public String getComponentName() {
        return "IvyProjectConfigurationProjectComponent";
    }

    public void loadState(Element state) {
        String ivySettingsXML = state.getAttributeValue(IVY_SETTINGS_XML_FILE);
        if (ivySettingsXML != null)
            ivyProjectConfiguration.setIvySettingsXMlFile(new File(ivySettingsXML));
        loadPropertyFiles(state);
        loadCustomProperties(state);
    }

    public Element getState() {
        Element element = new Element(SETTINGS_ELEMENT_NAME);
        File ivySettingsXMl = ivyProjectConfiguration.getIvySettingsXMlFile();
        if (ivySettingsXMl != null)
            element.setAttribute(IVY_SETTINGS_XML_FILE, ivySettingsXMl.getAbsolutePath());
        element.addContent(getPropertyFiles());
        element.addContent(getCustomProperties());
        return element;
    }

    public IvyProjectConfiguration getConfiguration() {
        return ivyProjectConfiguration;
    }

    private void loadPropertyFiles(Element state) {
        Element element = state.getChild(PROPERTY_FILES_ELEMENT_NAME);
        List<File> propertyFiles;
        if (element == null) {
            propertyFiles = Collections.emptyList();
        } else {
            List children = element.getChildren(PROPERTY_FILES_ATTRIBUTE_NAME);
            propertyFiles = new ArrayList<File>(children.size());
            for (Object attribute_ : children) {
                Element attribute = (Element) attribute_;
                propertyFiles.add(new File(attribute.getTextTrim()));
            }
        }
        ivyProjectConfiguration.setPropertyFiles(propertyFiles);
    }

    private void loadCustomProperties(Element state) {
        Element element = state.getChild(CUSTOM_PROPERTIES_ELEMENT_NAME);
        Map<String, String> customProperties;
        if (element == null) {
            customProperties = Collections.emptyMap();
        } else {
            List children = element.getChildren(CUSTOM_PROPERTIES_ATTRIBUTE_NAME);
            customProperties = new HashMap<String, String>(children.size());
            for (Object attribute_ : children) {
                Element attribute = (Element) attribute_;
                customProperties.put(attribute.getAttributeValue(CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME),
                        attribute.getTextTrim());
            }
        }
        ivyProjectConfiguration.setCustomProperties(customProperties);
    }

    private Element getPropertyFiles() {
        Element result = new Element(PROPERTY_FILES_ELEMENT_NAME);
        for (File file : ivyProjectConfiguration.getPropertyFiles()) {
            Element attribute = new Element(PROPERTY_FILES_ATTRIBUTE_NAME);
            attribute.setText(file.getAbsolutePath());
            result.addContent(attribute);
        }
        return result;
    }

    private Element getCustomProperties() {
        Element result = new Element(CUSTOM_PROPERTIES_ELEMENT_NAME);
        for (Map.Entry<String, String> entry: ivyProjectConfiguration.getCustomProperties().entrySet()){
            Element attribute = new Element(CUSTOM_PROPERTIES_ATTRIBUTE_NAME);
            attribute.setAttribute(CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME, entry.getKey());
            attribute.setText(entry.getValue());
            result.addContent(attribute);
        }
        return result;
    }
}
