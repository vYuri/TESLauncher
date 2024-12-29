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

package me.theentropyshard.teslauncher;

import me.theentropyshard.teslauncher.instance.InstanceAlreadyExistsException;
import me.theentropyshard.teslauncher.minecraft.account.AccountManager;
import me.theentropyshard.teslauncher.gui.Gui;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.minecraft.download.FabricDownloader;
import me.theentropyshard.teslauncher.minecraft.download.ModManifest;
import me.theentropyshard.teslauncher.network.UserAgentInterceptor;
import me.theentropyshard.teslauncher.gui.utils.WindowClosingListener;
import me.theentropyshard.teslauncher.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import me.theentropyshard.teslauncher.logging.Log;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TESLauncher {
    

    public static final String USER_AGENT = BuildConfig.APP_NAME + "/" + BuildConfig.APP_VERSION;

    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Args args;
    private final Path workDir;

    private final Path runtimesDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path instancesDir;
    private Path versionsDir;
    private final Path log4jConfigsDir;

    private final Path settingsFile;
    private final Settings settings;

    private final OkHttpClient httpClient;

    private final AccountManager accountManager;
    private final InstanceManager instanceManager;
    private ModManifest externalModManifest;
    private ModManifest localModManifest;
    private boolean firstInstall;

    private final ExecutorService taskPool;

    private final Gui gui;

    private volatile boolean shutdown;

    public static final String manifestURL = "https://elreino.live/launcher/7a290cbd-87bc-4c86-bd6e-7e60248f3080.json";
    public static JFrame frame;

    public TESLauncher(Args args, Path workDir) {
        this.args = args;
        this.workDir = workDir;

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        if (args.hasUnknownOptions()) {
            Log.warn("Unknown options: " + args.getUnknownOptions());
        }

        TESLauncher.setInstance(this);

        Path minecraftDir = this.workDir.resolve("minecraft");
        this.runtimesDir = minecraftDir.resolve("runtimes");
        this.assetsDir = minecraftDir.resolve("assets");
        this.librariesDir = minecraftDir.resolve("libraries");
        this.instancesDir = minecraftDir.resolve("instances");
        this.versionsDir = minecraftDir.resolve("versions");
        this.log4jConfigsDir = minecraftDir.resolve("log4j");
        this.createDirectories();

        this.settingsFile = this.workDir.resolve("settings.json");
        this.settings = Settings.load(this.settingsFile);

        this.httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new UserAgentInterceptor(TESLauncher.USER_AGENT))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        this.accountManager = new AccountManager(minecraftDir);
        try {
            this.accountManager.load();
        } catch (IOException e) {
            Log.error("Unable to load accounts", e);
        }

        this.instanceManager = new InstanceManager(this.instancesDir);
        try {
            this.instanceManager.load();
        } catch (IOException e) {
            Log.error("Unable to load instances", e);
        }

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.gui = new Gui(BuildConfig.APP_NAME);
        this.gui.getFrame().addWindowListener(new WindowClosingListener(e -> TESLauncher.this.shutdown()));

        this.gui.showGui();

        try {
            URI uri = new URI(manifestURL);
            this.externalModManifest = new ModManifest(uri);

            File manifestFile = minecraftDir.resolve("manifest.json").toFile();
            if(!manifestFile.exists() && externalModManifest != null) {
                this.firstInstall = true;
                Files.copy(uri.toURL().openStream(), manifestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            localModManifest = new ModManifest(manifestFile.toPath().toUri());
        } catch (URISyntaxException | IOException e) {
            Log.error("Failed to load mod manifest", e);
        }

        if(externalModManifest != null && localModManifest != null && firstInstall) {
            try {
                this.instanceManager.createInstance(localModManifest.getPackName(), "xd?", localModManifest.getMinecraftVersion());
            } catch (IOException | InstanceAlreadyExistsException e) {
                Log.error("Failed to create instance", e);
            }
        }

        versionsDir = this.instanceManager.getInstanceByName(localModManifest.getPackName()).getMinecraftDir().resolve("versions");
        try {
            FileUtils.createDirectoryIfNotExists(versionsDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*try {
            FabricDownloader fabricDownloader = new FabricDownloader(localModManifest.getMinecraftVersion(), instanceManager.getInstanceByName(localModManifest.getPackName()).getMinecraftDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    private void createDirectories() {
        try {
            FileUtils.createDirectoryIfNotExists(this.workDir);
            FileUtils.createDirectoryIfNotExists(this.runtimesDir);
            FileUtils.createDirectoryIfNotExists(this.assetsDir);
            FileUtils.createDirectoryIfNotExists(this.librariesDir);
            FileUtils.createDirectoryIfNotExists(this.instancesDir);
            FileUtils.createDirectoryIfNotExists(this.versionsDir);
            FileUtils.createDirectoryIfNotExists(this.log4jConfigsDir);
        } catch (IOException e) {
            Log.error("Unable to create launcher directories", e);
        }
    }

    public void doTask(Runnable r) {
        this.taskPool.submit(r);
    }

    public void shutdown() {
        if (this.shutdown) {
            return;
        }

        this.shutdown = true;

        this.taskPool.shutdown();

        try {
            this.accountManager.save();
        } catch (IOException e) {
            Log.error("Exception while saving accounts", e);
        }

        this.instanceManager.getInstances().forEach(instance -> {
            try {
                instance.save();
            } catch (IOException e) {
                Log.error("Exception while saving instance '" + instance + "'", e);
            }
        });

        this.settings.save(this.settingsFile);

        System.exit(0);
    }

    private static TESLauncher instance;

    public static TESLauncher getInstance() {
        return TESLauncher.instance;
    }

    private static void setInstance(TESLauncher instance) {
        TESLauncher.instance = instance;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public Args getArgs() {
        return this.args;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public Path getAssetsDir() {
        return this.assetsDir;
    }

    public Path getLibrariesDir() {
        return this.librariesDir;
    }

    public Path getInstancesDir() {
        return this.instancesDir;
    }

    public Path getRuntimesDir() {
        return this.runtimesDir;
    }

    public Path getVersionsDir() {
        return this.versionsDir;
    }

    public Path getLog4jConfigsDir() {
        return this.log4jConfigsDir;
    }

    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public Gui getGui() {
        return this.gui;
    }

    public ModManifest getExternalModManifest() {
        return externalModManifest;
    }

    public ModManifest getLocalModManifest() {
        return localModManifest;
    }

    public boolean isFirstInstall() {
        return firstInstall;
    }
}
