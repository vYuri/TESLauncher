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

package me.theentropyshard.teslauncher.minecraft;

import me.theentropyshard.teslauncher.gui.dialogs.MinecraftDownloadDialog;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class GuiMinecraftDownloader extends MinecraftDownloader {
    private final MinecraftDownloadDialog dialog;

    public GuiMinecraftDownloader(Path versionsDir, Path assetsDir, Path librariesDir, Path nativesDir, Path runtimesDir,
                                  Path instanceResourcesDir, MinecraftDownloadDialog dialog) {
        super(versionsDir, assetsDir, librariesDir, nativesDir, runtimesDir, instanceResourcesDir, dialog);
        this.dialog = dialog;
    }

    @Override
    public void downloadMinecraft(String versionId) throws IOException {
        SwingUtilities.invokeLater(() -> this.dialog.setVisible(true));
        super.downloadMinecraft(versionId);
        SwingUtilities.invokeLater(() -> this.dialog.getDialog().dispose());
    }
}
