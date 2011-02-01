package ivyplug;

import com.intellij.ide.errorTreeView.ErrorTreeElementKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 28.01.2011
 */
public class ReimportIvyModuleAction extends AnAction {

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Module module = e.getData(LangDataKeys.MODULE);
        String name = "Reimport Ivy Module";
        if (module != null)
            name += " \"" + module.getName() + "\"";
        presentation.setText(name);
    }

    public void actionPerformed(AnActionEvent e) {
        final Module module = e.getData(LangDataKeys.MODULE);
        if (module == null)
            return;
        HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        httpConfigurable.setAuthenticator();
        new Task.Backgroundable(module.getProject(), "Synchronizing data", false) {
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Importing Ivy module \"" + module.getName() + "\"...");
                indicator.setFraction(0.0);
                IvyModuleComponent ivyModuleComponent = module.getComponent(IvyModuleComponent.class);
                if (ivyModuleComponent.isIvyModule()) {
                    Project project = module.getProject();
                    MessagesProjectComponent messagesProjectComponent = project.getComponent(MessagesProjectComponent.class);
                    messagesProjectComponent.close(module);
                    try {
                        ReimportManager reimportManager = new ReimportManager();
                        ModuleManager moduleManager = ModuleManager.getInstance(project);
                        Map<String, ReimportManager.IvyModule> ivyModules = getIvyModules(moduleManager);
                        ResolveReport resolveReport = ivyModuleComponent.resolve();
                        ReimportManager.IvyModule ivyModule = new ReimportManager.IvyModule(module, resolveReport);
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
                    } catch (IvyException ex) {
                        messagesProjectComponent.open(module, ErrorTreeElementKind.ERROR, new String[] {
                                ex.getMessage(),
                                 "Reason: " + ex.getCause().getMessage()
                        });
/*
                Messages.showErrorDialog(ex.getMessage() + "\n" + "Reason: " + ex.getCause().getMessage(),
                        "Failed to resolve " + module.getName());
*/
                    }
                }
                indicator.setFraction(1.0);
            }
        }.queue();
    }

    private Map<String, ReimportManager.IvyModule> getIvyModules(ModuleManager moduleManager) {
        Map<String, ReimportManager.IvyModule> result = new HashMap<String, ReimportManager.IvyModule>();
        for (Module projectModule : moduleManager.getModules()) {
            IvyModuleComponent ivyModuleComponent = projectModule.getComponent(IvyModuleComponent.class);
            String org = ivyModuleComponent.getOrg();
            if (ivyModuleComponent.isIvyModule() && org != null) {
                result.put(org + ":" + projectModule.getName(), new ReimportManager.IvyModule(projectModule));
            }
        }
        return result;
    }

}