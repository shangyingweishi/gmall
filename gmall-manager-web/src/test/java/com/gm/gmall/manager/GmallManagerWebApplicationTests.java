package com.gm.gmall.manager;


import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class GmallManagerWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {
//
//        String traker = GmallManagerWebApplicationTests.class.getResource("/tracker.conf").getPath();//获取配置文件的路径
//
//        ClientGlobal.init(traker);
//
//        TrackerClient trackerClient = new TrackerClient();
//        TrackerServer trackerServer = trackerClient.getConnection();
//        StorageClient storageClient = new StorageClient(trackerServer, null);
//        String[] uploadFile = storageClient.upload_file("C:\\Users\\snow\\Desktop\\2.jpg", "jgp", null);
//        String url = "http://192.168.1.50";
//        for (String s : uploadFile) {
//            url += "/" + s ;
//        }
//
//        System.out.println(url);
   }

}
