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
package ivyplug.ui.module;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import ivyplug.adapters.ModuleComponentAdapter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
@State(name = "IvyModuleConfigurationModuleComponent",
       storages = {@Storage(id = "other", file = "$MODULE_FILE$")}
)
public class IvyModuleConfigurationModuleComponent extends ModuleComponentAdapter implements PersistentStateComponent<Element> {

    private static final String USE_AUTO_DISCOVERY = "useAutoDiscovery";
    private static final String IVY_XML_FILE = "ivyXMLFile";
    private static final String IVY_SETTINGS_XML_FILE = "ivySettingsXMLFile";
    private static final String SETTINGS_ELEMENT_NAME = "ivyPlugSettings";
    private static final String PROPERTY_FILES_ELEMENT_NAME = "propertyFiles";
    private static final String PROPERTY_FILES_ATTRIBUTE_NAME = "attribute";
    private static final String CUSTOM_PROPERTIES_ELEMENT_NAME = "customProperties";
    private static final String CUSTOM_PROPERTIES_ATTRIBUTE_NAME = "attribute";
    private static final String CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME = "name";

    private final IvyModuleConfiguration ivyModuleConfiguration;

    public IvyModuleConfigurationModuleComponent(Module module) {
        super(module);
        ivyModuleConfiguration = new IvyModuleConfiguration(module);
    }

    public void loadState(Element state) {
        String useAutoDiscovery = state.getAttributeValue(USE_AUTO_DISCOVERY);
        ivyModuleConfiguration.setUseAutoDiscovery(useAutoDiscovery == null ||
                                          useAutoDiscovery.equalsIgnoreCase("true"));
        String ivyXML = state.getAttributeValue(IVY_XML_FILE);
        if (ivyXML != null)
            ivyModuleConfiguration.setIvyXMlFile(new File(ivyXML));
        String ivySettingsXML = state.getAttributeValue(IVY_SETTINGS_XML_FILE);
        if (ivySettingsXML != null)
            ivyModuleConfiguration.setIvySettingsXMlFile(new File(ivySettingsXML));
        loadPropertyFiles(state);
        loadCustomProperties(state);
    }

    public Element getState() {
        Boolean useAutoDiscovery = ivyModuleConfiguration.isUseAutoDiscovery();
        File ivyXMl = ivyModuleConfiguration.getIvyXMlFile();
        File ivySettingsXMl = ivyModuleConfiguration.getIvySettingsXMlFile();
        Element element = new Element(SETTINGS_ELEMENT_NAME);
        if (ivyXMl != null)
            element.setAttribute(IVY_XML_FILE, ivyXMl.getAbsolutePath());
        if (ivySettingsXMl != null)
            element.setAttribute(IVY_SETTINGS_XML_FILE, ivySettingsXMl.getAbsolutePath());
        element.setAttribute(USE_AUTO_DISCOVERY, useAutoDiscovery.toString());
        element.addContent(getPropertyFiles());
        element.addContent(getCustomProperties());
        return element;
    }

    public IvyModuleConfiguration getConfiguration() {
        return ivyModuleConfiguration;
    }

    private void loadPropertyFiles(Element state) {
        Element element = state.getChild(PROPERTY_FILES_ELEMENT_NAME);
        List<File> propertyFiles;
        if (element == null) {
            propertyFiles = Collections.emptyList();
        } else {
            List children = element.getChildren(PROPERTY_FILES_ATTRIBUTE_NAME);
            propertyFiles = new ArrayList<File>(children.size());
            for (Object attribute_ : children) {
                Element attribute = (Element) attribute_;
                propertyFiles.add(new File(attribute.getTextTrim()));
            }
        }
        ivyModuleConfiguration.setPropertyFiles(propertyFiles);
    }

    private void loadCustomProperties(Element state) {
        Element element = state.getChild(CUSTOM_PROPERTIES_ELEMENT_NAME);
        Map<String, String> customProperties;
        if (element == null) {
            customProperties = Collections.emptyMap();
        } else {
            List children = element.getChildren(CUSTOM_PROPERTIES_ATTRIBUTE_NAME);
            customProperties = new HashMap<String, String>(children.size());
            for (Object attribute_ : children) {
                Element attribute = (Element) attribute_;
                customProperties.put(attribute.getAttributeValue(CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME),
                        attribute.getTextTrim());
            }
        }
        ivyModuleConfiguration.setCustomProperties(customProperties);
    }

    private Element getPropertyFiles() {
        Element result = new Element(PROPERTY_FILES_ELEMENT_NAME);
        for (File file : ivyModuleConfiguration.getPropertyFiles()) {
            Element attribute = new Element(PROPERTY_FILES_ATTRIBUTE_NAME);
            attribute.setText(file.getAbsolutePath());
            result.addContent(attribute);
        }
        return result;
    }

    private Element getCustomProperties() {
        Element result = new Element(CUSTOM_PROPERTIES_ELEMENT_NAME);
        for (Map.Entry<String, String> entry: ivyModuleConfiguration.getCustomProperties().entrySet()){
            Element attribute = new Element(CUSTOM_PROPERTIES_ATTRIBUTE_NAME);
            attribute.setAttribute(CUSTOM_PROPERTIES_ATTRIBUTE_KEY_NAME, entry.getKey());
            attribute.setText(entry.getValue());
            result.addContent(attribute);
        }
        return result;
    }
}
