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
import javax.swing.tree.TreeModel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Used to make plugin compatible with IDEA <10.
 *
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 07.02.2011
 */
public class JTreeFactory {

    private static Constructor constructor;

    static {
        try {
            Class cls = Class.forName("com.intellij.ui.treeStructure.Tree");
            try {
                constructor = cls.getConstructor(TreeModel.class);
            } catch (NoSuchMethodException e) {
                constructor = null;
            }
        } catch (ClassNotFoundException e) {
            constructor = null;
        }
    }

    public static JTree createJTree(TreeModel model) {
        if (constructor == null) {
            return fallback(model);
        } else {
            try {
                return (JTree) constructor.newInstance(model);
            } catch (InstantiationException e) {
                return fallback(model);
            } catch (IllegalAccessException e) {
                return fallback(model);
            } catch (InvocationTargetException e) {
                return fallback(model);
            }
        }
    }

    @SuppressWarnings({"deprecation"})
    private static JTree fallback(TreeModel model) {
        return new com.intellij.util.ui.Tree(model);
    }
}
