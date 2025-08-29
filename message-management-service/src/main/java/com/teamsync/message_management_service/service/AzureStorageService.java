package com.teamsync.message_management_service.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.teamsync.message_management_service.config.AzureStorageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AzureStorageService {

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Autowired
    private AzureStorageConfig azureStorageConfig;

    /**
     * Uploads a file to Azure Blob Storage and returns a secure URL
     * @param file MultipartFile to upload
     * @return Secure URL for the uploaded file
     */
    public String uploadFile(MultipartFile file) {
        try {
            // Get file bytes
            byte[] fileBytes = file.getBytes();
            
            // Generate unique blob name to avoid conflicts
            String blobName = generateUniqueBlobName(file.getOriginalFilename());
            
            // Get container client
            BlobContainerClient containerClient = blobServiceClient
                    .getBlobContainerClient(azureStorageConfig.getContainerName());
            
            // Get blob client
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Upload the file with content type
            blobClient.upload(new java.io.ByteArrayInputStream(fileBytes), fileBytes.length, true);
            
            // Set content type after upload
            com.azure.storage.blob.models.BlobHttpHeaders headers = new com.azure.storage.blob.models.BlobHttpHeaders();
            headers.setContentType(file.getContentType());
            blobClient.setHttpHeaders(headers);
            
            // Generate secure URL with SAS token
            return generateSecureUrl(blobName);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /**
     * Generates a unique blob name to avoid file name conflicts
     * @param originalFileName Original file name
     * @return Unique blob name
     */
    private String generateUniqueBlobName(String originalFileName) {
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + fileExtension;
    }

    /**
     * Generates a secure URL with SAS token for the uploaded file
     * @param blobName Name of the blob in Azure
     * @return Secure URL with SAS token
     */
    private String generateSecureUrl(String blobName) {
        // Create SAS permissions
        BlobSasPermission permission = new BlobSasPermission()
                .setReadPermission(true);
        
        // Set expiration time (1 year from now)
        OffsetDateTime expiryTime = OffsetDateTime.now().plusYears(1);
        
        // Create SAS signature values
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, permission);
        
        // Generate SAS token
        String sasToken = blobServiceClient
                .getBlobContainerClient(azureStorageConfig.getContainerName())
                .getBlobClient(blobName)
                .generateSas(sasSignatureValues);
        
        // Construct the secure URL
        return String.format("https://%s.blob.core.windows.net/%s/%s?%s",
                azureStorageConfig.getAccountName(),
                azureStorageConfig.getContainerName(),
                blobName,
                sasToken);
    }

    /**
     * Deletes multiple files from Azure Blob Storage by their URLs
     * @param fileUrls Array of file URLs to delete
     * @return Number of files successfully deleted
     */
    public int deleteFilesByUrls(String[] fileUrls) {
        if (fileUrls == null || fileUrls.length == 0) {
            return 0;
        }

        int deletedCount = 0;
        BlobContainerClient containerClient = blobServiceClient
                .getBlobContainerClient(azureStorageConfig.getContainerName());

        for (String fileUrl : fileUrls) {
            try {
                if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                    String blobName = extractBlobNameFromUrl(fileUrl);
                    if (blobName != null) {
                        BlobClient blobClient = containerClient.getBlobClient(blobName);
                        if (blobClient.exists()) {
                            blobClient.delete();
                            deletedCount++;
                        }
                    }
                }
            } catch (Exception e) {
                // Log the error but continue with other files
                System.err.println("Failed to delete file: " + fileUrl + " - " + e.getMessage());
            }
        }

        return deletedCount;
    }

    /**
     * Extracts the blob name from a complete Azure Blob Storage URL
     * @param fileUrl Complete URL of the file in Azure Blob Storage
     * @return Blob name or null if URL format is invalid
     */
    private String extractBlobNameFromUrl(String fileUrl) {
        try {
            // Expected URL format: https://{account}.blob.core.windows.net/{container}/{blobName}?{sasToken}
            String[] urlParts = fileUrl.split("/");
            
            // Find the container name and blob name
            for (int i = 0; i < urlParts.length; i++) {
                if (urlParts[i].equals(azureStorageConfig.getContainerName()) && i + 1 < urlParts.length) {
                    // The next part should be the blob name (before the query parameters)
                    String blobNameWithParams = urlParts[i + 1];
                    // Remove query parameters if present
                    if (blobNameWithParams.contains("?")) {
                        return blobNameWithParams.substring(0, blobNameWithParams.indexOf("?"));
                    }
                    return blobNameWithParams;
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
} 