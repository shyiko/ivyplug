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
package ivyplug.ui.configuration;

import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import ivyplug.bundles.IvyPlugBundle;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">sshyiko</a>
 * @since 02.02.2011
 */
public class PropertiesPanel extends JPanel {

    private final JButton addButton = new JButton(IvyPlugBundle.message("button.add"));
    private final JButton removeButton = new JButton(IvyPlugBundle.message("button.remove"));
    private final TableView<Variable> tableView;
    private boolean modified;

    public PropertiesPanel() {
        final ListTableModel<Variable> tableViewModel = new ListTableModel<Variable>(
                new AbstractVariableColumn(IvyPlugBundle.message("column.name")) {

                    public String valueOf(Variable variable) {
                        return variable.name;
                    }

                    public void setValue(Variable variable, String value) {
                        variable.name = value;
                    }
                }, new AbstractVariableColumn(IvyPlugBundle.message("column.value")) {

                    public String valueOf(Variable variable) {
                        return variable.value;
                    }

                    public void setValue(Variable variable, String value) {
                        variable.value = value;
                    }
                });
        tableViewModel.setSortable(false);
        tableView = new TableView<Variable>(tableViewModel);
        tableView.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                setModified();
            }
        });
        JTable tableComponent = tableView.getComponent();
        tableComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tableViewModel.addRow(new Variable());
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] indicesAsc = tableView.getSelectedRows();
                Arrays.sort(indicesAsc);
                for (int i = indicesAsc.length - 1; i > -1; i--) {
                    tableViewModel.removeRow(indicesAsc[i]);
                }
            }
        });

        JTableHeader tableHeader = tableView.getTableHeader();
        FontMetrics fontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());
        TableColumn nameColumn = tableHeader.getColumnModel().getColumn(0);
        int preferredWidth = fontMetrics.stringWidth((String) nameColumn.getHeaderValue()) + 100;
        nameColumn.setWidth(preferredWidth);
        nameColumn.setPreferredWidth(preferredWidth);
        nameColumn.setMinWidth(preferredWidth);
        nameColumn.setMaxWidth(preferredWidth);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 100));
        setMinimumSize(new Dimension(100, 100));
        tableComponent.setBorder(BorderFactory.createEtchedBorder());
        JPanel propertyFilesWrapperPanel = new JPanel(new BorderLayout());
        propertyFilesWrapperPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        propertyFilesWrapperPanel.add(ScrollPaneFactory.createScrollPane(tableComponent), BorderLayout.CENTER);
        add(propertyFilesWrapperPanel, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 0), 0, 0);
        addButton.setSize(new Dimension(104, 25));
        addButton.setPreferredSize(new Dimension(104, 25));
        controlPanel.add(addButton, gc);
        controlPanel.add(removeButton, gc);
        gc.weighty = 1.0;
        controlPanel.add(new JPanel(new GridBagLayout()), gc);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        add(controlPanel, BorderLayout.EAST);
    }

    public boolean isModified() {
        return modified;
    }

    @SuppressWarnings({"unchecked"})
    public void setData(Map<String, String> data) {
        ListTableModel<Variable> model = (ListTableModel<Variable>) tableView.getModel();
        model.setItems(new ArrayList<Variable>());
        for (Map.Entry<String, String> entry: data.entrySet()){
            model.addRow(new Variable(entry.getKey(), entry.getValue()));
        }
        setUnModified();
    }

    @SuppressWarnings({"unchecked"})
    public Map<String, String> getData() {
        ListTableModel<Variable> model = (ListTableModel<Variable>) tableView.getModel();
        List<Variable> variables = model.getItems();
        Map<String, String> result = new HashMap<String, String>(variables.size());
        for (Variable variable : variables) {
            if (variable.name != null && !variable.name.trim().isEmpty()) {
                result.put(variable.name, variable.value != null ? variable.value : "");
            }
        }
        return result;
    }

    public void setUnModified() {
        modified = false;
    }

    private void setModified() {
        modified = true;
    }

    private static abstract class AbstractVariableColumn extends ColumnInfo<Variable, String> {

        private DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();

        private AbstractVariableColumn(String name) {
            super(name);
        }

        public Class getColumnClass() {
            return String.class;
        }

        public boolean isCellEditable(Variable variable) {
            return true;
        }

        @Override
        public TableCellRenderer getRenderer(Variable variable) {
            return defaultTableCellRenderer;
        }
    }

    private static class Variable {
        public String name;
        public String value;

        private Variable() {
        }

        private Variable(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
