package ivyplug.adapters;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public abstract class ProjectComponentAdapter implements ProjectComponent {

    protected Project project;

    protected ProjectComponentAdapter(Project project) {
        this.project = project;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
