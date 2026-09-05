package com.jemigraph.jemigraph_backend.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        initializeApp(
                "/opt/jemigraph/secrets/com-jemiapp-client-firebase-adminsdk-fbsvc-9db3d8fbdf.json",
                "client"
        );

        initializeApp(
                "/opt/jemigraph/secrets/com-jemigrapher-app-firebase-adminsdk-fbsvc-54c0a5dd41.json",
                "photographer"
        );
    }

//    @PostConstruct
//    public void initialize() {
//
//        initializeApp("src/main/resources/com-jemiapp-client-firebase-adminsdk-fbsvc-9db3d8fbdf.json", "client");
//
//        initializeApp("src/main/resources/com-jemigrapher-app-firebase-adminsdk-fbsvc-54c0a5dd41.json", "photographer");
//    }

    private void initializeApp(String filePath, String appName) {
        try (FileInputStream serviceAccount = new FileInputStream(filePath)) {

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            String projectId = null;
            if (credentials instanceof ServiceAccountCredentials) {
                projectId = ((ServiceAccountCredentials) credentials).getProjectId();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();


            boolean appExists = FirebaseApp.getApps().stream()
                    .anyMatch(app -> app.getName().equals(appName));

            if (!appExists) {
                FirebaseApp app = FirebaseApp.initializeApp(options, appName);
                System.out.println("✅ FirebaseApp '" + appName + "' imeanzishwa kwa mafanikio. Project ID: " + projectId);
            } else {
                FirebaseApp existingApp = FirebaseApp.getInstance(appName);
                System.out.println("🔄 FirebaseApp '" + appName + "' tayari ilishakuwa initialized mbeleni.");
                System.out.println("🆔 Existing Project ID (" + appName + "): " + existingApp.getOptions().getProjectId());
            }

        } catch (Exception e) {
            System.err.println("❌ Hitilafu wakati wa kuanzisha Firebase kwa ajili ya '" + appName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
}