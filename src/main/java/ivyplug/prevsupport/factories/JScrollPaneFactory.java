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
package ivyplug.prevsupport.factories;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Used to make plugin compatible with IDEA <10.
 *
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 07.02.2011
 */
public class JScrollPaneFactory {

    private static Method factoryMethod;

    static {
        try {
            Class factoryClass = Class.forName("com.intellij.ui.ScrollPaneFactory");
            try {
                factoryMethod = factoryClass.getMethod("createScrollPane", Component.class);
            } catch (NoSuchMethodException e9) {
                try {
                    factoryMethod = factoryClass.getMethod("createScrollPane", JComponent.class);
                } catch (NoSuchMethodException e10) {
                    factoryMethod = null;
                }
            }
        } catch (ClassNotFoundException e) {
            factoryMethod = null;
        }
    }

    public static JScrollPane createJScrollPane(Component component) {
        if (factoryMethod == null) {
            return fallback(component);
        } else {
            try {
                return (JScrollPane) factoryMethod.invoke(null, component);
            } catch (IllegalAccessException e) {
                return fallback(component);
            } catch (InvocationTargetException e) {
                return fallback(component);
            }
        }
    }

    @SuppressWarnings({"UndesirableClassUsage"})
    private static JScrollPane fallback(Component component) {
        return new JScrollPane(component);
    }
}
