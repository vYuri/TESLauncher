/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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

package me.theentropyshard.teslauncher.gui.dialogs.addaccount;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.dialogs.AppDialog;

import javax.swing.*;
import java.awt.*;

public class AddAccountDialog extends AppDialog {
    private final MicrosoftAccountCreationView microsoftView;

    public AddAccountDialog() {
        super(TESLauncher.frame, "Añadir cuenta");

        JPanel root = new JPanel(new BorderLayout());

        JTabbedPane viewSelector = new JTabbedPane(JTabbedPane.TOP);
        viewSelector.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");

        this.microsoftView = new MicrosoftAccountCreationView(this);
        viewSelector.addTab("Microsoft", this.microsoftView);
        root.add(this.microsoftView);

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
