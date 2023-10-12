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

package me.theentropyshard.teslauncher;

import com.beust.jcommander.JCommander;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.gui.AppWindow;
import me.theentropyshard.teslauncher.gui.Gui;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.instance.InstanceManagerImpl;
import me.theentropyshard.teslauncher.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TESLauncher {
    public static final String TITLE = "TESLauncher";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Args args;
    private final Logger logger;
    private final Path workDir;

    private final Path runtimesDir;
    private final Path minecraftDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path instancesDir;
    private final Path versionsDir;
    private final Path log4jConfigsDir;

    private final AccountsManager accountsManager;
    private final InstanceManager instanceManager;

    private final ExecutorService taskPool;

    private final Gui gui;

    private volatile boolean shutdown;

    public static AppWindow window;

    private TESLauncher(Args args, Logger logger, Path workDir) {
        this.args = args;
        this.logger = logger;
        this.workDir = workDir;

        TESLauncher.setInstance(this);

        this.runtimesDir = this.workDir.resolve("runtimes");
        this.minecraftDir = this.workDir.resolve("minecraft");
        this.assetsDir = this.minecraftDir.resolve("assets");
        this.librariesDir = this.minecraftDir.resolve("libraries");
        this.instancesDir = this.minecraftDir.resolve("instances");
        this.versionsDir = this.minecraftDir.resolve("versions");
        this.log4jConfigsDir = this.minecraftDir.resolve("log4j");
        this.createDirectories();

        this.accountsManager = new AccountsManager(this.workDir);
        try {
            this.accountsManager.loadAccounts();
        } catch (IOException e) {
            this.logger.error("Unable to load accounts", e);
        }

        this.instanceManager = new InstanceManagerImpl(this.instancesDir);
        try {
            this.instanceManager.load();
        } catch (IOException e) {
            this.logger.error("Unable to load instances", e);
        }

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.gui = new Gui(false);
        this.gui.getAppWindow().addWindowClosingListener(this::shutdown);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        this.gui.showGui();
    }

    public static void start(String[] rawArgs) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(rawArgs);

        String workDirPath = args.getWorkDirPath();
        Path workDir = (workDirPath == null || workDirPath.isEmpty() ?
                Paths.get(System.getProperty("user.dir", ".")) :
                Paths.get(workDirPath)).normalize().toAbsolutePath();

        System.setProperty("teslauncher.workDir", workDir.toString());
        Logger logger = LogManager.getLogger(TESLauncher.class);

        new TESLauncher(args, logger, workDir);
    }

    private void createDirectories() {
        try {
            PathUtils.createDirectoryIfNotExists(this.workDir);
            PathUtils.createDirectoryIfNotExists(this.runtimesDir);
            PathUtils.createDirectoryIfNotExists(this.minecraftDir);
            PathUtils.createDirectoryIfNotExists(this.assetsDir);
            PathUtils.createDirectoryIfNotExists(this.librariesDir);
            PathUtils.createDirectoryIfNotExists(this.instancesDir);
            PathUtils.createDirectoryIfNotExists(this.versionsDir);
            PathUtils.createDirectoryIfNotExists(this.log4jConfigsDir);
        } catch (IOException e) {
            this.logger.error("Unable to create launcher directories", e);
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

        try {
            this.taskPool.shutdown();
            TESLauncher.this.instanceManager.getInstances().forEach(i -> {
                try {
                    TESLauncher.this.instanceManager.save(i);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static TESLauncher instance;

    public static TESLauncher getInstance() {
        return TESLauncher.instance;
    }

    private static void setInstance(TESLauncher instance) {
        TESLauncher.instance = instance;
    }

    public Args getArgs() {
        return this.args;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public Path getMinecraftDir() {
        return this.minecraftDir;
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

    public Path getVersionsDir() {
        return this.versionsDir;
    }

    public Path getLog4jConfigsDir() {
        return this.log4jConfigsDir;
    }

    public AccountsManager getAccountsManager() {
        return this.accountsManager;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public Gui getGui() {
        return this.gui;
    }
}
