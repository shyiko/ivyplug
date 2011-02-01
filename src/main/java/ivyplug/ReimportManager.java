package ivyplug;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import ivyplug.dependencies.DependencySyncManager;
import ivyplug.dependencies.DependencyType;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;

import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class ReimportManager {

    public List<IvyModule> removeProjectModulesFromArtifactsReports(Map<String, IvyModule> projectModules,
                                                                     List<ArtifactDownloadReport> artifactDownloadReports) {
        List<IvyModule> result = new ArrayList<IvyModule>();
        for (int i = artifactDownloadReports.size() - 1; i > -1; i--) {
            ArtifactDownloadReport artifactDownloadReport = artifactDownloadReports.get(i);
            ModuleRevisionId moduleRevisionId = artifactDownloadReport.getArtifact().getModuleRevisionId();
            String projectModule = moduleRevisionId.getOrganisation() + ":" + moduleRevisionId.getName();
            IvyModule module = projectModules.get(projectModule);
            if (module != null) {
                result.add(module);
                artifactDownloadReports.remove(artifactDownloadReport);
            }
        }
        return result;
    }

    public void informAboutFailedDependencies(Module module, List<ArtifactDownloadReport> failedDependencies) {
        Project project = module.getProject();
        MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
        List<Map.Entry<ErrorTreeElementKind, String[]>> messages = new ArrayList<Map.Entry<ErrorTreeElementKind, String[]>>();
        for (ArtifactDownloadReport failedDependency : failedDependencies) {
            messages.add(new AbstractMap.SimpleEntry<ErrorTreeElementKind, String[]>(ErrorTreeElementKind.ERROR,
                    toMessage(failedDependency)));
        }
        messagesProjectComponent.open(module, messages);
/*
        StringBuilder sb = new StringBuilder("Following dependencies are missing:\n");
        for (Object problemMessage : failedDependencies) {
            sb.append("\t").append(problemMessage).append("\n");
        }
        Messages.showErrorDialog(sb.toString(), "Failed to resolve " + module.getName());
*/
    }

    public void addArtifactDependencies(Module module, List<ArtifactDownloadReport> artifactDownloadReports) {
        DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
        for (ArtifactDownloadReport artifactDownloadReport : artifactDownloadReports) {
            Artifact artifact = artifactDownloadReport.getArtifact();
            DependencyType type = getType(artifact);
            if (type == null)
                continue;
            ModuleRevisionId moduleRevisionId = artifact.getModuleRevisionId();
            dependencySyncManager.addArtifactDependency(moduleRevisionId.getOrganisation(), moduleRevisionId.getName(),
                    moduleRevisionId.getRevision(), type, artifactDownloadReport.getLocalFile());
        }
    }

    public void addModuleDependencies(Module module, Module dependency) {
        DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
        dependencySyncManager.addModuleDependency(dependency);
    }

    public void commitChanges(Module module) {
        DependencySyncManager dependencySyncManager = module.getComponent(DependencySyncManager.class);
        dependencySyncManager.commit();
    }

    private String[] toMessage(ArtifactDownloadReport report) {
        String downloadDetails = report.getDownloadDetails();
        String[] result;
        if (downloadDetails == null ||
            downloadDetails.equals(ArtifactDownloadReport.MISSING_ARTIFACT) ||
            downloadDetails.trim().isEmpty()) {
            result = new String[1];
        } else {
            result = new String[2];
            result[1] = "Reason: " + downloadDetails;
        }
        result[0] = "Failed to locate dependency " + report.getArtifact();
        return result;
    }

    private DependencyType getType(Artifact artifact) {
        DependencyType result = null;
        String artifactType = artifact.getType();
        if ("jar".equalsIgnoreCase(artifactType))
                result = DependencyType.CLASSES;
        else
        if ("source".equalsIgnoreCase(artifactType))
                result = DependencyType.SOURCES;
        else
        if ("javadoc".equalsIgnoreCase(artifactType))
                result = DependencyType.JAVADOCS;
        return result;
    }

    public static class IvyModule {

        private Module module;
        private List<ArtifactDownloadReport> failedArtifactsReports;
        private List<ArtifactDownloadReport> successfulArtifacts;

        public IvyModule(Module module) {
            this(module, null);
        }

        public IvyModule(Module module, ResolveReport resolveReport) {
            this.module = module;
            failedArtifactsReports = new ArrayList<ArtifactDownloadReport>();
            successfulArtifacts = new ArrayList<ArtifactDownloadReport>();
            if (resolveReport != null) {
                for (ArtifactDownloadReport artifactDownloadReport : resolveReport.getAllArtifactsReports()) {
                    if (artifactDownloadReport.getLocalFile() == null) {
                        failedArtifactsReports.add(artifactDownloadReport);
                    } else {
                        successfulArtifacts.add(artifactDownloadReport);
                    }
                }
                for (IvyNode ivyNode : resolveReport.getUnresolvedDependencies()) {
                    ModuleRevisionId resolvedId = ivyNode.getResolvedId();
                    String type = resolvedId.getAttribute("type");
                    String ext = resolvedId.getAttribute("ext");
                    Artifact artifact = new DefaultArtifact(resolvedId, new Date(ivyNode.getPublication()),
                            resolvedId.getName(), type == null ? "jar" : type, ext == null ? "ext" : ext);
                    ArtifactDownloadReport artifactDownloadReport = new ArtifactDownloadReport(artifact);
                    artifactDownloadReport.setDownloadStatus(DownloadStatus.FAILED);
                    failedArtifactsReports.add(artifactDownloadReport);
                }
            }
        }

        public Module getModule() {
            return module;
        }

        public List<ArtifactDownloadReport> getFailedArtifactsReports() {
            return failedArtifactsReports;
        }

        public List<ArtifactDownloadReport> getSuccessfulArtifactsReports() {
            return successfulArtifacts;
        }
    }

}
