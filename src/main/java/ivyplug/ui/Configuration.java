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
package ivyplug.ui;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 03.02.2011
 */
public abstract class Configuration {

    private File ivySettingsXMlFile;
    private List<File> propertyFiles = Collections.emptyList();
    private Map<String, String> customProperties = Collections.emptyMap();

    @Nullable
    public File getIvySettingsXMlFile() {
        return ivySettingsXMlFile;
    }

    public void setIvySettingsXMlFile(File ivySettingsXMlFile) {
        this.ivySettingsXMlFile = ivySettingsXMlFile;
    }

    public List<File> getPropertyFiles() {
        return propertyFiles;
    }

    public void setPropertyFiles(List<File> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public Map<String, String> getResolvedProperties() {
        Map<String, String> result = new HashMap<String, String>(customProperties);
        for (File file : getPropertyFiles()) {
            if (file.exists()) {
                try {
                    Reader reader = new FileReader(file);
                    try {
                        Properties properties = new Properties();
                        properties.load(reader);
                        for (Map.Entry<Object, Object> entry: properties.entrySet()){
                            result.put((String) entry.getKey(), (String) entry.getValue());
                        }
                    } finally {
                        reader.close();
                    }
                } catch (FileNotFoundException e) {
                    // todo: warn
                } catch (IOException e) {
                    // todo: warn
                }
            }
        }
        return result;
    }
}
