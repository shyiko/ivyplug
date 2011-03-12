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
package ivyplug.dependencies;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.util.ui.UIUtil;
import ivyplug.adapters.ModuleComponentAdapter;
import ivyplug.resolving.dependencies.ResolvedLibraryDependency;
import ivyplug.resolving.dependencies.ResolvedModuleDependency;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 29.01.2011
 */
public class DependencySyncManager extends ModuleComponentAdapter {

    public static final String LIBRARY_PREFIX = "Ivy: ";

    private Application application;
    private Project project;
    private LibraryTablesRegistrar libraryTablesRegistrar;
    private ModuleRootManager moduleRootManager;
    private Set<ResolvedLibraryDependency> libraryDependencies;
    private Set<ResolvedModuleDependency> moduleDependencies;

    public DependencySyncManager(Module module) {
        super(module);
        application = ApplicationManager.getApplication();
        project = module.getProject();
        libraryTablesRegistrar = LibraryTablesRegistrar.getInstance();
        moduleRootManager = ModuleRootManager.getInstance(module);
        libraryDependencies = new HashSet<ResolvedLibraryDependency>();
        moduleDependencies = new HashSet<ResolvedModuleDependency>();
    }

    public void addResolvedLibraryDependency(ResolvedLibraryDependency resolvedLibraryDependency) {
        libraryDependencies.add(resolvedLibraryDependency);
    }

    public void addResolvedModuleDependency(ResolvedModuleDependency resolvedModuleDependency) {
        moduleDependencies.add(resolvedModuleDependency);
    }

    public void commit(final boolean removeOldLibraries) {
        final Set<ResolvedLibraryDependency> libraryDependenciesToMerge = libraryDependencies;
        final Set<ResolvedModuleDependency> moduleDependenciesToMerge = moduleDependencies;
        libraryDependencies = new HashSet<ResolvedLibraryDependency>();
        moduleDependencies = new HashSet<ResolvedModuleDependency>();
        UIUtil.invokeLaterIfNeeded(new Runnable() {

            public void run() {
                application.runWriteAction(new Runnable() {

                    public void run() {
                        ModifiableRootModel modifiableModuleModel = moduleRootManager.getModifiableModel();
                        try {
                            if (removeOldLibraries) {
                                removeOldLibraries(modifiableModuleModel);
                            }
                            mergeLibraryDependencies(modifiableModuleModel, libraryDependenciesToMerge);
                            mergeModuleDependencies(modifiableModuleModel, moduleDependenciesToMerge);
                            modifiableModuleModel.commit();
                        } finally {
                            if (modifiableModuleModel.isWritable()) {
                                modifiableModuleModel.dispose();
                            }
                        }
                    }
                });
            }
        });
    }

    private void removeOldLibraries(ModifiableRootModel modifiableModuleModel) {
        for (OrderEntry orderEntry : modifiableModuleModel.getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                String libraryName = libraryOrderEntry.getLibraryName();
                if (libraryName != null && libraryName.startsWith(LIBRARY_PREFIX)) {
                    modifiableModuleModel.removeOrderEntry(libraryOrderEntry);
                }
            }
        }
    }

    private void mergeLibraryDependencies(ModifiableRootModel modifiableModuleModel, Set<ResolvedLibraryDependency> libraryDependenciesToCommit) {
        LibraryTable projectLibraryTable = libraryTablesRegistrar.getLibraryTable(project);
        Map<String, LibraryOrderEntry> moduleLibraries = getModuleLibraries(modifiableModuleModel);
        for (ResolvedLibraryDependency resolvedLibraryDependency : libraryDependenciesToCommit) {
            String libraryName = getLibraryName(resolvedLibraryDependency);
            Library library = projectLibraryTable.getLibraryByName(libraryName);
            if (library == null) {
                library = projectLibraryTable.createLibrary(libraryName);
            }
            LibraryOrderEntry libraryOrderEntry = moduleLibraries.get(libraryName);
            if (libraryOrderEntry == null) {
                libraryOrderEntry = modifiableModuleModel.addLibraryEntry(library);
                moduleLibraries.put(libraryName, libraryOrderEntry);
            }
            libraryOrderEntry.setScope(getDependencyScope(resolvedLibraryDependency));
            mergeResolvedLibraryDependency(library, resolvedLibraryDependency);
        }
    }

    private Map<String, LibraryOrderEntry> getModuleLibraries(ModifiableRootModel modifiableModuleModel) {
        Map<String, LibraryOrderEntry> result = new HashMap<String, LibraryOrderEntry>();
        for (OrderEntry orderEntry : modifiableModuleModel.getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                String libName = libraryOrderEntry.getLibraryName();
                if (libName != null) {
                    result.put(libName, libraryOrderEntry);
                }
            }
        }
        return result;
    }

    private String getLibraryName(ResolvedLibraryDependency ResolvedLibraryDependency) {
        return String.format("%s%s:%s:%s", LIBRARY_PREFIX,
                ResolvedLibraryDependency.getOrg(), ResolvedLibraryDependency.getModule(), ResolvedLibraryDependency.getRev());
    }

    private DependencyScope getDependencyScope(ResolvedLibraryDependency dependency) {
        switch (dependency.getScope()) {
            case COMPILE:
                return DependencyScope.COMPILE;
            case PROVIDED:
                return DependencyScope.PROVIDED;
            case RUNTIME:
                return DependencyScope.RUNTIME;
            case TEST:
                return DependencyScope.TEST;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void mergeModuleDependencies(ModifiableRootModel modifiableModuleModel, Set<ResolvedModuleDependency> moduleDependenciesToCommit) {
        Module[] moduleDependencies = modifiableModuleModel.getModuleDependencies();
        Set<String> alreadyBoundModules = new HashSet<String>(moduleDependencies.length);
        for (Module module : moduleDependencies) {
            alreadyBoundModules.add(module.getName());
        }
        for (ResolvedModuleDependency ResolvedModuleDependency : moduleDependenciesToCommit) {
            Module module = ResolvedModuleDependency.getModule();
            if (!alreadyBoundModules.contains(module.getName())) {
                modifiableModuleModel.addModuleOrderEntry(module);
            }
        }
    }

    private void mergeResolvedLibraryDependency(Library library, ResolvedLibraryDependency dependency) {
        Library.ModifiableModel modifiableModel = null;
        try {
            String[] searchScope;
            OrderRootType orderRootType;
            switch (dependency.getArtifactType()) {
                case CLASSES:
                    searchScope = library.getUrls(OrderRootType.CLASSES);
                    orderRootType = OrderRootType.CLASSES;
                    break;
                case SOURCES:
                    searchScope = library.getUrls(OrderRootType.SOURCES);
                    orderRootType = OrderRootType.SOURCES;
                    break;
                case JAVADOCS:
                    searchScope = library.getUrls(JavadocOrderRootType.getInstance());
                    orderRootType = JavadocOrderRootType.getInstance();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            String dependencyURI = toURI(dependency.getFile());
            for (String existingDependency : searchScope) {
                if (existingDependency.equals(dependencyURI)) {
                    return;
                }
            }
            modifiableModel = library.getModifiableModel();
            modifiableModel.addRoot(dependencyURI, orderRootType);
        } finally {
            if (modifiableModel != null) {
                modifiableModel.commit();
            }
        }
    }

    private String toURI(File file) {
        String result = file.getAbsolutePath();
        if (file.getName().toLowerCase().endsWith(".jar")) {
            result = "jar://" + result + "!/";
        }
        return result;
    }
}