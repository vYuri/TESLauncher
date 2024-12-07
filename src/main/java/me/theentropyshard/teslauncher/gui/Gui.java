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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class Gui {
    private final JTabbedPane viewSelector;
    private final JFrame frame;

    public final JButton accountButton;
    public final JButton deleteButton;

    private PlayView playView;
    private AccountsView accountsView;

    private boolean darkTheme;

    public Gui(String title, boolean darkTheme) {
        this.darkTheme = darkTheme;

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        FlatLaf.registerCustomDefaultsSource("themes");

        DarkLauncherLaf.setup();

        JPanel bg = new JPanel();
        bg.setBackground(new Color(30, 30, 30));
        bg.setBounds(0, 0, 960, 85);

        this.viewSelector = new JTabbedPane(JTabbedPane.RIGHT);

        JLabel label = new JLabel(new ImageIcon("src/main/resources/assets/bg.png"));
        JLabel label1 = new JLabel(new ImageIcon("src/main/resources/assets/title.png"));
        label.setSize(new Dimension(960, 540));
        //label1.setSize(new Dimension(102, 44));
        label1.setBounds(TESLauncher.WIDTH / 2 - 480 / 2, (int) (TESLauncher.HEIGHT * 0.5 - 208 / 2.0), 480, 208);

        AccountManager accountManager = TESLauncher.getInstance().getAccountManager();

        this.accountButton = this.getAccountButton();
        this.deleteButton = this.getDeleteButton();
        JButton playButton = this.getPlayButton();

        if (accountManager.getCurrentAccount() == null) {
            deleteButton.setVisible(false);
        }

        TESLauncher.frame = this.frame = new JFrame(title);
        frame.setIconImage(SwingUtils.getImage("/assets/icons/screaminglabs_logo.png"));
        this.frame.setLayout(null);

        this.frame.add(playButton);
        this.frame.add(this.accountButton);
        this.frame.add(this.deleteButton);
        //this.frame.add(bg);
        this.frame.add(label1);
        this.frame.add(label);

        this.frame.getContentPane().setPreferredSize(new Dimension(TESLauncher.WIDTH, TESLauncher.HEIGHT));

        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    @NotNull
    private JButton getDeleteButton() {
        JButton deleteButton = new JButton();
        int height = 65;
        int width = 65;

        deleteButton.setBounds(270, 10, width, height);
        deleteButton.setIcon(new ImageIcon("src/main/resources/assets/trash_icon.png"));
        deleteButton.setFocusable(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Account account = TESLauncher.getInstance().getAccountManager().getCurrentAccount();
                boolean ok = MessageBox.showConfirmMessage(
                        frame,
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
                            frame,
                            "Unable to remove account '" + account.getUsername() + "': " + ex.getMessage()
                    );
                }

                accountButton.setText("Añadir cuenta");
                accountButton.setIcon(new ImageIcon("src/main/resources/assets/add_account.png"));
                deleteButton.setVisible(false);
            }
        });
        return deleteButton;
    }

    private JButton getPlayButton() {
        JButton playButton = new JButton("JUGAR");
        int height = 80;
        int width = 450;

        playButton.setBounds(TESLauncher.WIDTH / 2 - width / 2, (int) (TESLauncher.HEIGHT * 0.9 - height / 2.0), width, height);
        playButton.setFocusable(false);
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setFont(new Font("Arial", Font.PLAIN, 55));
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(playButton.getBounds().x + " " + playButton.getBounds().y);
            }
        });

        return playButton;
    }

    private JButton getAccountButton() {
        AccountManager accountManager = TESLauncher.getInstance().getAccountManager();
        JButton accountButton = new JButton(accountManager.getCurrentAccount() == null ? "Añadir cuenta" : accountManager.getCurrentAccount().getUsername());
        int height = 65;
        int width = 250;

        accountButton.setBounds(10, 10, width, height);
        accountButton.setFocusable(false);
        accountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accountButton.setPreferredSize(new Dimension(250, 55));
        accountButton.setFont(new Font("Arial", Font.PLAIN, 24));
        if (accountManager.getCurrentAccount() != null) {
            accountButton.setIcon(SwingUtils.loadIconFromBase64(accountManager.getCurrentAccount().getHeadIcon()));
        } else {
            accountButton.setIcon(new ImageIcon("src/main/resources/assets/add_account.png"));
        }
        accountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (accountManager.getCurrentAccount() == null) {
                    new AddAccountDialog();
                    System.out.println(accountButton.getHeight());
                }
            }
        });

        return accountButton;
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
