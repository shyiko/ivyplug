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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import ivyplug.adapters.ProjectComponentAdapter;
import ivyplug.bundles.IvyPlugBundle;
import ivyplug.facade.DefaultEventManager;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.core.event.IvyListener;
import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;
import org.apache.ivy.core.event.resolve.StartResolveDependencyEvent;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.util.DefaultMessageLogger;

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

    public void configure(String module, File ivySettingXml) throws IvyException {
        Ivy ivy = getIvy(module);
        ivy.popContext();
        try {
            ivy.configure(ivySettingXml);
        } catch (Exception e) {
            throw new IvyException(IvyPlugBundle.message("failed.configuration", ivySettingXml.getAbsolutePath()), e);
        }
    }

    public ResolveReport resolve(String module, File ivyXml) throws IvyException {
        Ivy ivy = getIvy(module);
        try {
            return ivy.resolve(ivyXml);
        } catch (Exception e) {
            throw new IvyException(IvyPlugBundle.message("failed.resolve", ivyXml.getAbsolutePath()), e);
        }
    }

    public void setVariables(String module, Map<String, String> variables) throws IvyException {
        Ivy ivy = getIvy(module);
        IvySettings settings = ivy.getSettings();
        settings.addAllVariables(variables, true);
    }

    public void bindWatcher(String module, final ProgressIndicator indicator) throws IvyException {
        Ivy ivy = getIvy(module);
        DefaultEventManager eventManager = (DefaultEventManager) ivy.getResolveEngine().getEventManager();
        eventManager.removeAllListeners();
        if (indicator == null)
            return;
        final Map<String, Long> downloadStats = new HashMap<String, Long>();
        eventManager.addIvyListener(new IvyListener() {
            public void progress(IvyEvent event) {
                if (event.getClass() == TransferEvent.class) {
                    TransferEvent e = (TransferEvent) event;
                    if (e.getEventType() == TransferEvent.TRANSFER_STARTED || e.getEventType() == TransferEvent.TRANSFER_PROGRESS) {
                        String artifactURI = e.getResource().getName();
                        Long downloaded = downloadStats.get(artifactURI);
                        downloaded = (downloaded == null ? 0 : downloaded) + e.getLength();
                        downloadStats.put(artifactURI, downloaded);
                        indicator.setText(IvyPlugBundle.message("artifact.downloading.message", artifactURI, downloaded / 1024, e.getTotalLength() / 1024));
                    }
                } else
                if (event.getClass() == StartResolveDependencyEvent.class) {
                    StartResolveDependencyEvent e = (StartResolveDependencyEvent) event;
                    indicator.setText(IvyPlugBundle.message("resolving.dependency.message", e.getDependencyDescriptor().getDependencyRevisionId()));
                } else
                if (event.getClass() == StartArtifactDownloadEvent.class) {
                    StartArtifactDownloadEvent e = (StartArtifactDownloadEvent) event;
                    indicator.setText(IvyPlugBundle.message("pom.downloading.message", e.getArtifact().getId()));
                }
            }
        });
    }

    private Ivy getIvy(String module) throws IvyException {
        Ivy result = moduleIvyMap.get(module);
        if (result == null) {
            result = new Ivy();
            result.setEventManager(new DefaultEventManager());
            result.bind();
            ((DefaultEventManager) result.getEventManager()).removeAllListeners();
            result.getLoggerEngine().setDefaultLogger(new DefaultMessageLogger(-1));
            try {
                result.configureDefault();
            } catch (Exception e) {
                throw new IvyException(IvyPlugBundle.message("failed.to.load.default.ivysettings.xml"), e);
            }
            moduleIvyMap.put(module, result);
        }
        return result;
    }
}
