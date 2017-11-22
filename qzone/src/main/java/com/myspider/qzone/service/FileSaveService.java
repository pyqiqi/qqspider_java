package com.myspider.qzone.service;

import com.myspider.qzone.spider.utils.MultiPageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;


/**
 * Created by *** on 2016/1/17.
 * 文件存储
 */
@Service
public class FileSaveService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${file.path}")
    private String filePath;
    private  boolean isCompess = false;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Async
    public void save(String username, String fileName, MultiPageStore pageStore) {
        if (!pageStore.hasFile()) {
            return;
        }
        FileOutputStream outfs = null;
        try {
            outfs = getFileOutStream(username, fileName,".tar.gz");
            pageStore.compressFile(outfs);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (null != outfs) {
                try {
                    outfs.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private FileOutputStream getFileOutStream(String username, String fileName,String extname) throws FileNotFoundException {
        String datetimeStamp = null;
        String dateStamp = null;
        try {
            datetimeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            dateStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        Path path = Paths.get(filePath, dateStamp, username);
        logger.debug("pathName:" + path);
		File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
		return new FileOutputStream(path.resolve(datetimeStamp + "_" + fileName + extname).toFile());
    }

    @Async
    public void save(String username, String fileName, String content) {
        if(null ==content)
            return;
        FileOutputStream outfs = null;
        try {
            ByteArrayInputStream bins = new ByteArrayInputStream(content.getBytes());
            if(isCompess) {
                outfs = getFileOutStream(username, fileName, ".gz");
                GZIPOutputStream gzout = new GZIPOutputStream(outfs);
                byte[] buf = new byte[1024];
                int size;
                while ((size = bins.read(buf)) != -1)
                    gzout.write(buf, 0, size);
                gzout.flush();
                gzout.close();
                bins.close();
            }else{
                outfs = getFileOutStream(username, fileName, "");
                outfs.write(content.getBytes());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (null != outfs) {
                try {
                    outfs.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }


    }

}
