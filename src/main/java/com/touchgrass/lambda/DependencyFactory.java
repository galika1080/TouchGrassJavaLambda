
package com.touchgrass.lambda;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import software.amazon.awssdk.services.s3.S3Client; 

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of DynamoDbClient
     */
    public static DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                       .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                       .region(Region.US_WEST_2)
                       .httpClientBuilder(UrlConnectionHttpClient.builder())
                       .build();
    }

    public static S3Client s3Client() {
        return S3Client.builder()
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .region(Region.US_WEST_2)
                    .httpClientBuilder(UrlConnectionHttpClient.builder())
                    .build();
    }
}
