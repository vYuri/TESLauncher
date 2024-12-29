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

package me.theentropyshard.teslauncher.minecraft.data.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.data.*;
import me.theentropyshard.teslauncher.minecraft.data.argument.Argument;
import me.theentropyshard.teslauncher.minecraft.data.argument.ArgumentType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class VersionDeserializer implements JsonDeserializer<Version> {
    public VersionDeserializer() {

    }

    @Override
    public Version deserialize(JsonElement root, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject jsonObject = root.getAsJsonObject();

        Version version = new Version();

        version.setId(jsonObject.get("id").getAsString());
        version.setMainClass(jsonObject.get("mainClass").getAsString());
        version.setType(ctx.deserialize(jsonObject.get("type"), VersionType.class));
        version.setReleaseTime(ctx.deserialize(jsonObject.get("releaseTime"), OffsetDateTime.class));
        version.setTime(ctx.deserialize(jsonObject.get("time"), OffsetDateTime.class));

        if (jsonObject.has("minecraftArguments")) {
            version.setMinecraftArguments(jsonObject.get("minecraftArguments").getAsString());
        }

        if (jsonObject.has("arguments")) {
            EnumMap<ArgumentType, List<Argument>> processedArguments = new EnumMap<>(ArgumentType.class);

            EnumMap<ArgumentType, JsonArray> unprocessedArguments = ctx.deserialize(jsonObject.get("arguments"), new TypeToken<EnumMap<ArgumentType, JsonArray>>() {}.getType());
            processedArguments.put(ArgumentType.JVM, this.processArgs(unprocessedArguments.get(ArgumentType.JVM), ctx));
            processedArguments.put(ArgumentType.GAME, this.processArgs(unprocessedArguments.get(ArgumentType.GAME), ctx));

            version.setArguments(processedArguments);
        }


        if(jsonObject.has("inheritsFrom")) {
            String versionInherits = jsonObject.get("inheritsFrom").getAsString();
            try {
                JsonObject inheritedObject = JsonParser.parseReader(new FileReader(TESLauncher.getInstance().getVersionsDir().resolve(versionInherits).resolve(versionInherits+".json").toFile())).getAsJsonObject();
                version.setAssets(inheritedObject.get("assets").getAsString());

                if (inheritedObject.has("complianceLevel")) {
                    version.setComplianceLevel(inheritedObject.get("complianceLevel").getAsInt());
                } else {
                    version.setComplianceLevel(0);
                }

                if (inheritedObject.has("javaVersion")) {
                    version.setJavaVersion(ctx.deserialize(inheritedObject.get("javaVersion"), Version.JavaVersion.class));
                }

                version.setDownloads(ctx.deserialize(inheritedObject.get("downloads"), new TypeToken<EnumMap<DownloadType, Version.Download>>() {}.getType()));
                version.setLibraries(ctx.deserialize(inheritedObject.get("libraries"), new TypeToken<List<Library>>() {}.getType()));
                version.setAssetIndex(ctx.deserialize(inheritedObject.get("assetIndex"), Version.AssetIndex.class));

                List<FabricLibrary> fabricLibraries = new ArrayList<>();
                jsonObject.get("libraries").getAsJsonArray().forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();
                    fabricLibraries.add(new FabricLibrary(object.get("name").getAsString(), object.get("url").getAsString()));
                });

                version.setFabricLibraries(fabricLibraries);

                if (inheritedObject.has("arguments")) {
                    EnumMap<ArgumentType, List<Argument>> processedArguments = new EnumMap<>(ArgumentType.class);

                    EnumMap<ArgumentType, JsonArray> unprocessedArguments = ctx.deserialize(inheritedObject.get("arguments"), new TypeToken<EnumMap<ArgumentType, JsonArray>>() {}.getType());
                    processedArguments.put(ArgumentType.JVM, this.processArgs(unprocessedArguments.get(ArgumentType.JVM), ctx));
                    processedArguments.put(ArgumentType.GAME, this.processArgs(unprocessedArguments.get(ArgumentType.GAME), ctx));

                    version.setArguments(processedArguments);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            version.setAssetIndex(ctx.deserialize(jsonObject.get("assetIndex"), Version.AssetIndex.class));
            version.setAssets(jsonObject.get("assets").getAsString());

            if (jsonObject.has("complianceLevel")) {
                version.setComplianceLevel(jsonObject.get("complianceLevel").getAsInt());
            } else {
                version.setComplianceLevel(0);
            }

            version.setDownloads(ctx.deserialize(jsonObject.get("downloads"), new TypeToken<EnumMap<DownloadType, Version.Download>>() {}.getType()));
            version.setLibraries(ctx.deserialize(jsonObject.get("libraries"), new TypeToken<List<Library>>() {}.getType()));
        }

        return version;
    }

    private List<Argument> processArgs(JsonArray array, JsonDeserializationContext ctx) {
        List<Argument> arguments = new ArrayList<>();

        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                arguments.add(Argument.withValues(element.getAsString()));
            } else if (element.isJsonObject()) {
                arguments.add(ctx.deserialize(element.getAsJsonObject(), Argument.class));
            }
        }

        return arguments;
    }
}
