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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;
import ivyplug.adapters.ProjectComponentAdapter;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.dependencies.DependencySyncManager;
import ivyplug.dependencies.ProjectDependenciesManager;
import ivyplug.resolving.ResolveContext;
import ivyplug.resolving.ResolveException;
import ivyplug.resolving.ResolveResult;
import ivyplug.resolving.Resolver;
import ivyplug.resolving.dependencies.ResolvedDependency;
import ivyplug.resolving.dependencies.ResolvedLibraryDependency;
import ivyplug.resolving.dependencies.ResolvedModuleDependency;
import ivyplug.resolving.dependencies.UnresolvedDependency;
import ivyplug.resolving.events.*;
import ivyplug.ui.configuration.project.IvyProjectConfigurationProjectComponent;
import ivyplug.ui.messages.Message;
import ivyplug.ui.messages.MessagesProjectComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class IvyProjectComponent extends ProjectComponentAdapter {

    private ResolverLoader resolverLoader;
    private boolean syncInProgress;

    public IvyProjectComponent(Project project) {
        super(project);
        resolverLoader = project.getComponent(ResolverLoader.class);
    }

    public boolean isSyncInProgress() {
        return syncInProgress;
    }

    public void scheduleReimport(final List<Module> modules) {
        if (resolverLoader.loadIvy()) {
            refreshProxyConfigurationsIfAny();
            new Task.Backgroundable(project, IvyPlugBundle.message("synchronization.task.title"), false) {
                public void run(@NotNull ProgressIndicator indicator) {
                    syncInProgress = true;
                    try {
                        indicator.setText(IvyPlugBundle.message("synchronization.preparing.message"));
                        indicator.setFraction(0.0);
                        bindWatcher(indicator);
                        reimport(modules);
                        indicator.setFraction(1.0);
                    } finally {
                        syncInProgress = false;
                    }
                }
            }.queue();
        }
    }

    private void refreshProxyConfigurationsIfAny() {
        HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        httpConfigurable.setAuthenticator();
    }

    private void reimport(Collection<Module> modules) {
        Map<ResolveContext, ResolveResult> resolveMap = resolve(modules);
        for (Map.Entry<ResolveContext, ResolveResult> entry : resolveMap.entrySet()) {
            Module module = entry.getKey().getModuleToResolve();
            ResolveResult resolveResult = entry.getValue();
            DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
            for (ResolvedDependency dependency : resolveResult.getResolvedDependencies()) {
                switch (dependency.getDependencyType()) {
                    case LIBRARY:
                        dependencySyncManager.addResolvedLibraryDependency((ResolvedLibraryDependency) dependency);
                        break;
                    case MODULE:
                        dependencySyncManager.addResolvedModuleDependency((ResolvedModuleDependency) dependency);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            IvyProjectConfigurationProjectComponent projectConfigurationComponent =
                    module.getProject().getComponent(IvyProjectConfigurationProjectComponent.class);
            dependencySyncManager.commit(projectConfigurationComponent.getConfiguration().isAutoCleanup());
        }
        ProjectDependenciesManager projectDependenciesManager = project.getComponent(ProjectDependenciesManager.class);
        projectDependenciesManager.removeUnusedLibraries();
    }

    private Map<ResolveContext, ResolveResult> resolve(Collection<Module> modules) {
        Map<ResolveContext, ResolveResult> result = new HashMap<ResolveContext, ResolveResult>();
        Resolver resolver = resolverLoader.getResolver();
        for (Module module : modules) {
            ResolveContext resolveContext = new ResolveContext(module);
            ResolveResult resolveResult;
            try {
                resolveResult = resolver.resolve(resolveContext);
            } catch (ResolveException ex) {
                handleResolveException(module, ex);
                continue;
            }
            IvyModuleComponent moduleComponent = module.getComponent(IvyModuleComponent.class);
            moduleComponent.setOrg(resolveResult.getOrganisation());
            result.put(resolveContext, resolveResult);
        }
        processModuleDependencies(result);
        return result;
    }

    private void processModuleDependencies(Map<ResolveContext, ResolveResult> resolveMap) {
        Map<String, Module> ivyModules = new HashMap<String, Module>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            IvyModuleComponent moduleComponent = module.getComponent(IvyModuleComponent.class);
            String org;
            if (moduleComponent.isIvyModule() && (org = moduleComponent.getOrg()) != null) {
                ivyModules.put(org + ":" + module.getName(), module);
            }
        }
        Map<ResolveContext, ResolveResult> notFullyResolved = new HashMap<ResolveContext, ResolveResult>();
        for (Map.Entry<ResolveContext, ResolveResult> entry: resolveMap.entrySet()){
            ResolveContext resolveContext = entry.getKey();
            ResolveResult resolveResult = entry.getValue();
            List<ResolvedDependency> resolvedDependencies = resolveResult.getResolvedDependencies();
            List<UnresolvedDependency> unresolvedDependencies = resolveResult.getUnresolvedDependencies();
            for (int i = unresolvedDependencies.size() - 1; i > -1; i--) {
                UnresolvedDependency dependency = unresolvedDependencies.get(i);
                String org = dependency.getOrg();
                String moduleId = dependency.getModule();
                Module module = ivyModules.get(org + ":" + moduleId);
                if (module != null) {
                    unresolvedDependencies.remove(dependency);
                    resolvedDependencies.add(new ResolvedModuleDependency(module));
                }
            }
            if (!unresolvedDependencies.isEmpty()) {
                notFullyResolved.put(resolveContext, resolveResult);
            }
        }
        for (Map.Entry<ResolveContext, ResolveResult> entry: notFullyResolved.entrySet()){
            ResolveContext resolveContext = entry.getKey();
            ResolveResult resolveResult = entry.getValue();
            resolveMap.remove(resolveContext);
            Module moduleToResolve = resolveContext.getModuleToResolve();
            informAboutFailedDependencies(moduleToResolve, resolveResult.getUnresolvedDependencies());
        }
    }

    private void informAboutFailedDependencies(Module module, List<UnresolvedDependency> failedDependencies) {
        Project project = module.getProject();
        Message[] messages = new Message[failedDependencies.size()];
        int i = 0;
        for (UnresolvedDependency failedDependency : failedDependencies) {
            messages[i++] = new Message(Message.Type.ERROR, IvyPlugBundle.message("resolve.failed.dependency.message",
                    getAsAString(failedDependency.getOrg(), "") + ":" +
                            getAsAString(failedDependency.getModule(), "") + ":" +
                            getAsAString(failedDependency.getRev(), "unknown")));
        }
        MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
        messagesProjectComponent.show(module, messages);
    }

    private void handleResolveException(Module module, ResolveException ex) {
        MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
        messagesProjectComponent.show(module, new Message(Message.Type.ERROR, ex.getMessage(),
                IvyPlugBundle.message("resolve.exception.reason", ex.getCause().getMessage())));
    }

    private void bindWatcher(final ProgressIndicator indicator) {
        EventManager eventManager = EventManager.getInstance();
        eventManager.setEventListener(new ivyplug.resolving.events.EventListener() {
            public void onEvent(Event event) {
                //todo: refactor
                String statusMessage = null;
                if (event instanceof ResolveEvent) {
                    ResolveEvent e = (ResolveEvent) event;
                    String dependency = getAsAString(e.getOrg(), "") + ":" +
                            getAsAString(e.getModule(), "") + ":" +
                            getAsAString(e.getRev(), "unknown") +
                            getAsAString(":/", e.getBranch(), "");
                    statusMessage = IvyPlugBundle.message("resolve.event.message", dependency);
                } else if (event instanceof StartingDownloadEvent) {
                    StartingDownloadEvent e = (StartingDownloadEvent) event;
                    String dependency = getAsAString(e.getOrg(), "") + ":" +
                            getAsAString(e.getModule(), "") + ":" +
                            getAsAString(e.getRev(), "unknown") +
                            getAsAString(":/", e.getBranch(), "") +
                            getAsAString(" (", e.getType(), ")", "");
                    statusMessage = IvyPlugBundle.message("starting.download.event.message", dependency);
                } else if (event instanceof DownloadProgressEvent) {
                    DownloadProgressEvent e = (DownloadProgressEvent) event;
                    statusMessage = IvyPlugBundle.message("download.progress.event.message", e.getUri(),
                            e.getBytesCompleted() / 1024, e.getTotalSize() / 1024);
                }
                if (statusMessage != null) {
                    indicator.setText(statusMessage);
                }
            }
        });
    }

    private String getAsAString(String prefix, String value, String suffix, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return prefix + value + suffix;
    }

    private String getAsAString(String prefix, String value, String defaultValue) {
        return getAsAString(prefix, value, "", defaultValue);
    }

    private String getAsAString(String value, String defaultValue) {
        return getAsAString("", value, defaultValue);
    }
}
