package com.teamsync.feedmanagement.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Value("${azure.storage.sas-token}")
    private String sasToken;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getSasToken() {
        return sasToken;
    }
} 