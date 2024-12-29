package me.theentropyshard.teslauncher.minecraft.download;

import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.utils.FileDownloader;
import me.theentropyshard.teslauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.util.UnzipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.jar.JarFile;

public class FabricDownloader {

    public static final String INSTALLER_PROVIDER = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/%s/fabric-installer-%s.jar";

    private final String minecraftVersion;
    private final Path minecraftDir;

    public FabricDownloader(String minecraftVersion, Path minecraftDir) throws IOException {
        this.minecraftVersion = minecraftVersion;
        this.minecraftDir = minecraftDir;
        ModManifest localManifest = TESLauncher.getInstance().getLocalModManifest();

        Log.info("Checking for Fabric Installed");
        if(TESLauncher.getInstance().isFirstInstall()) {
            Log.info("Initializing Fabric install");
            this.installFabric();
            this.installModPack(false);
        } else {
            Log.info("Fabric is already installed: loader version " + localManifest.getFabricLoaderVersion());
            this.installModPack(true);
        }
    }


    public void installFabric() throws IOException {
        System.out.println("Installing Fabric Installer");
        new FileDownloader(new URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.1/fabric-installer-1.0.1.jar"), minecraftDir.toFile(), "fabric-installer.jar").downloadFile();
        ModManifest localManifest = TESLauncher.getInstance().getLocalModManifest();

        ProcessBuilder installer = new ProcessBuilder("java", "-jar", minecraftDir.resolve("fabric-installer.jar").toFile().getAbsolutePath(), "client", "-dir", minecraftDir.toFile().getAbsolutePath(), "-mcversion", localManifest.getMinecraftVersion(), "-loader", localManifest.getFabricLoaderVersion(), "-launcher", "win32");
        Process p = installer.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Fabric installed");
    }

    /**
     * Installs the modpack
     * @param update - If true, modpack is already installed and will check for any updates
     */
    public void installModPack(boolean update) throws IOException {
        ModManifest external = TESLauncher.getInstance().getExternalModManifest();
        ModManifest local = TESLauncher.getInstance().getLocalModManifest();

        if(external.getVersion() == local.getVersion() && update)  {
            Log.info("Everything is up to date!");
            return;
        }

        // Si se llega a esta fase, hay alguna update, por ende requerirá unzippear
        Path tempDir = minecraftDir.resolve("temp_modpack");
        FileUtils.createDirectoryIfNotExists(tempDir);

        Log.info("Downloading Pack");

        new FileDownloader(external.getPackUrl(), tempDir.toFile(), "TemporaryPack.zip").downloadFile();

        Path modsDir = minecraftDir.resolve("mods");
        Path modsConfigDir = minecraftDir.resolve("config");

        FileUtils.createDirectoryIfNotExists(modsDir);
        FileUtils.createDirectoryIfNotExists(modsConfigDir);

        if(!update) {
            Log.info("Unzipping pack...");
            new ZipFile(tempDir.resolve("TemporaryPack.zip").toFile()).extractAll(tempDir.toFile().getAbsolutePath());

            Arrays.stream(tempDir.toFile().listFiles()).forEach(file -> {
                if(file.getName().endsWith(".jar")) {
                    try {
                        Files.move(file.toPath(), modsDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
                        Log.info("Moved file " + file.getName());
                    } catch (IOException e) {
                        Log.error("Failed moving file " + file.getName(), e);
                    }
                }
            });

            local.getFolderRelocations().forEach(fileRelocation -> {
                Path file = tempDir.resolve(fileRelocation.getFileName());
                try {
                    if(fileRelocation.getFileName().endsWith("/")) { // Tratamiento especial para carpetas que pueden llegar a tener contenidos
                        org.apache.commons.io.FileUtils.copyDirectory(file.toFile(), minecraftDir.resolve(fileRelocation.getDestination()).resolve(file.getFileName()).toFile());
                    } else {
                        Files.copy(file, minecraftDir.resolve(fileRelocation.getDestination()).resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                    Log.info("Moved file " + file.toFile().getName());
                } catch (IOException e) {
                    Log.error("Failed moving file " + file.toFile().getName(), e);
                }
            });

            org.apache.commons.io.FileUtils.deleteDirectory(tempDir.toFile());
        } else { // Significa que solo hay que revisar por update
            Log.info("Unzipping pack...");
            new ZipFile(tempDir.resolve("TemporaryPack.zip").toFile()).extractAll(tempDir.toFile().getAbsolutePath());

            external.getFolderRemovals().stream().filter(fileRemoval -> fileRemoval.getVersion() > local.getVersion()).forEach(file -> {
                if(file.getFileName().endsWith(".jar")) {
                    modsDir.resolve(file.getFileName()).toFile().delete();
                    Log.info("Removed file " + file.getFileName() + " from mods");
                } else {
                    minecraftDir.resolve(file.getFileName()).toFile().delete();
                    Log.info("Removed file " + file.getFileName() + " from files");
                }
            });

            external.getFolderRelocations().stream().filter(fileRelocation -> fileRelocation.getVersion() > local.getVersion()).forEach(file -> {
                try {
                    if(file.getFileName().endsWith(".jar")) {
                        Files.copy(tempDir.resolve(file.getFileName()), modsDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        Log.info("Moved file " + file.getFileName() + " into mods");
                    } else if(file.getFileName().endsWith("/")){
                        org.apache.commons.io.FileUtils.copyDirectory(tempDir.resolve(file.getFileName()).toFile(), minecraftDir.resolve(file.getDestination()).toFile());
                        Log.info("Moved file " + file.getFileName());
                    } else {
                        Files.copy(tempDir.resolve(file.getFileName()), minecraftDir.resolve(file.getDestination()), StandardCopyOption.REPLACE_EXISTING);
                        Log.info("Moved file " + file.getFileName());
                    }
                } catch (IOException e) {
                    Log.error("Failed moving file " + file.getFileName(), e);
                }
            });


            // Updatea el local file para saber que se realizó la actualización correspondiente

            try {
                Log.info("Trying to update manifest file");
                org.apache.commons.io.FileUtils.delete(TESLauncher.getInstance().getWorkDir().resolve("minecraft/manifest.json").toFile());
                new FileDownloader(new URL(TESLauncher.manifestURL), TESLauncher.getInstance().getWorkDir().resolve("minecraft/").toFile(), "manifest.json").downloadFile();
                Log.info("Updated manifest file: " + TESLauncher.getInstance().getWorkDir().resolve("minecraft/manifest.json").toFile().getAbsolutePath());
            } catch (IOException e) {
                Log.error("Failed updating local manifest file", e);
            }

            org.apache.commons.io.FileUtils.deleteDirectory(tempDir.toFile());
        }

    }

}
