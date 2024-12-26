package me.theentropyshard.teslauncher.minecraft.download;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.theentropyshard.teslauncher.logging.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ModManifest {

    private final int version;
    private final String fabricVersion;
    private final String fabricLoaderVersion;
    private final String minecraftVersion;
    private final String packName;
    private final URL packUrl;
    private final JsonObject manifestJson;
    private final List<FileRelocation> folderRelocations;
    private final List<FileRemoval> folderRemovals;

    public ModManifest(URI uri) throws MalformedURLException {

        try(InputStreamReader inputStreamReader = new InputStreamReader(uri.toURL().openStream())) {
            BufferedReader reader = new BufferedReader(inputStreamReader);

            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }

            manifestJson = JsonParser.parseString(json.toString()).getAsJsonObject();
        } catch (IOException e) {
            Log.error("Error reading manifest file", e);
            throw new RuntimeException(e);
        }


        this.packName = manifestJson.get("pack-name").getAsString();
        this.packUrl = new URL(manifestJson.get("pack-url").getAsString());
        this.version = manifestJson.get("version").getAsInt();
        this.minecraftVersion = manifestJson.get("minecraft-version").getAsString();
        this.fabricVersion = manifestJson.get("fabric-installer-version").getAsString();
        this.fabricLoaderVersion = manifestJson.get("fabic-loader-version").getAsString();

        this.folderRelocations = new ArrayList<>();
        manifestJson.get("folder-relocations").getAsJsonArray().forEach(element -> folderRelocations.add(new FileRelocation(element.getAsJsonObject())));

        this.folderRemovals = new ArrayList<>();
        manifestJson.get("folder-removals").getAsJsonArray().forEach(element -> folderRemovals.add(new FileRemoval(element.getAsJsonObject())));
    }

    public String getFabricVersion() {
        return fabricVersion;
    }

    public String getFabricLoaderVersion() {
        return fabricLoaderVersion;
    }

    public int  getVersion() {
        return version;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getPackName() {
        return packName;
    }

    public URL getPackUrl() {
        return packUrl;
    }

    public List<FileRelocation> getFolderRelocations() {
        return folderRelocations;
    }

    public List<FileRemoval> getFolderRemovals() {
        return folderRemovals;
    }

    public static class FileRemoval {
        protected final String fileName;
        protected final int version;

        protected FileRemoval(JsonObject object) {
            this.fileName = object.get("file-name").getAsString();
            this.version = object.get("version").getAsInt();
        }

        public int getVersion() {
            return version;
        }

        public String getFileName() {
            return fileName;
        }
    }
    public static class FileRelocation {
        protected final String fileName;
        protected final String destination;
        protected final int version;

        protected FileRelocation(JsonObject object) {
            this.fileName = object.get("file-name").getAsString();
            this.destination = object.get("destination").getAsString();
            this.version = object.get("version").getAsInt();
        }

        public int getVersion() {
            return version;
        }

        public String getFileName() {
            return fileName;
        }

        public String getDestination() {
            return destination;
        }
    }
}
