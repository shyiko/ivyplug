/*
 * Copyright 2011 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ivyplug;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import ivyplug.adapters.ProjectComponentAdapter;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.resolving.Resolver;
import ivyplug.ui.configuration.project.IvyProjectConfiguration;
import ivyplug.ui.configuration.project.IvyProjectConfigurationProjectComponent;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 10.02.2011
 */
public class ResolverLoader extends ProjectComponentAdapter {

    private File ivyHome;
    private Resolver resolver;

    public ResolverLoader(Project project) {
        super(project);
    }

    public Resolver getResolver() {
        return resolver;
    }

    public boolean loadIvy() {
        final AtomicBoolean result = new AtomicBoolean();
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            public void run() {
                File ivyHome = getIvyHomeFromProjectSettings();
                if (ivyHome == null) {
                    boolean takeToIvyPlugSettings = Messages.showYesNoDialog(IvyPlugBundle.message("no.configured.ivy.message.description"),
                            IvyPlugBundle.message("no.configured.ivy.message.title"), Messages.getQuestionIcon()) == 0;
                    if (takeToIvyPlugSettings) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, IvyPlugBundle.message("ivyplug.project.configuration.tab.title"));
                        ivyHome = getIvyHomeFromProjectSettings();
                    }
                }
                if (ivyHome != null && !ivyHome.equals(ResolverLoader.this.ivyHome)) {
                    ResolverLoader.this.ivyHome = ivyHome;
                    reloadResolver(ivyHome);
                }
                result.set(ivyHome != null);
            }
        });
        return result.get();
    }

    private File getIvyHomeFromProjectSettings() {
        IvyProjectConfigurationProjectComponent projectComponent =
                project.getComponent(IvyProjectConfigurationProjectComponent.class);
        IvyProjectConfiguration configuration = projectComponent.getConfiguration();
        return configuration.getIvyHome();
    }

    private void reloadResolver(File ivyHome) {
        List<URL> ivyLibraries = getLibraries(ivyHome);
        URL[] urls = new URL[ivyLibraries.size() + 1];
        int i = 0;
        for (URL ivyLibrary : ivyLibraries) {
            urls[i++] = ivyLibrary;
        }
        urls[i] = findBridgeJar();
        ClassLoader parentClassLoader = ResolverLoader.class.getClassLoader();
        URLClassLoader bridgeClassLoader = new BridgeClassLoader(urls, parentClassLoader);
        try {
            Class<?> resolverImpl = bridgeClassLoader.loadClass("ivyplug.ivy.bridge.ResolverImpl");
            resolver = (Resolver) resolverImpl.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URL findBridgeJar() {
        PluginClassLoader classLoader = (PluginClassLoader) getClass().getClassLoader();
        PluginId pluginId = classLoader.getPluginId();
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginId);
        if (pluginDescriptor == null) {
            throw new IllegalStateException();
        }
        File pluginHomeDir = pluginDescriptor.getPath();
        return toURL(new File(pluginHomeDir, "lib/ext/ivyplug-ivy-bridge.jar"));
    }

    private List<URL> getLibraries(File ivyHome) {
        File libDirectory = new File(ivyHome, "lib");
        List<URL> result = new ArrayList<URL>();
        if (libDirectory.exists())
            result.addAll(listJarsRecursive(libDirectory));
        File[] rootJars = ivyHome.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getName().toLowerCase().endsWith(".jar");
            }
        });
        for (File jar : rootJars) {
            result.add(toURL(jar));
        }
        return result;
    }

    private List<URL> listJarsRecursive(File directory) {
        List<URL> result = new ArrayList<URL>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(listJarsRecursive(file));
            } else {
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    result.add(toURL(file));
                }
            }
        }
        return result;
    }

    private URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
