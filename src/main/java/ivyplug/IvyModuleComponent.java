package ivyplug;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import ivyplug.adapters.ModuleComponentAdapter;
import ivyplug.ui.module.IvyModuleConfiguration;
import ivyplug.ui.module.IvyModuleConfigurationModuleComponent;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
@State(name = "IvyModuleComponent",
       storages = {@Storage(id = "other", file = "$MODULE_FILE$")}
)
public class IvyModuleComponent extends ModuleComponentAdapter implements PersistentStateComponent<Element> {

    private static final String STATE_ORG = "org";
    private IvyModuleConfigurationModuleComponent ivyModuleConfigurationModuleComponent;
    private IvyProjectComponent ivyProjectComponent;
    private String org;

    public IvyModuleComponent(Module module) {
        super(module);
        ivyModuleConfigurationModuleComponent = module.getComponent(IvyModuleConfigurationModuleComponent.class);
        ivyProjectComponent = module.getProject().getComponent(IvyProjectComponent.class);
    }

    @NotNull
    public String getComponentName() {
        return "IvyModuleComponent";
    }

    public void loadState(Element state) {
        org = state.getAttributeValue(STATE_ORG);
    }

    public Element getState() {
        Element result = new Element(getComponentName());
        if (org != null)
            result.setAttribute(STATE_ORG, org);
        return result;
    }

    public String getOrg() {
        return org;
    }

    public boolean isIvyModule() {
        IvyModuleConfiguration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        return configuration.getIvyXMlFile() != null;
    }

    public ResolveReport resolve() throws IvyException {
        return resolve(null);
    }

    public ResolveReport resolve(ProgressIndicator indicator) throws IvyException {
        ResolveReport result = null;
        IvyModuleConfiguration configuration = ivyModuleConfigurationModuleComponent.getConfiguration();
        File ivyXMl = configuration.getIvyXMlFile();
        if (ivyXMl != null) {
            ivyProjectComponent.setVariables(module.getName(), configuration.getResolvedProperties());
            ivyProjectComponent.bindWatcher(module.getName(), indicator);
            File ivySettingsXML = configuration.getIvySettingsXMlFile();
            if (ivySettingsXML != null)
                ivyProjectComponent.configure(module.getName(), ivySettingsXML);
            result = ivyProjectComponent.resolve(module.getName(), ivyXMl);
            ModuleDescriptor moduleDescriptor = result.getModuleDescriptor();
            ModuleRevisionId moduleRevisionId = moduleDescriptor.getModuleRevisionId();
            org = moduleRevisionId.getOrganisation();
        }
        return result;
    }
}
