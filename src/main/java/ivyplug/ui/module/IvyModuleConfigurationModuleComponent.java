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

import java.io.File;

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
        return element;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public class Configuration {

        private boolean useAutoDiscovery = true;
        private File ivyXMlFile;
        private File ivySettingsXMlFile;

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
