package ivyplug.ui.module;

import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
public class IvyModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {

    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        return new ModuleConfigurationEditor[] {new IvyModuleConfigurationEditor(state)};
    }
}
