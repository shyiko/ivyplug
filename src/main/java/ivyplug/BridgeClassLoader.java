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
package ivyplug;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 11.03.2011
 */
public class BridgeClassLoader extends URLClassLoader {

    private ParentClassLoader parent;

    public BridgeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);
        this.parent = new ParentClassLoader(parent);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class result = findLoadedClass(name);
        if (result == null) {
            try {
                result = findClass(name);
            } catch (ClassNotFoundException ex) {
                if (parent != null) {
                    result = parent.findLoadedClass_(name);
                    if (result == null) {
                        result = parent.loadClass(name);
                    }
                }
            }
        }
        return result;
    }

    public static class ParentClassLoader extends ClassLoader {

        public ParentClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> findLoadedClass_(String name) {
            return findLoadedClass(name);
        }
    }
}