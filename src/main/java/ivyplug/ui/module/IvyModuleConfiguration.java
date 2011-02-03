package ivyplug.ui.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import ivyplug.ui.Configuration;
import ivyplug.ui.project.IvyProjectConfiguration;
import ivyplug.ui.project.IvyProjectConfigurationProjectComponent;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 03.02.2011
 */
public class IvyModuleConfiguration extends Configuration {

    private static final String DEFAULT_IVY_XML_FILENAME = "ivy.xml";
    private static final String DEFAULT_IVY_SETTINGS_XML_FILENAME = "ivysettings.xml";

    private IvyProjectConfiguration projectConfiguration;
    private ModuleRootManager moduleRootManager;
    private boolean useAutoDiscovery = true;
    private File ivyXMlFile;

    public IvyModuleConfiguration(Module module) {
        moduleRootManager = ModuleRootManager.getInstance(module);
        IvyProjectConfigurationProjectComponent configurationProjectComponent =
                module.getProject().getComponent(IvyProjectConfigurationProjectComponent.class);
        projectConfiguration = configurationProjectComponent.getConfiguration();
    }

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
            setIvySettingsXMlFile(locateFileInsideModule(DEFAULT_IVY_SETTINGS_XML_FILENAME));
        }
        File result = super.getIvySettingsXMlFile();
        if (result == null) {
            setIvySettingsXMlFile(projectConfiguration.getIvySettingsXMlFile());
        }
        return result;
    }

    public Map<String, String> getResolvedProperties() {
        Map<String, String> result = new HashMap<String, String>(projectConfiguration.getResolvedProperties());
        result.put("basedir", ivyXMlFile.getParent());
        result.putAll(super.getResolvedProperties());
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
