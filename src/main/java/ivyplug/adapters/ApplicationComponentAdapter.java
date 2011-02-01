package ivyplug.adapters;

import com.intellij.openapi.components.ApplicationComponent;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public abstract class ApplicationComponentAdapter implements ApplicationComponent {

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
