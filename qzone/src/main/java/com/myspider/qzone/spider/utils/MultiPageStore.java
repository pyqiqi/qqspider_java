package com.myspider.qzone.spider.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by *** on 2016/1/10.
 * 多页面归档压缩
 * @since 1.7
 */
public class MultiPageStore {
    private static final int BUFFER_SIZE = 1024;
    private static final String FILE_SEPARATOR = "/";
    private Map<String, Object> rootMap;

    public MultiPageStore() {
        rootMap = new LinkedHashMap<String, Object>();
    }

    public boolean hasFile() {
        return !rootMap.isEmpty();
    }

    public synchronized void addFile(String path, String content) {
        // path path1/path2
        String[] filter = path.split("/");
        Map<String, Object> tempMap = rootMap;
        for (int i = 0; i < filter.length; i++) {
            String temp = filter[i];
            if ("".equals(temp))
                continue;
            if (i == filter.length - 1) {
                tempMap.put(temp, content);
            } else {
                Map<String, Object> nextMap;
                if (tempMap.containsKey(temp)) {
                    nextMap = (Map<String, Object>) tempMap.get(temp);
                } else {
                    nextMap = new LinkedHashMap<String, Object>();
                    tempMap.put(temp, nextMap);
                    tempMap = nextMap;
                }
                tempMap = nextMap;
            }
        }
    }

    public synchronized void addFile(String path, byte[] bytes) {
        // path path1/path2
        String[] filter = path.split("/");
        Map<String, Object> tempMap = rootMap;
        for (int i = 0; i < filter.length; i++) {
            String temp = filter[i];
            if ("".equals(temp))
                continue;
            if (i == filter.length - 1) {
                tempMap.put(temp, bytes);
            } else {
                Map<String, Object> nextMap;
                if (tempMap.containsKey(temp)) {
                    nextMap = (Map<String, Object>) tempMap.get(temp);
                } else {
                    nextMap = new LinkedHashMap<String, Object>();
                    tempMap.put(temp, nextMap);
                    tempMap = nextMap;
                }
                tempMap = nextMap;
            }
        }
    }

    public synchronized boolean exists(String path) {
        // path path1/path2
        String[] filter = path.split("/");
        Map<String, Object> tempMap = rootMap;
        for (int i = 0; i < filter.length; i++) {
            String temp = filter[i];
            if (i == filter.length - 1) {
                return tempMap.containsKey(temp);
            } else {
                Map<String, Object> nextMap;
                if (tempMap.containsKey(temp)) {
                    nextMap = (Map<String, Object>) tempMap.get(temp);
                    tempMap = nextMap;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public synchronized Object getFile(String path) {
        // path path1/path2
        String[] filter = path.split("/");
        Map<String, Object> tempMap = rootMap;
        String temp;
        for (int i = 0; i < filter.length; i++) {
            temp = filter[i];
            if (i == filter.length - 1) {
                return tempMap.get(temp);
            } else {
                Map<String, Object> nextMap;
                if (tempMap.containsKey(temp)) {
                    nextMap = (Map<String, Object>) tempMap.get(temp);
                    tempMap = nextMap;
                } else {
                    // throw new FileNotFoundException(path);
                    return null;
                }
            }
        }
        return null;
    }

    public void printFileInfo() {
        printMapInfo(rootMap, "file:");
    }

    public void printMapInfo(Map<String, Object> map, String path) {
        Map<String, Object> tempMap;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if ("String".equals(entry.getValue().getClass().getSimpleName())) {
                System.out.println(path + FILE_SEPARATOR + entry.getKey());
            } else {
                String nextpath = path + FILE_SEPARATOR + entry.getKey();
                Object obj = entry.getValue();
                if (obj instanceof Map) {
                    tempMap = (Map<String, Object>) obj;
                    printMapInfo(tempMap, nextpath);
                }
            }
        }	
    }

    private void tarMapDir(Map<String, Object> map, String path, TarArchiveOutputStream taos) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String nextpath = path + "/" + entry.getKey();
            if ("String".equals(entry.getValue().getClass().getSimpleName())) {
                String content = (String) entry.getValue();
                addTarFile(nextpath, content.getBytes("utf-8"), taos);
            } else if ("byte[]".equals(entry.getValue().getClass().getSimpleName())) {
                addTarFile(nextpath, (byte[]) entry.getValue(), taos);
            } else {
                Map<String, Object> tempMap = (Map<String, Object>) entry.getValue();
                tarMapDir(tempMap, nextpath, taos);
            }
        }
    }

    private void addTarFile(String filePathName, byte[] bytes, TarArchiveOutputStream taos) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        // 写入文件信息
        TarArchiveEntry entry = new TarArchiveEntry(filePathName);
        entry.setSize(inputStream.available());
        taos.putArchiveEntry(entry);

        byte[] buf = new byte[BUFFER_SIZE];
        int i = 0;
        // 写入数据
        while ((i = inputStream.read(buf)) != -1) {
            taos.write(buf, 0, i);
        }
        inputStream.close();
        taos.closeArchiveEntry();
    }

    public void compressFile(OutputStream outputStream) throws IOException {
        ByteArrayOutputStream bouts = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bouts);
        tarMapDir(rootMap, "", taos);
        taos.flush();
        taos.close();

        // xz 压缩
        ByteArrayInputStream bins = new ByteArrayInputStream(bouts.toByteArray());
        GZIPOutputStream gzout = new GZIPOutputStream(outputStream);
        byte[] buf = new byte[BUFFER_SIZE];
        int size;
        while ((size = bins.read(buf)) != -1)
            gzout.write(buf, 0, size);
        gzout.flush();
        gzout.close();
        bins.close();
        bouts.close();
    }

    public byte[] getCompressBytes() throws IOException {
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        compressFile(outs);
        byte[] byts = outs.toByteArray();
        outs.close();
        return byts;
    }

    private void dearchiveFile(String path, TarArchiveInputStream tais) throws IOException {
        ByteArrayOutputStream bouts = new ByteArrayOutputStream();
        byte data[] = new byte[BUFFER_SIZE];
        int count;
        while ((count = tais.read(data, 0, BUFFER_SIZE)) != -1) {
            bouts.write(data, 0, count);
        }
        if (path.contains(".xls"))
            addFile(path, bouts.toByteArray());
        else
            addFile(path, new String(bouts.toByteArray(), "utf-8"));
        bouts.close();
    }

    private void dearchiveDir(String path, TarArchiveInputStream tais) throws IOException {
        TarArchiveEntry entry = null;
        while ((entry = tais.getNextTarEntry()) != null) {
            // 文件
            String dir = path + FILE_SEPARATOR + entry.getName();
            if (entry.isDirectory()) {
                // System.out.println(dir);
            } else {
                dearchiveFile(dir, tais);
            }
        }
    }

    public void decompressFile(InputStream scrIn) throws IOException {
        decompressFile(scrIn, "");
    }

    private void decompressFile(InputStream scrIn, String destPath) throws IOException {
        // xz 解压
        ByteArrayOutputStream bouts = new ByteArrayOutputStream();
        GZIPInputStream gzin = new GZIPInputStream(scrIn);
        byte[] buf = new byte[BUFFER_SIZE];
        int size;
        while ((size = gzin.read(buf)) != -1)
            bouts.write(buf, 0, size);
        gzin.close();

        ByteArrayInputStream bints = new ByteArrayInputStream(bouts.toByteArray());
        TarArchiveInputStream tais = new TarArchiveInputStream(bints);
        dearchiveDir(destPath, tais);

        bouts.close();
        bints.close();
        tais.close();
    }
}
