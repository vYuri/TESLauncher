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
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.dialogs.addaccount.AddAccountDialog;
import me.theentropyshard.teslauncher.gui.laf.DarkLauncherLaf;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;
import me.theentropyshard.teslauncher.instance.InstanceRunner;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.AccountManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class Gui {
    private final JFrame frame;
    private final JSlider slider;
    public final JButton accountButton;

    public Gui(String title) {

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        FlatLaf.registerCustomDefaultsSource("themes");

        DarkLauncherLaf.setup();

        JLabel background = new JLabel();
        background.setSize(new Dimension(960, 540));

        String[] imagePaths = {
                "src/main/resources/assets/bg.png",
                "src/main/resources/assets/bg2.png",
                "src/main/resources/assets/bg3.png"
        };

        Timer timer = new Timer();
        final int[] currentIndex = {0};

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                background.setIcon(new ImageIcon(imagePaths[currentIndex[0]]));
                currentIndex[0] = (currentIndex[0] + 1) % imagePaths.length;
            }
        }, 0, 3000); // Cambia cada 3 segundos

        JLabel logo = new JLabel(new ImageIcon("src/main/resources/assets/title.png"));
        logo.setBounds(TESLauncher.WIDTH / 2 - 270 / 2, 0, 370, 270);

        JPanel bar = new JPanel();
        bar.setBackground(new Color(30, 30, 30));
        bar.setBounds(0, 484, 960, 56);

        JLabel label = new JLabel();
        label.setBounds(20, 415, 200, 100);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setText("Memoria RAM asiganada");

        this.accountButton = this.getAccountButton();

        this.slider = this.getSlider();

        TESLauncher.frame = this.frame = new JFrame(title);
        frame.setIconImage(SwingUtils.getImage("/assets/icons/screaminglabs_logo.png"));
        this.frame.setLayout(null);

        this.frame.add(this.getPlayButton());
        this.frame.add(this.accountButton);
        this.frame.add(this.getInfoButton());
        this.frame.add(this.slider);
        this.frame.add(label);
        this.frame.add(bar);
        this.frame.add(logo);
        this.frame.add(background);

        this.frame.getContentPane().setPreferredSize(new Dimension(TESLauncher.WIDTH, TESLauncher.HEIGHT));

        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    private JButton getPlayButton() {
        JButton playButton = new JButton();
        int height = 36;
        int width = 174;

        // Reduce la opacidad del botón
        float defaultOpacity = 0.0f;
        float hoverOpacity = 0.3f; // Opacidad más oscura al pasar el ratón
        playButton.setOpaque(false);
        playButton.setForeground(new Color(255, 255, 255, (int)(defaultOpacity * 255)));
        playButton.setBackground(new Color(0, 0, 0, 0));

        playButton.setBounds(TESLauncher.WIDTH / 2 - width / 2, 484 - height / 2, width, height);
        playButton.setFocusable(false);
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setIcon(new ImageIcon("src/main/resources/assets/play.png"));

        playButton.setBorder(BorderFactory.createEmptyBorder());

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
            } else {
                MinecraftInstance instance = TESLauncher.getInstance().getInstanceManager().getInstanceByName(TESLauncher.getInstance().getLocalModManifest().getPackName());

                new InstanceRunner(TESLauncher.getInstance().getAccountManager().getCurrentAccount(), new InstanceItem(SwingUtils.getIcon("/assets/grass_icon.png"), instance.getName())).start();
            }
        });

        // Agregar el efecto de hover
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                playButton.setBackground(new Color(0, 0, 0, (int)(hoverOpacity * 255)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playButton.setBackground(new Color(0, 0, 0, (int)(defaultOpacity * 255)));
            }
        });

        return playButton;
    }


    private JButton getAccountButton() {
        AccountManager accountManager = TESLauncher.getInstance().getAccountManager();
        JButton accountButton = new JButton();
        int height = 32;
        int width = 32;

        // Inicializa el botón con opacidad transparente
        float defaultOpacity = 0.0f;
        float hoverOpacity = 0.3f; // Opacidad más oscura al pasar el ratón
        accountButton.setOpaque(false);
        accountButton.setForeground(new Color(255, 255, 255, (int)(defaultOpacity * 255)));
        accountButton.setBackground(new Color(0, 0, 0, 0));

        accountButton.setBounds(620, 484 - height / 2, width, height);
        accountButton.setFocusable(false);
        accountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (accountManager.getCurrentAccount() != null) {
            accountButton.setIcon(SwingUtils.loadIconFromBase64(accountManager.getCurrentAccount().getHeadIcon()));
        } else {
            accountButton.setIcon(new ImageIcon("src/main/resources/assets/add_account.png"));
        }

        accountButton.setBorder(BorderFactory.createEmptyBorder());

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

        // Agregar el efecto de hover
        accountButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                accountButton.setBackground(new Color(0, 0, 0, (int)(hoverOpacity * 255)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                accountButton.setBackground(new Color(0, 0, 0, (int)(defaultOpacity * 255)));
            }
        });

        return accountButton;
    }


    private JButton getInfoButton() {
        JButton infoButton = new JButton();
        int height = 32;
        int width = 32;

        // Inicializa el botón con opacidad transparente
        float defaultOpacity = 0.0f;
        float hoverOpacity = 0.3f; // Opacidad más oscura al pasar el ratón
        infoButton.setOpaque(false);
        infoButton.setForeground(new Color(255, 255, 255, (int)(defaultOpacity * 255)));
        infoButton.setBackground(new Color(0, 0, 0, 0));

        infoButton.setBounds(300, 484 - height / 2, width, height);
        infoButton.setFocusable(false);
        infoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        infoButton.setIcon(new ImageIcon("src/main/resources/assets/info.png"));

        infoButton.setBorder(BorderFactory.createEmptyBorder());

        // Agregar el efecto de hover
        infoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                infoButton.setBackground(new Color(0, 0, 0, (int)(hoverOpacity * 255)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                infoButton.setBackground(new Color(0, 0, 0, (int)(defaultOpacity * 255)));
            }
        });

        return infoButton;
    }

    private JSlider getSlider() {
        JSlider slider = new JSlider(2, 8);

        slider.setBounds(15, 455, 150, 100);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(2);
        slider.setMinorTickSpacing(1);
        slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slider.setPaintLabels(true);

        return slider;
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> this.frame.setVisible(true));
    }

    public JFrame getFrame() {
        return this.frame;
    }
}
