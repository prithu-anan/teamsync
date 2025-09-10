// package com.teamsync.notificationservice.config.mongock;

// import com.mongodb.client.MongoDatabase;
// import io.mongock.api.annotations.ChangeUnit;
// import io.mongock.api.annotations.Execution;
// import io.mongock.api.annotations.RollbackExecution;

// @ChangeUnit(id = "create-notification-indexes", order = "001", author = "teamsync")
// public class DatabaseChangeLog001 {
    
//     @Execution
//     public void createIndexes(MongoDatabase mongoDatabase) {
//         // Create index on userId for faster queries
//         mongoDatabase.getCollection("notifications")
//                 .createIndex(new org.bson.Document("userId", 1));
        
//         // Create compound index on userId and isRead
//         mongoDatabase.getCollection("notifications")
//                 .createIndex(new org.bson.Document("userId", 1).append("isRead", 1));
        
//         // Create index on createdAt for sorting
//         mongoDatabase.getCollection("notifications")
//                 .createIndex(new org.bson.Document("createdAt", -1));
//     }
    
//     @RollbackExecution
//     public void rollback(MongoDatabase mongoDatabase) {
//         mongoDatabase.getCollection("notifications").dropIndexes();
//     }
// }