package com.example.continuing.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class StorageService {
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretAccessKey;
	@Value("${cloud.aws.region.static}")
	private String region;
	
	@Value("${application.bucket.name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
    	final AmazonS3 s3Client = getAmazonS3();
    	String originalFilename = file.getOriginalFilename();
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
//        オブジェクトのアクセス制御リスト（ACL）を誰でも（被付与者）がオブジェクトの読み取りアクセス（許可）を持つようにする
        s3Client.setObjectAcl(bucketName, fileName, CannedAccessControlList.PublicRead);
        String url = s3Client.getUrl(bucketName, fileName).toString();
        fileObj.delete();
        System.out.println("File uploaded : " + fileName);
        return url;
        
    }

//
//    public byte[] downloadFile(String fileName) {
//        S3Object s3Object = s3Client.getObject(bucketName, fileName);
//        S3ObjectInputStream inputStream = s3Object.getObjectContent();
//        try {
//            byte[] content = IOUtils.toByteArray(inputStream);
//            return content;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//

    public void deleteFile(String url) {
    	final AmazonS3 s3Client = getAmazonS3();
    	String fileName = url.substring(url.lastIndexOf("/") + 1);
        s3Client.deleteObject(bucketName, fileName);
        System.out.println(fileName + " removed ...");
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }
    
    private AmazonS3 getAmazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey)))
                .withRegion(region)
                .build();
    }
}
