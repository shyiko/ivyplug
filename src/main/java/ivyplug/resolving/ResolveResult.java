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
package ivyplug.resolving;

import ivyplug.resolving.dependencies.ResolvedDependency;
import ivyplug.resolving.dependencies.UnresolvedDependency;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 09.02.2011
 */
public class ResolveResult {

    private String organisation;
    private List<ResolvedDependency> resolvedDependencies;
    private List<UnresolvedDependency> unresolvedDependencies;

    private ResolveResult() {
        resolvedDependencies = new ArrayList<ResolvedDependency>();
        unresolvedDependencies = new ArrayList<UnresolvedDependency>();
    }

    public String getOrganisation() {
        return organisation;
    }

    public List<ResolvedDependency> getResolvedDependencies() {
        return resolvedDependencies;
    }

    public List<UnresolvedDependency> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }

    public static class Builder {

        private ResolveResult resolveResult = new ResolveResult();

        public ResolveResult build() {
            return resolveResult;
        }

        public void setOrganisation(String organisation) {
            resolveResult.organisation = organisation;
        }

        public void addResolvedDependency(ResolvedDependency resolvedDependency) {
            resolveResult.resolvedDependencies.add(resolvedDependency);
        }

        public void addUnresolvedDependency(UnresolvedDependency unresolvedDependency) {
            resolveResult.unresolvedDependencies.add(unresolvedDependency);
        }
    }
}
