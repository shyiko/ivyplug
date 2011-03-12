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
package ivyplug.resolving.dependencies;

import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 03.02.2011
 */
public class ResolvedLibraryDependency extends ResolvedDependency {

    private String org;
    private String module;
    private String rev;
    private ArtifactType artifactType;
    private File file;
    private Scope scope;

    public ResolvedLibraryDependency(String org, String module, String rev, ArtifactType artifactType, File file, Scope scope) {
        super(DependencyType.LIBRARY);
        this.org = org;
        this.module = module;
        this.rev = rev;
        this.artifactType = artifactType;
        this.file = file;
        this.scope = scope;
    }

    public String getOrg() {
        return org;
    }

    public String getModule() {
        return module;
    }

    public String getRev() {
        return rev;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public File getFile() {
        return file;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResolvedLibraryDependency that = (ResolvedLibraryDependency) o;

        if (artifactType != that.artifactType) {
            return false;
        }
        if (!file.equals(that.file)) {
            return false;
        }
        if (!module.equals(that.module)) {
            return false;
        }
        if (!org.equals(that.org)) {
            return false;
        }
        if (!rev.equals(that.rev)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = org.hashCode();
        result = 31 * result + module.hashCode();
        result = 31 * result + rev.hashCode();
        result = 31 * result + artifactType.hashCode();
        result = 31 * result + file.hashCode();
        return result;
    }
}