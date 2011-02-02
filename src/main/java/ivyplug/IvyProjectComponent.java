package ivyplug;

import com.intellij.openapi.project.Project;
import ivyplug.adapters.ProjectComponentAdapter;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.settings.IvyVariableContainer;
import org.apache.ivy.util.DefaultMessageLogger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class IvyProjectComponent extends ProjectComponentAdapter {

    private Map<String, Ivy> moduleIvyMap;

    public IvyProjectComponent(Project project) {
        super(project);
        moduleIvyMap = new HashMap<String, Ivy>();
    }

    @NotNull
    public String getComponentName() {
        return "IvyProjectComponent";
    }

    public void configure(String module, File ivySettingXml) throws IvyException {
        Ivy ivy = getIvy(module);
        ivy.popContext();
        try {
            ivy.configure(ivySettingXml);
        } catch (Exception e) {
            throw new IvyException("Failed to load " + ivySettingXml.getAbsolutePath(), e);
        }
    }

    public ResolveReport resolve(String module, File ivyXml) throws IvyException {
        Ivy ivy = getIvy(module);
        try {
            return ivy.resolve(ivyXml);
        } catch (Exception e) {
            throw new IvyException("Failed to load " + ivyXml.getAbsolutePath(), e);
        }
    }

    public void setVariables(String module, Map<String, String> variables) throws IvyException {
        Ivy ivy = getIvy(module);
        IvySettings settings = ivy.getSettings();
        settings.addAllVariables(variables, true);
    }

    private Ivy getIvy(String module) throws IvyException {
        Ivy result = moduleIvyMap.get(module);
        if (result == null) {
            result = Ivy.newInstance();
            result.getLoggerEngine().setDefaultLogger(new DefaultMessageLogger(-1));
            try {
                result.configureDefault();
            } catch (Exception e) {
                throw new IvyException("Failed to load default ivysettings.xml", e);
            }
            moduleIvyMap.put(module, result);
        }
        return result;
    }
}
