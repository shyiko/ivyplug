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
package ivyplug.ivy.bridge;

import com.intellij.openapi.module.Module;
import ivyplug.resolving.*;
import ivyplug.resolving.dependencies.ArtifactType;
import ivyplug.resolving.dependencies.ResolvedLibraryDependency;
import ivyplug.resolving.dependencies.Scope;
import ivyplug.resolving.dependencies.UnresolvedDependency;
import ivyplug.resolving.events.DownloadProgressEvent;
import ivyplug.resolving.events.ResolveEvent;
import ivyplug.resolving.events.StartingDownloadEvent;
import ivyplug.ui.configuration.module.IvyModuleConfiguration;
import ivyplug.ui.configuration.module.IvyModuleConfigurationModuleComponent;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.core.event.IvyListener;
import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;
import org.apache.ivy.core.event.resolve.StartResolveDependencyEvent;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ArtifactRevisionId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.util.DefaultMessageLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 11.03.2011
 */
public class ResolverImpl implements Resolver {

    public ResolveResult resolve(ResolveContext context) throws ResolveException {
        Module moduleToResolve = context.getModuleToResolve();
        IvyModuleConfigurationModuleComponent configurationModuleComponent =
                    moduleToResolve.getComponent(IvyModuleConfigurationModuleComponent.class);
        IvyModuleConfiguration configuration = configurationModuleComponent.getConfiguration();
        ResolveReport resolveReport = resolve(configuration);
        return buildResolveResult(resolveReport);
    }

    private ResolveReport resolve(IvyModuleConfiguration configuration) throws IvyConfigurationException, IvyResolveException {
        Ivy ivy = getIvy(configuration);
        File ivySettingsXML = configuration.getIvySettingsXMlFile();
        try {
            if (ivySettingsXML == null) {
                ivy.configureDefault();
            } else {
                ivy.configure(ivySettingsXML);
            }
        } catch (Exception e) {
            throw new IvyConfigurationException(ivySettingsXML == null ? null : ivySettingsXML.getAbsolutePath(), e);
        }
        File ivyXMlFile = configuration.getIvyXMlFile();
        if (ivyXMlFile == null)
            throw new IllegalStateException();
        try {
            return ivy.resolve(ivyXMlFile);
        } catch (Exception e) {
            throw new IvyResolveException(ivyXMlFile.getAbsolutePath(), e);
        }
    }

    private Ivy getIvy(IvyModuleConfiguration configuration) {
        IvyPlugVariableContainer variableContainer = new IvyPlugVariableContainer();
        IvyPlugEventManager eventManager = new IvyPlugEventManager();
        DefaultMessageLogger messageLogger = new DefaultMessageLogger(-1);
        IvySettings ivySettings = new IvySettings(variableContainer);
        Ivy ivy = new Ivy();
        ivy.setEventManager(eventManager);
        ivy.setSettings(ivySettings);
        ivy.bind();
        variableContainer.bind();
        variableContainer.clean();
        ivySettings.addAllVariables(configuration.getResolvedProperties(), true);
        eventManager.removeAllListeners();
        ivy.getLoggerEngine().setDefaultLogger(messageLogger);
        bindWatcher(ivy);
        return ivy;
    }

    private ResolveResult buildResolveResult(ResolveReport resolveReport) {
        ResolveResult.Builder builder = new ResolveResult.Builder();
        populateWithOrganisation(builder, resolveReport);
        populateWithDependencies(builder, resolveReport);
        return builder.build();
    }

    private void populateWithOrganisation(ResolveResult.Builder builder, ResolveReport resolveReport) {
        ModuleDescriptor moduleDescriptor = resolveReport.getModuleDescriptor();
        ModuleRevisionId moduleRevisionId = moduleDescriptor.getModuleRevisionId();
        String organisation = moduleRevisionId.getOrganisation();
        builder.setOrganisation(organisation);
    }

    private void populateWithDependencies(ResolveResult.Builder builder, ResolveReport resolveReport) {
        processArtifactsReports(builder, resolveReport);
        processUnresolvedDependencies(builder, resolveReport);
    }

