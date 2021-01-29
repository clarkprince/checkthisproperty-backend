package com.mapbox.data;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MapboxDataManager {


    Logger logger = LoggerFactory.getLogger(MapboxDataScheduler.class);

    @Value("${files.path}")
    private String filePath;

    @Value("${mapbox.credential.url}")
    private String mapBoxUrl;

    @Value("${mapbox.credential.accessToken}")
    private String accessToken;

    private String awsEndpointUrl;

    private String awsAccessKey;

    private String awsSecretKey;

    private String awsBucketName;

    @Value("${aws.region}")
    private String awsRegion;


    public void processFile() throws IOException {
        final List<String> fileToProcess = findFiles("shp");
        logger.info("No of files to process {}" , fileToProcess.size());
        fileToProcess.forEach( (file) -> {
            try {
                logger.info("Processing file {}", file);
                String processingFileName = file.substring(0, file.indexOf(".shp"));
                convertShpToGeJson(file.substring(0, file.indexOf(".shp")));
                String response  = getS3Credentials();
                JSONObject json = new JSONObject(response);
                awsEndpointUrl = json.getString( "url" );
                awsAccessKey = json.getString("accessKeyId");
                awsSecretKey = json.getString("secretAccessKey");
                awsBucketName = json.getString("bucket");
                uploadFilesToS3(processingFileName + ".json");
                completeUpload();
            } catch (IOException e) {
                logger.error("Error occurred while converting file {}, {}", file, e);
            }
        });

    }

    private String getS3Credentials() {
        logger.info("get s3 credentials");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response  = restTemplate.postForEntity(mapBoxUrl + "?access_token=" + accessToken, null , String.class);
        logger.info(response.getBody());
        return response.getBody();
    }

    private void uploadFilesToS3(String fileName) {
        logger.info("upload files to s3 {}" , awsAccessKey );
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        // Get AmazonS3 client and return the s3Client object.
        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.fromName(awsRegion))
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .build();
        PutObjectResult putObjectResult = amazonS3.putObject(awsBucketName, fileName, new File(fileName));
        logger.info(putObjectResult.toString());

    }

    private void completeUpload() {
        //TODO
        logger.info("Completing upload");
    }


    private List<String> findFiles(String fileExtension)
            throws IOException {

        if (!Files.isDirectory(Paths.get(filePath))) {
            throw new IllegalArgumentException("Invalid path, path must be a directory!");
        }

        List<String> filesToReturn;

        try (Stream<Path> walk = Files.walk(Paths.get(filePath))) {
            filesToReturn = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }

        return filesToReturn;
    }


    private void convertShpToGeJson(String fileName) throws IOException {
        String cmd = "ogr2ogr -f GEOJSON " +  fileName + ".json " +  fileName + ".shp" ;
        logger.info(cmd);
        Runtime.getRuntime().exec(cmd);
    }

}

