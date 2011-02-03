package ivyplug.ui.project;

import com.intellij.openapi.project.Project;
import ivyplug.ui.Configuration;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 03.02.2011
 */
public class IvyProjectConfiguration extends Configuration {

    private Project project;

    public IvyProjectConfiguration(Project project) {
        this.project = project;
    }
}
