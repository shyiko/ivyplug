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
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import ivyplug.adapters.ProjectComponentAdapter;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 04.02.2011
 */
public class ProjectDependenciesManager extends ProjectComponentAdapter {

    private Application application;
    private LibraryTable libraryTable;
    private ModuleManager moduleManager;

    public ProjectDependenciesManager(Project project) {
        super(project);
        application = ApplicationManager.getApplication();
        LibraryTablesRegistrar libraryTablesRegistrar = LibraryTablesRegistrar.getInstance();
        libraryTable = libraryTablesRegistrar.getLibraryTable(project);
        moduleManager = ModuleManager.getInstance(project);
    }

    public void removeUnusedLibraries() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                application.runWriteAction(new Runnable() {

                    public void run() {
                        Set<String> librariesUsedByModules = getLibrariesUsedByModules();

                        Library[] libraries = libraryTable.getLibraries();
                        for (int i = libraries.length - 1; i > -1; i--) {
                            Library library = libraries[i];
                            String projectLibraryName = library.getName();
                            if (projectLibraryName != null &&
                                    projectLibraryName.startsWith(DependencySyncManager.LIBRARY_PREFIX) &&
                                    !librariesUsedByModules.contains(projectLibraryName)) {
                                libraryTable.removeLibrary(library);
                            }
                        }
                    }
                });
            }
        });
    }

    private Set<String> getLibrariesUsedByModules() {
        Set<String> result = new HashSet<String>();
        for (Module module : moduleManager.getModules()) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            for (OrderEntry orderEntry : moduleRootManager.getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry) {
                    LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) orderEntry;
                    String moduleLibraryName = libraryOrderEntry.getLibraryName();
                    if (moduleLibraryName != null)
                        result.add(moduleLibraryName);
                }
            }
        }
        return result;
    }
}