    private void processArtifactsReports(ResolveResult.Builder builder, ResolveReport resolveReport) {
        for (ArtifactDownloadReport artifactDownloadReport : resolveReport.getAllArtifactsReports()) {
            Artifact artifact = artifactDownloadReport.getArtifact();
            ArtifactType artifactType = getType(artifact.getType());
            if (artifactType == null) {
                continue;
            }
            ModuleRevisionId moduleRevisionId = artifact.getModuleRevisionId();
            ModuleId moduleId = moduleRevisionId.getModuleId();
            String organisation = moduleId.getOrganisation();
            String module = moduleId.getName();
            String revision = moduleRevisionId.getRevision();
            Scope scope = Scope.COMPILE;
            File localFile = artifactDownloadReport.getLocalFile();
            if (localFile == null) {
                builder.addUnresolvedDependency(new UnresolvedDependency(organisation, module, revision,
                        artifactType, scope));
            } else {
                builder.addResolvedDependency(new ResolvedLibraryDependency(organisation, module, revision,
                        artifactType, localFile, scope));
            }
        }
    }

    private void processUnresolvedDependencies(ResolveResult.Builder builder, ResolveReport resolveReport) {
        for (IvyNode ivyNode : resolveReport.getUnresolvedDependencies()) {
            ModuleRevisionId moduleRevisionId = ivyNode.getResolvedId();
            String type = moduleRevisionId.getAttribute("type");
            ArtifactType artifactType = getType(type == null ? "jar" : type);
            if (artifactType == null) {
                continue;
            }
            String organisation = moduleRevisionId.getOrganisation();
            ModuleId moduleId = moduleRevisionId.getModuleId();
            String module = moduleId.getName();
            String revision = moduleRevisionId.getRevision();
            Scope scope = Scope.COMPILE;
            builder.addUnresolvedDependency(new UnresolvedDependency(organisation, module, revision, artifactType, scope));
        }
    }

    private ArtifactType getType(String artifactType) {
        ArtifactType result = null;
        if ("jar".equalsIgnoreCase(artifactType))
                result = ArtifactType.CLASSES;
        else
        if ("source".equalsIgnoreCase(artifactType))
                result = ArtifactType.SOURCES;
        else
        if ("javadoc".equalsIgnoreCase(artifactType))
                result = ArtifactType.JAVADOCS;
        return result;
    }

    private void bindWatcher(Ivy ivy) {
        EventManager eventManager = ivy.getResolveEngine().getEventManager();
        final ivyplug.resolving.events.EventManager pluginEventManager = ivyplug.resolving.events.EventManager.getInstance();
        final Map<String, Long> downloadStats = new HashMap<String, Long>();
        eventManager.addIvyListener(new IvyListener() {

            public void progress(IvyEvent event) {
                if (event.getClass() == TransferEvent.class) {
                    handleTransferEvent((TransferEvent) event);
                } else if (event.getClass() == StartResolveDependencyEvent.class) {
                    handleStartResolveDependencyEvent((StartResolveDependencyEvent) event);
                } else if (event.getClass() == StartArtifactDownloadEvent.class) {
                    handleStartArtifactDownloadEvent((StartArtifactDownloadEvent) event);
                }
            }

            private void handleTransferEvent(TransferEvent event) {
                int eventType = event.getEventType();
                if (eventType == TransferEvent.TRANSFER_STARTED || eventType == TransferEvent.TRANSFER_PROGRESS) {
                    String artifactURI = event.getResource().getName();
                    Long downloaded = downloadStats.get(artifactURI);
                    downloaded = (downloaded == null ? 0 : downloaded) + event.getLength();
                    downloadStats.put(artifactURI, downloaded);
                    DownloadProgressEvent downloadProgressEvent = new DownloadProgressEvent(artifactURI, downloaded, event.getTotalLength());
                    pluginEventManager.fireEvent(downloadProgressEvent);
                } else if (eventType == TransferEvent.TRANSFER_ERROR || eventType == TransferEvent.TRANSFER_COMPLETED) {
                    String artifactURI = event.getResource().getName();
                    downloadStats.put(artifactURI, null);
                }
            }

            private void handleStartResolveDependencyEvent(StartResolveDependencyEvent event) {
                ModuleRevisionId dep = event.getDependencyDescriptor().getDependencyRevisionId();
                ResolveEvent resolveEvent = new ResolveEvent(dep.getOrganisation(), dep.getName(), dep.getRevision(), dep.getBranch());
                pluginEventManager.fireEvent(resolveEvent);
            }

            private void handleStartArtifactDownloadEvent(StartArtifactDownloadEvent event) {
                ArtifactRevisionId artifact = event.getArtifact().getId();
                ModuleRevisionId dep = artifact.getModuleRevisionId();
                StartingDownloadEvent startingDownloadEvent = new StartingDownloadEvent(dep.getOrganisation(),
                        dep.getName(), dep.getRevision(), dep.getBranch(), artifact.getName(), artifact.getExt(), artifact.getType());
                pluginEventManager.fireEvent(startingDownloadEvent);
            }
        });
    }

}