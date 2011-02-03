package ivyplug.bundles;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 03.02.2011
 */
public class IvyPlugBundle {

    private static Reference<ResourceBundle> bundle;

    @NonNls
    private static final String BUNDLE = "IvyPlugBundle";

    private IvyPlugBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (IvyPlugBundle.bundle != null) bundle = IvyPlugBundle.bundle.get();
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            IvyPlugBundle.bundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
