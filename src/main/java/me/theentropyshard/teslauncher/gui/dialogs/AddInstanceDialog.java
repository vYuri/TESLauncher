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

package me.theentropyshard.teslauncher.gui.dialogs;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddInstanceDialog {
    private final JDialog dialog;
    private final JTextField nameField;
    private final JTextField groupField;

    public AddInstanceDialog(PlayView playView, String groupName) {
        JPanel root = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel headerPanelLeftPanel = new JPanel();
        headerPanelLeftPanel.setLayout(new GridLayout(2, 1));
        headerPanelLeftPanel.add(new JLabel("Name:") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});
        headerPanelLeftPanel.add(new JLabel("Group:") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});

        JPanel headerPanelRightPanel = new JPanel();
        headerPanelRightPanel.setLayout(new GridLayout(2, 1));

        this.nameField = new JTextField();
        headerPanelRightPanel.add(this.nameField);

        this.groupField = new JTextField(groupName);
        headerPanelRightPanel.add(this.groupField);

        headerPanel.add(headerPanelLeftPanel, BorderLayout.WEST);
        headerPanel.add(headerPanelRightPanel, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);

        JTable versionsTable = new JTable();

        JScrollPane scrollPane = new JScrollPane(versionsTable);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 0;

        JLabel filterLabel = new JLabel("Filter");
        filterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        filterPanel.add(filterLabel, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        JCheckBox releasesBox = new JCheckBox("Releases");
        JCheckBox snapshotsBox = new JCheckBox("Snapshots");
        JCheckBox betasBox = new JCheckBox("Betas");
        JCheckBox alphasBox = new JCheckBox("Alphas");
        JCheckBox experimentsBox = new JCheckBox("Experiments");

        gbc.gridy = 1;
        filterPanel.add(releasesBox, gbc);
        gbc.gridy = 2;
        filterPanel.add(snapshotsBox, gbc);
        gbc.gridy = 3;
        filterPanel.add(betasBox, gbc);
        gbc.gridy = 4;
        filterPanel.add(alphasBox, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1.0;
        filterPanel.add(experimentsBox, gbc);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(filterPanel, BorderLayout.EAST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String chosenGroupName = this.groupField.getText();
            playView.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), this.nameField.getText()), chosenGroupName);
            this.getDialog().dispose();
        });
        buttonsPanel.add(addButton);
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBorder(new EmptyBorder(0, 10, 6, 6));
        root.add(buttonsPanel, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(800, 600));

        this.dialog = new JDialog(TESLauncher.window.getFrame(), "Add New Instance", true);
        this.dialog.setResizable(false);
        this.dialog.add(root, BorderLayout.CENTER);
        this.dialog.pack();
        this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.center(this.dialog, 0);
        this.dialog.setVisible(true);
    }

    public void center(Window window, int screen) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();

        if (screen < 0 || screen >= allDevices.length) {
            screen = 0;
        }

        Rectangle bounds = allDevices[screen].getDefaultConfiguration().getBounds();
        window.setLocation(
                ((bounds.width - window.getWidth()) / 2) + bounds.x,
                ((bounds.height - window.getHeight()) / 2) + bounds.y
        );
    }

    public JDialog getDialog() {
        return this.dialog;
    }
}