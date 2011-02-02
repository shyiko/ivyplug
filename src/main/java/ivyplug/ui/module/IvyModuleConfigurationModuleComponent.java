package ivyplug.ui.module;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import ivyplug.adapters.ModuleComponentAdapter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
@State(name = "IvyModuleConfigurationModuleComponent",
       storages = {@Storage(id = "other", file = "$MODULE_FILE$")}
)
public class IvyModuleConfigurationModuleComponent extends ModuleComponentAdapter implements PersistentStateComponent<Element> {

    private static final String DEFAULT_IVY_XML_FILENAME = "ivy.xml";
    private static final String DEFAULT_IVY_SETTINGS_XML_FILENAME = "ivysettings.xml";
    private static final String USE_AUTO_DISCOVERY = "useAutoDiscovery";
    private static final String IVY_XML_FILE = "ivyXMLFile";
    private static final String IVY_SETTINGS_XML_FILE = "ivySettingsXMLFile";
    private static final String SETTINGS_ELEMENT_NAME = "ivyPlugSettings";
    private static final String PROPERTY_FILES_ELEMENT_NAME = "propertyFiles";
    private static final String PROPERTY_FILES_ATTRIBUTE_NAME = "attribute";
    private static final String CUSTOM_PROPERTIES_ELEMENT_NAME = "customProperties";
    private static final String CUSTOM_PROPERTIES_ATTRIBUTE_NAME = "attribute";
    private static final String CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME = "name";

    private Configuration configuration;
    private ModuleRootManager moduleRootManager;

    public IvyModuleConfigurationModuleComponent(Module module) {
        super(module);
        configuration = new Configuration();
        moduleRootManager = ModuleRootManager.getInstance(module);
    }

    @NotNull
    public String getComponentName() {
        return "IvyModuleConfigurationModuleComponent";
    }

    public void loadState(Element state) {
        String useAutoDiscovery = state.getAttributeValue(USE_AUTO_DISCOVERY);
        configuration.setUseAutoDiscovery(useAutoDiscovery == null ||
                                          useAutoDiscovery.equalsIgnoreCase("true"));
        String ivyXML = state.getAttributeValue(IVY_XML_FILE);
        if (ivyXML != null)
            configuration.setIvyXMlFile(new File(ivyXML));
        String ivySettingsXML = state.getAttributeValue(IVY_SETTINGS_XML_FILE);
        if (ivySettingsXML != null)
            configuration.setIvySettingsXMlFile(new File(ivySettingsXML));
        loadPropertyFiles(state);
        loadCustomProperties(state);
    }

    public Element getState() {
        Boolean useAutoDiscovery = configuration.isUseAutoDiscovery();
        File ivyXMl = configuration.getIvyXMlFile();
        File ivySettingsXMl = configuration.getIvySettingsXMlFile();
        Element element = new Element(SETTINGS_ELEMENT_NAME);
        if (ivyXMl != null)
            element.setAttribute(IVY_XML_FILE, ivyXMl.getAbsolutePath());
        if (ivySettingsXMl != null)
            element.setAttribute(IVY_SETTINGS_XML_FILE, ivySettingsXMl.getAbsolutePath());
        element.setAttribute(USE_AUTO_DISCOVERY, useAutoDiscovery.toString());
        element.addContent(getPropertyFiles());
        element.addContent(getCustomProperties());
        return element;
    }

    public Configuration getConfiguration() {
        return configuration;
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
        configuration.setPropertyFiles(propertyFiles);
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
        configuration.setCustomProperties(customProperties);
    }

    private Element getPropertyFiles() {
        Element result = new Element(PROPERTY_FILES_ELEMENT_NAME);
        for (File file : configuration.getPropertyFiles()) {
            Element attribute = new Element(PROPERTY_FILES_ATTRIBUTE_NAME);
            attribute.setText(file.getAbsolutePath());
            result.addContent(attribute);
        }
        return result;
    }

    private Element getCustomProperties() {
        Element result = new Element(CUSTOM_PROPERTIES_ELEMENT_NAME);
        for (Map.Entry<String, String> entry: configuration.getCustomProperties().entrySet()){
            Element attribute = new Element(CUSTOM_PROPERTIES_ATTRIBUTE_NAME);
            attribute.setAttribute(CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME, entry.getKey());
            attribute.setText(entry.getValue());
            result.addContent(attribute);
        }
        return result;
    }

    public class Configuration {

        private boolean useAutoDiscovery = true;
        private File ivyXMlFile;
        private File ivySettingsXMlFile;
        private List<File> propertyFiles = Collections.emptyList();
        private Map<String, String> customProperties = Collections.emptyMap();

        private Configuration() {}

        public boolean isUseAutoDiscovery() {
            return useAutoDiscovery;
        }

        public void setUseAutoDiscovery(boolean useAutoDiscovery) {
            this.useAutoDiscovery = useAutoDiscovery;
        }

        @Nullable
        public File getIvyXMlFile() {
            if (useAutoDiscovery) {
                ivyXMlFile = locateFileInsideModule(DEFAULT_IVY_XML_FILENAME);
            }
            return ivyXMlFile;
        }

        public void setIvyXMlFile(File ivyXMlFile) {
            this.ivyXMlFile = ivyXMlFile;
        }

        @Nullable
        public File getIvySettingsXMlFile() {
            if (useAutoDiscovery) {
                ivySettingsXMlFile = locateFileInsideModule(DEFAULT_IVY_SETTINGS_XML_FILENAME);
            }
            return ivySettingsXMlFile;
        }

        public void setIvySettingsXMlFile(File ivySettingsXMlFile) {
            this.ivySettingsXMlFile = ivySettingsXMlFile;
        }

        public List<File> getPropertyFiles() {
            return propertyFiles;
        }

        public void setPropertyFiles(List<File> propertyFiles) {
            this.propertyFiles = propertyFiles;
        }

        public Map<String, String> getCustomProperties() {
            return customProperties;
        }

        public void setCustomProperties(Map<String, String> customProperties) {
            this.customProperties = customProperties;
        }

        public Map<String, String> getResolvedProperties() {
            Map<String, String> result = new HashMap<String, String>();
            result.put("basedir", ivyXMlFile.getParent());
            result.putAll(customProperties);
            for (File file : getPropertyFiles()) {
                if (file.exists()) {
                    try {
                        Reader reader = new FileReader(file);
                        try {
                            Properties properties = new Properties();
                            properties.load(reader);
                            for (Map.Entry<Object, Object> entry: properties.entrySet()){
                                result.put((String) entry.getKey(), (String) entry.getValue());
                            }
                        } finally {
                            reader.close();
                        }
                    } catch (FileNotFoundException e) {
                        // todo: warn
                    } catch (IOException e) {
                        // todo: warn
                    }
                }
            }
            return result;
        }

        @Nullable
        private File locateFileInsideModule(String filename) {
            File result = null;
            VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
            for (VirtualFile contentRoot : contentRoots) {
                File file = new File(contentRoot.getPath(), filename);
                if (file.exists()) {
                    result = file;
                    break;
                }
            }
            return result;
        }
    }
}
