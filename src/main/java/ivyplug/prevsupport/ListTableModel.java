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

import com.intellij.util.ui.*;

import java.util.Collection;
import java.util.List;

/**
 * Modified version of com.intellij.util.ui.ListTableModel, used to make plugin
 * compatible with IDEA <10.
 *
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 07.02.2011
 */
public class ListTableModel<Item> extends com.intellij.util.ui.ListTableModel<Item> {

    private List<Item> myItems;

    public ListTableModel(ColumnInfo... columnInfos) {
        super(columnInfos);
    }

    @Override
    public void setItems(List<Item> list) {
        super.setItems(list);
        myItems = list;
    }

    public void addRow(Item item) {
        myItems.add(item);
        fireTableRowsInserted(myItems.size() - 1, myItems.size() - 1);
    }

    public void addRows(final Collection<Item> items) {
        myItems.addAll(items);
        fireTableRowsInserted(myItems.size() - items.size(), myItems.size() - 1);
    }
}

