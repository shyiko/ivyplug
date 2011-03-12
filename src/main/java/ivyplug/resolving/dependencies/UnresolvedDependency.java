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

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 11.03.2011
 */
public class UnresolvedDependency extends Dependency {

    private String org;
    private String module;
    private String rev;
    private ArtifactType artifactType;
    private Scope scope;

    public UnresolvedDependency(String org, String module, String rev, ArtifactType artifactType, Scope scope) {
        this.org = org;
        this.module = module;
        this.rev = rev;
        this.artifactType = artifactType;
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

        UnresolvedDependency that = (UnresolvedDependency) o;

        if (artifactType != that.artifactType) {
            return false;
        }
        if (module != null ? !module.equals(that.module) : that.module != null) {
            return false;
        }
        if (org != null ? !org.equals(that.org) : that.org != null) {
            return false;
        }
        if (rev != null ? !rev.equals(that.rev) : that.rev != null) {
            return false;
        }
        if (scope != that.scope) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = org != null ? org.hashCode() : 0;
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (rev != null ? rev.hashCode() : 0);
        result = 31 * result + (artifactType != null ? artifactType.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }
}
