/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.gui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.instance.Instance;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class InstanceSettingsDialog extends AppDialog {
    public InstanceSettingsDialog(Instance instance) {
        super(TESLauncher.window.getFrame(), "Instance Settings - " + instance.getName());

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        JPanel mainTab = new JPanel(new GridBagLayout());
        // TODO: use OOP to make such settings tabs
        JPanel commonPanel = new JPanel();
        commonPanel.setBorder(new TitledBorder("Common"));
        // add text field with name here

        JPanel javaTab = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel javaInstallation = new JPanel(new GridLayout(0, 1));
        JTextField javaPathTextField = new JTextField();
        javaPathTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Path to java.exe");
        javaInstallation.add(javaPathTextField);
        javaInstallation.setBorder(new TitledBorder("Java Installation"));

        JPanel memorySettings = new JPanel(new GridLayout(2, 2));
        JLabel minMemoryLabel = new JLabel("Minimum memory (Megabytes):");
        JLabel maxMemoryLabel = new JLabel("Maximum memory (Megabytes):");
        JTextField minMemoryField = new JTextField();
        minMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "512");
        JTextField maxMemoryField = new JTextField();
        maxMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "2048");
        memorySettings.add(minMemoryLabel);
        memorySettings.add(minMemoryField);
        memorySettings.add(maxMemoryLabel);
        memorySettings.add(maxMemoryField);
        memorySettings.setBorder(new TitledBorder("Memory Settings"));

        gbc.gridy++;
        javaTab.add(javaInstallation, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        javaTab.add(memorySettings, gbc);

        tabbedPane.addTab("Main", mainTab);
        tabbedPane.addTab("Java", javaTab);

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instance.setJavaPath(javaPathTextField.getText());
                String minMemory = minMemoryField.getText();
                if (minMemory.isEmpty()) {
                    minMemory = "512";
                }

                String maxMemory = maxMemoryField.getText();
                if (maxMemory.isEmpty()) {
                    maxMemory = "2048";
                }

                int minimumMemoryInMegabytes = Integer.parseInt(minMemory);
                int maximumMemoryInMegabytes = Integer.parseInt(maxMemory);

                if (minimumMemoryInMegabytes >= maximumMemoryInMegabytes) {
                    JOptionPane.showMessageDialog(InstanceSettingsDialog.this.getDialog(),
                            "Minimum amount of RAM cannot be larger than maximum",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (minimumMemoryInMegabytes < 512) {
                    JOptionPane.showMessageDialog(
                            InstanceSettingsDialog.this.getDialog(),
                            "Minimum amount of RAM cannot be less than 512 MiB",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                instance.setMinimumMemoryInMegabytes(minimumMemoryInMegabytes);
                instance.setMaximumMemoryInMegabytes(maximumMemoryInMegabytes);
            }
        });

        this.setContent(tabbedPane);
        this.center(0);
        this.setVisible(true);
    }
}
