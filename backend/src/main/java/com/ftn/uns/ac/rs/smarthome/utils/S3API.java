package com.ftn.uns.ac.rs.smarthome.utils;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class S3API {
    private final Region region;
    private final URI endpoint;
    private final URI getEndpoint;
    private final StaticCredentialsProvider credentials;
    private final S3Configuration serviceConfiguration;
    private final Executor s3ExecutionContext;
    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner s3Presigner;

    public S3API() {
        this.region = Region.US_EAST_1;
        this.endpoint = URI.create("http://localhost:8080");
        this.getEndpoint = URI.create("http://localhost:8080");
        this.credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create("K2b6efQVbzaleA4uGQDn",
                        "fmSX6ynh6LvFE7xCn1UTSKnalOQycAMQwCObX67f"));
        this.serviceConfiguration = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        this.s3ExecutionContext = Executors.newFixedThreadPool(10);
        this.s3AsyncClient = S3AsyncClient.builder()
                .region(region)
                .credentialsProvider(credentials)
                .serviceConfiguration(serviceConfiguration)
                .endpointOverride(endpoint)
                .build();
        this.s3Presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentials)
                .serviceConfiguration(serviceConfiguration)
                .endpointOverride(getEndpoint)
                .build();
    }

    public CompletionStage<Void> put(String bucket, String key, File file, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        AsyncRequestBody asyncRequestBody = AsyncRequestBody.fromFile(file.toPath());
        CompletableFuture<PutObjectResponse> putFuture = s3AsyncClient.putObject(request, asyncRequestBody);
        return putFuture.thenApplyAsync(response -> null, s3ExecutionContext);
    }

    public CompletionStage<Void> delete(String bucket, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        CompletableFuture<DeleteObjectResponse> deleteFuture = s3AsyncClient.deleteObject(request);
        return deleteFuture.thenApplyAsync(response -> null, s3ExecutionContext);
    }

    public CompletionStage<String> get(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(getObjectRequest)
                .build();
        return CompletableFuture.supplyAsync(() -> s3Presigner.presignGetObject(getObjectPresignRequest).url().toString(), s3ExecutionContext);
    }
}


