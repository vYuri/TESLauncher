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

package me.theentropyshard.teslauncher.gui;

import com.formdev.flatlaf.FlatLaf;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.dialogs.addaccount.AddAccountDialog;
import me.theentropyshard.teslauncher.gui.laf.DarkLauncherLaf;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.view.playview.PlayView;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.AccountManager;
import me.theentropyshard.teslauncher.minecraft.account.AccountStorage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class Gui {
    private final JTabbedPane viewSelector;
    private final JFrame frame;

    public final JButton accountButton;

    private PlayView playView;
    private AccountsView accountsView;

    private boolean darkTheme;

    public Gui(String title, boolean darkTheme) {
        this.darkTheme = darkTheme;

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        FlatLaf.registerCustomDefaultsSource("themes");

        DarkLauncherLaf.setup();

        this.viewSelector = new JTabbedPane(JTabbedPane.RIGHT);

        TESLauncher.frame = this.frame = new JFrame(title);

        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel playPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        playPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        playPanel.setLayout(new GridBagLayout());

        AccountManager accountManager = TESLauncher.getInstance().getAccountManager();

        this.accountButton = new JButton(accountManager.getCurrentAccount() == null ? "Añadir cuenta" : accountManager.getCurrentAccount().getUsername());
        accountButton.setFocusable(false);
        accountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accountButton.setPreferredSize(new Dimension(250, 55));
        accountButton.setFont(new Font("Arial", Font.PLAIN, 24));
        accountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accountManager.getCurrentAccount() == null) {
                    new AddAccountDialog();
                }
            }
        });

        JButton deleteButton = getDeleteButton();

        JButton playButton = new JButton("JUGAR");
        playButton.setFocusable(false);
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setPreferredSize(new Dimension((int) (TESLauncher.WIDTH * 0.3), (int) (TESLauncher.HEIGHT * 0.15)));
        playButton.setFont(new Font("Arial", Font.PLAIN, 55));

        accountPanel.add(accountButton);
        if (accountManager.getCurrentAccount() != null) {
            accountPanel.add(deleteButton);
        }
        playPanel.add(playButton);

        this.frame.add(accountPanel, BorderLayout.NORTH);
        this.frame.add(playPanel, BorderLayout.CENTER);

        this.frame.getContentPane().setPreferredSize(new Dimension(TESLauncher.WIDTH, TESLauncher.HEIGHT));
        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    @NotNull
    private JButton getDeleteButton() {
        JButton deleteButton = new JButton("");
        deleteButton.setIcon(new ImageIcon("src/main/resources/assets/trash_icon.png"));
        deleteButton.setFocusable(false);
        deleteButton.setSize(65, 65);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Account account = TESLauncher.getInstance().getAccountManager().getCurrentAccount();
                boolean ok = MessageBox.showConfirmMessage(
                        TESLauncher.frame,
                        "Account removal",
                        "Are you sure that you want to remove account '" + account.getUsername() + "'?"
                );

                if (!ok) {
                    return;
                }

                try {
                    TESLauncher.getInstance().getAccountManager().removeAccount(account);
                } catch (IOException ex) {
                    ex.printStackTrace();

                    MessageBox.showErrorMessage(
                            TESLauncher.frame,
                            "Unable to remove account '" + account.getUsername() + "': " + ex.getMessage()
                    );
                }

                accountButton.setName("Añadir cuenta");
                deleteButton.setVisible(false);
            }
        });
        return deleteButton;
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> {
            this.playView = new PlayView();
            this.accountsView = new AccountsView();
            this.viewSelector.addTab("Accounts", this.accountsView);
            this.frame.setVisible(true);
        });
    }

    public JFrame getFrame() {
        return this.frame;
    }

    public boolean isDarkTheme() {
        return this.darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }

    public PlayView getPlayView() {
        return this.playView;
    }

    public AccountsView getAccountsView() {
        return this.accountsView;
    }
}
