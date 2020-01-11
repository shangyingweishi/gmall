package com.gm.gmall.manager.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {
    public static String fileUpload(MultipartFile multipartFile) {

        String traker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();//获取配置文件的路径

        try {
            ClientGlobal.init(traker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String imgUrl = "http://192.168.1.50";
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageClient storageClient = new StorageClient(trackerServer, null);
        try {

            //获得上传文件的二进制数组
            byte[] bytes = multipartFile.getBytes();
            String originalFilename = multipartFile.getOriginalFilename();
            System.out.println(originalFilename);
            int i = originalFilename.lastIndexOf(".");
            String substring = originalFilename.substring(i + 1);

            String[] uploadFile = storageClient.upload_file(bytes,substring, null);

            for (String s : uploadFile) {
                imgUrl += "/" + s ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        return imgUrl;

    }
}
