/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.gui.playview;

import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

public class PlayView extends View {
    private final PlayViewHeader header;
    private final JPanel instancesPanelView;
    private final InstancesPanel defaultInstancesPanel;
    private final Map<String, InstancesPanel> groups;
    private final CardLayout cardLayout;
    private final DefaultComboBoxModel<String> model;

    public PlayView() {
        JPanel root = this.getRoot();

        this.groups = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.instancesPanelView = new JPanel(this.cardLayout);

        this.header = new PlayViewHeader();
        root.add(this.header.getRoot(), BorderLayout.NORTH);

        this.defaultInstancesPanel = new InstancesPanel();
        this.groups.put("<default>", this.defaultInstancesPanel);
        root.add(this.instancesPanelView, BorderLayout.CENTER);

        this.groups.forEach((name, panel) -> {
            this.instancesPanelView.add(panel.getRoot(), name);
        });

        JComboBox<String> instanceGroups = this.header.getInstanceGroups();
        String[] items = {"<default>"};
        this.model = new DefaultComboBoxModel<>(items);

        instanceGroups.setModel(this.model);
        instanceGroups.addItemListener(e -> {
            int stateChange = e.getStateChange();
            if (stateChange == ItemEvent.SELECTED) {
                Object[] selectedObjects = e.getItemSelectable().getSelectedObjects();
                String groupName = String.valueOf(selectedObjects[0]);
                this.cardLayout.show(this.instancesPanelView, groupName);
                System.out.println(groupName);
            }
        });

        this.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), "Minecraft"), "<default>");
        this.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), "Minecraft"), "<default>");
        this.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), "Minecraft"), "<default>");
        this.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), "Minecraft"), "Modpacks");
        this.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), "Minecraft"), "Others");
        this.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), "Minecraft"), "Others");
    }

    public void addInstanceItem(InstanceItem item, String groupName) {
        InstancesPanel panel = this.groups.get(groupName);
        if (panel == null) {
            panel = new InstancesPanel();
            this.groups.put(groupName, panel);
            this.model.addElement(groupName);
            this.instancesPanelView.add(panel.getRoot(), groupName);
        }
        panel.addInstanceItem(item);
    }

    public PlayViewHeader getHeader() {
        return this.header;
    }

    public InstancesPanel getDefaultInstancesPanel() {
        return this.defaultInstancesPanel;
    }
}
