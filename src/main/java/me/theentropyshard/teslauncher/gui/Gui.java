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
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.AccountManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Gui {
    private final JFrame frame;
    public final JButton accountButton;

    public Gui(String title) {

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        FlatLaf.registerCustomDefaultsSource("themes");

        DarkLauncherLaf.setup();

        JLabel background = new JLabel(new ImageIcon("src/main/resources/assets/bg.png"));
        background.setSize(new Dimension(960, 540));

        JLabel logo = new JLabel(new ImageIcon("src/main/resources/assets/title.png"));
        logo.setBounds(TESLauncher.WIDTH / 2 - 270 / 2, 0, 270, 270);

        JPanel bar = new JPanel();
        bar.setBackground(new Color(30, 30, 30));
        bar.setBounds(0, 484, 960, 56);

        this.accountButton = this.getAccountButton();

        TESLauncher.frame = this.frame = new JFrame(title);
        frame.setIconImage(SwingUtils.getImage("/assets/icons/screaminglabs_logo.png"));
        this.frame.setLayout(null);

        this.frame.add(this.getPlayButton());
        this.frame.add(this.accountButton);
        this.frame.add(this.getInfoButton());
        this.frame.add(bar);
        this.frame.add(logo);
        this.frame.add(background);

        this.frame.getContentPane().setPreferredSize(new Dimension(TESLauncher.WIDTH, TESLauncher.HEIGHT));

        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    private JButton getPlayButton() {
        JButton playButton = new JButton();
        int height = 84;
        int width = 454;

        playButton.setBounds(TESLauncher.WIDTH / 2 - width / 2, 484 - height / 2, width, height);
        playButton.setFocusable(false);
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setIcon(new ImageIcon("src/main/resources/assets/play.png"));
        playButton.addActionListener(e -> {
            if (TESLauncher.getInstance().getAccountManager().getCurrentAccount() == null) {

                int option = JOptionPane.showOptionDialog(
                null,
                        "Debes iniciar sesión para poder jugar",
                        "Iniciar sesión",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new String[]{"Iniciar sesión"},
                        "Iniciar Sesión"
                );

                if (option == 0) {
                    new AddAccountDialog();
                }
            }
        });

        return playButton;
    }

    private JButton getAccountButton() {
        AccountManager accountManager = TESLauncher.getInstance().getAccountManager();
        JButton accountButton = new JButton();
        int height = 70;
        int width = 70;

        accountButton.setBounds(718, 484 - height / 2, width, height);
        accountButton.setFocusable(false);
        accountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (accountManager.getCurrentAccount() != null) {
            accountButton.setIcon(SwingUtils.loadIconFromBase64(accountManager.getCurrentAccount().getHeadIcon()));
        } else {
            accountButton.setIcon(new ImageIcon("src/main/resources/assets/add_account.png"));
        }
        accountButton.addActionListener(e -> {
            if (accountManager.getCurrentAccount() == null) {
                new AddAccountDialog();
            } else {
                Account account = TESLauncher.getInstance().getAccountManager().getCurrentAccount();

                String[] options = {"Sí", "No"};

                int option = JOptionPane.showOptionDialog(
                        null,
                        "¿Quieres cerrar sesión con la cuenta '" + account.getUsername() + "'?",
                        "Cerrar sesión",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (option == 1) {
                    return;
                }

                try {
                    TESLauncher.getInstance().getAccountManager().removeAccount(account);
                    accountButton.setIcon(new ImageIcon("src/main/resources/assets/add_account.png"));
                } catch (IOException ex) {
                    MessageBox.showErrorMessage(
                            frame,
                            "Error al cerrar sesión con la cuenta '" + account.getUsername() + "': " + ex.getMessage()
                    );
                }
            }
        });

        return accountButton;
    }

    private JButton getInfoButton() {
        JButton infoButton = new JButton();
        int height = 70;
        int width = 70;

        infoButton.setBounds(172, 484 - height / 2, width, height);
        infoButton.setFocusable(false);
        infoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        infoButton.setIcon(new ImageIcon("src/main/resources/assets/info.png"));

        return infoButton;
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> this.frame.setVisible(true));
    }

    public JFrame getFrame() {
        return this.frame;
    }
}
