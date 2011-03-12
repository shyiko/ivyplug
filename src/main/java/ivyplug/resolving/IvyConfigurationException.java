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

import ivyplug.bundles.IvyPlugBundle;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 12.02.2011
 */
public class IvyConfigurationException extends ResolveException {

    private String ivySettingsFile;

    public IvyConfigurationException(Throwable cause) {
        this(null, cause);
    }

    public IvyConfigurationException(@Nullable String ivySettingsFile, Throwable cause) {
        super(cause);
        this.ivySettingsFile = ivySettingsFile;
    }

    @Nullable
    public String getIvySettingsFile() {
        return ivySettingsFile;
    }

    @Override
    public String getMessage() {
        return ivySettingsFile == null ?
               IvyPlugBundle.message("resolve.default.configuration.exception.message") :
               IvyPlugBundle.message("resolve.resolving.exception.message", ivySettingsFile);
    }
}
