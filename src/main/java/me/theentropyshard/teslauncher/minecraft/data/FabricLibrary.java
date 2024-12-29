package me.theentropyshard.teslauncher.minecraft.data;

public class FabricLibrary {

    private final String name;
    private final String url;
    public FabricLibrary(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
