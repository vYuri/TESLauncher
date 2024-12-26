package me.theentropyshard.teslauncher.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {
    private String name;
    private URL url;
    private File downloadLocation;

    public FileDownloader(URL url, File downloadLocation, String name) {
        this.url = url;
        this.downloadLocation = downloadLocation;
        this.name = name;
    }

    public void downloadFile() throws IOException {
        ReadableByteChannel readChannel = Channels.newChannel(url.openStream());

        FileOutputStream fileOutputStream = new FileOutputStream(downloadLocation.getAbsolutePath() + "/"+name);

        FileChannel writeChannel = fileOutputStream.getChannel();
        writeChannel.transferFrom(readChannel,0,Long.MAX_VALUE);

        readChannel.close();
        writeChannel.close();
        fileOutputStream.close();
    }

}