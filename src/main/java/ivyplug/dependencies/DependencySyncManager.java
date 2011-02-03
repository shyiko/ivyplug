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
import ivyplug.adapters.ModuleComponentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 29.01.2011
 */
public class DependencySyncManager extends ModuleComponentAdapter {

    private static final String LIBRARY_PREFIX = "Ivy: ";

    private Project project;
    private LibraryTablesRegistrar libraryTablesRegistrar;
    private Module module;
    private Map<String, List<Dependency>> dependencies;
    private Map<String, Module> modules;
    private Application application;

    public DependencySyncManager(Module module) {
        super(module);
        this.module = module;
        project = module.getProject();
        libraryTablesRegistrar = LibraryTablesRegistrar.getInstance();
        dependencies = new HashMap<String, List<Dependency>>();
        modules = new HashMap<String, Module>();
        application = ApplicationManager.getApplication();
    }

    @NotNull
    public String getComponentName() {
        return "DependencySyncManager";
    }

    public void addArtifactDependency(String org, String module, String rev, DependencyType type, File file) {
        String key = getKey(org, module, rev);
        List<Dependency> dependencyList = dependencies.get(key);
        if (dependencyList == null) {
            dependencyList = new LinkedList<Dependency>();
            dependencies.put(key, dependencyList);
        }
        dependencyList.add(new Dependency(type, file));
    }

    public void addModuleDependency(Module module) {
        modules.put(module.getName(), module);
    }

    public void commit() {

        // todo: refactor all this crap
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                application.runWriteAction(new Runnable() {

                    public void run() {

                        LibraryTable libraryTable = libraryTablesRegistrar.getLibraryTable(project);
                        for (Map.Entry<String, List<Dependency>> entry : dependencies.entrySet()) {
                            Library library = libraryTable.getLibraryByName(entry.getKey());
                            if (library == null) {
                                library = libraryTable.createLibrary(entry.getKey());
                            }
                            merge(library, entry.getValue());
                        }
                        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
                        ModifiableRootModel modifiableRootModel = moduleRootManager.getModifiableModel();

                        List<Library> libraries = new LinkedList<Library>();
                        for (OrderEntry orderEntry : modifiableRootModel.getOrderEntries()) {
                            if (orderEntry instanceof LibraryOrderEntry) {
                                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                                libraries.add(libraryOrderEntry.getLibrary());
                            } else if (orderEntry instanceof ModuleOrderEntry) {
                                ModuleOrderEntry moduleOrderEntry = (ModuleOrderEntry) orderEntry;
                                modules.remove(moduleOrderEntry.getModuleName());
                            }
                        }

                        try {
                            LibraryTable moduleLibraryTable = modifiableRootModel.getModuleLibraryTable();
                            Set<String> keys = dependencies.keySet();
                            for (Library library : libraries) {
                                if (!isGenerated(library))
                                    continue;
                                boolean keyWasPresent = keys.remove(library.getName());
                                if (!keyWasPresent)
                                    moduleLibraryTable.removeLibrary(library);
                            }
                            for (String key : dependencies.keySet()) {
                                Library library = libraryTable.getLibraryByName(key);
                                modifiableRootModel.addLibraryEntry(library);
                            }
                            for (Module projectModule : modules.values()) {
                                modifiableRootModel.addModuleOrderEntry(projectModule);
                            }


                        } finally {
                            modifiableRootModel.commit();
                        }
                    }
                });
            }
        });
    }

    private boolean isGenerated(Library library) {
        String name = library.getName();
        return name != null && name.startsWith(LIBRARY_PREFIX);
    }

    private String getKey(String org, String module, String rev) {
        return String.format("%s%s:%s:%s", LIBRARY_PREFIX, org, module, rev);
    }

    private void merge(Library library, List<Dependency> dependencies) {
        Library.ModifiableModel modifiableModel = null;
        try {
            String[] classes = library.getUrls(OrderRootType.CLASSES);
            String[] sources = library.getUrls(OrderRootType.SOURCES);
            String[] javadocs = library.getUrls(JavadocOrderRootType.getInstance());
            for (Dependency dependency : dependencies) {
                String dependencyURI = toURI(dependency.file);
                String[] searchScope = null;
                OrderRootType destType = null;
                switch(dependency.type) {
                    case CLASSES:
                        searchScope = classes;
                        destType = OrderRootType.CLASSES;
                        break;
                    case SOURCES:
                        searchScope = sources;
                        destType = OrderRootType.SOURCES;
                        break;
                    case JAVADOCS:
                        searchScope = javadocs;
                        destType = JavadocOrderRootType.getInstance();
                        break;
                }
                for (String existingDependency : searchScope) {
                    if (existingDependency.equals(dependencyURI)) {
                        return;
                    }
                }
                if (modifiableModel == null)
                    modifiableModel = library.getModifiableModel();
                modifiableModel.addRoot(dependencyURI, destType);
            }
        } finally {
            if (modifiableModel != null)
                modifiableModel.commit();
        }
    }

    private String toURI(File file) {
        String result = file.getAbsolutePath();
        if (file.getName().toLowerCase().endsWith(".jar")) {
            result = "jar://" + result + "!/";
        }
        return result;
    }

    private class Dependency {

        private DependencyType type;
        private File file;

        private Dependency(DependencyType type, File file) {
            this.type = type;
            this.file = file;
        }
    }
}
