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

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 10.02.2011
 */
public class ResolveContext {

    private Module moduleToResolve;

    public ResolveContext(@NotNull Module moduleToResolve) {
        this.moduleToResolve = moduleToResolve;
    }

    @NotNull
    public Module getModuleToResolve() {
        return moduleToResolve;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResolveContext that = (ResolveContext) o;

        if (!moduleToResolve.equals(that.moduleToResolve)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return moduleToResolve.hashCode();
    }
}
