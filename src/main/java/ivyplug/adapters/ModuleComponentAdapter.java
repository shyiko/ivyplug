package ivyplug.adapters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 29.01.2011
 */
public abstract class ModuleComponentAdapter implements ModuleComponent {

    protected Module module;

    protected ModuleComponentAdapter(Module module) {
        this.module = module;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void moduleAdded() {
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
