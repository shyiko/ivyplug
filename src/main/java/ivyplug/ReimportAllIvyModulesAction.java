package ivyplug;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;
import ivyplug.bundles.IvyPlugBundle;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 31.01.2011
 */
public class ReimportAllIvyModulesAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(LangDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        httpConfigurable.setAuthenticator();
        new Task.Backgroundable(project, IvyPlugBundle.message("synchronizing.data"), false) {
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText(IvyPlugBundle.message("preparing.to.synchronize.ivy.dependencies"));
                indicator.setFraction(0.0);
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                ReimportManager reimportManager = new ReimportManager();
                Map<String, ReimportManager.IvyModule> ivyModules = getIvyModules(moduleManager, indicator);
                for (ReimportManager.IvyModule ivyModule : ivyModules.values()) {
                    List<ArtifactDownloadReport> failedArtifactsReports = ivyModule.getFailedArtifactsReports();
                    List<ArtifactDownloadReport> successfulArtifactsReports = ivyModule.getSuccessfulArtifactsReports();
                    // remove project modules from ivy reports and add them as module dependencies
                    List<ReimportManager.IvyModule> projectModules = reimportManager.removeProjectModulesFromArtifactsReports(ivyModules, failedArtifactsReports);
                    projectModules.addAll(reimportManager.removeProjectModulesFromArtifactsReports(ivyModules, successfulArtifactsReports));
                    for (ReimportManager.IvyModule projectModule : projectModules) {
                        reimportManager.addModuleDependencies(ivyModule.getModule(), projectModule.getModule());
                    }
                    // add artifact dependencies
                    reimportManager.addArtifactDependencies(ivyModule.getModule(), successfulArtifactsReports);
                    // commit all changes
                    reimportManager.commitChanges(ivyModule.getModule());
                    if (!failedArtifactsReports.isEmpty())
                        reimportManager.informAboutFailedDependencies(ivyModule.getModule(), failedArtifactsReports);
                }
                indicator.setFraction(1.0);
            }
        }.queue();
    }

    private Map<String, ReimportManager.IvyModule> getIvyModules(ModuleManager moduleManager, ProgressIndicator indicator) {
        Map<String, ReimportManager.IvyModule> result = new HashMap<String, ReimportManager.IvyModule>();
        Module[] modules = moduleManager.getModules();
        for (Module projectModule : modules) {
            MessagesProjectComponent messagesProjectComponent = projectModule.getProject().getComponent(MessagesProjectComponent.class);
            messagesProjectComponent.close(projectModule);
            IvyModuleComponent ivyModuleComponent = projectModule.getComponent(IvyModuleComponent.class);
            if (ivyModuleComponent.isIvyModule()) {
                try {
                    indicator.setText(IvyPlugBundle.message("preparing.to.synchronize.module.ivy.dependencies", projectModule.getName()));
                    ResolveReport resolveReport = ivyModuleComponent.resolve(indicator);
                    ModuleRevisionId moduleRevisionId = resolveReport.getModuleDescriptor().getModuleRevisionId();
                    result.put(moduleRevisionId.getOrganisation() + ":" + moduleRevisionId.getName(),
                            new ReimportManager.IvyModule(projectModule, resolveReport));
                } catch (IvyException ex) {
                    messagesProjectComponent.open(projectModule, ErrorTreeElementKind.ERROR, new String[] {
                            ex.getMessage(), IvyPlugBundle.message("ivyexception.reason", ex.getCause().getMessage())
                    });
/*
                    Messages.showErrorDialog(ex.getMessage() + "\n" + "Reason: " + ex.getCause().getMessage(),
                            "Failed to resolve " + projectModule.getName());
*/
                }
            }
        }
        return result;
    }
}
