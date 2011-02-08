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
package ivyplug.prevsupport;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A copy of com.intellij.ideaapi.fileChooser.FileTypeDescriptor, used to make plugin
 * compatible with IDEA <10.
 *
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 07.02.2011
 */
public class FileTypeDescriptor extends FileChooserDescriptor {

    private final ArrayList<String> ext;

    public FileTypeDescriptor(String title, @NotNull String... extensions) {
        super(true, false, false, true, false, false);
        assert extensions.length > 0 : "There should be at least one extension";
        ext = new ArrayList<String>(Arrays.asList(extensions));
        setTitle(title);
    }

    @Override
    public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        final String ex = file.getExtension();
        return file.isDirectory() || (ex != null && ext.contains(ex.toLowerCase()));
    }

    @Override
    public boolean isFileSelectable(VirtualFile file) {
        return !file.isDirectory() && isFileVisible(file, true);
    }
}