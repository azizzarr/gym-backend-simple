package com.gymapp.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String serviceAccountJson = "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"gym-app-c37ed\",\n" +
            "  \"private_key_id\": \"3ea2e8226280bfdcc6ffc2c1881c1a2575953b35\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC6N5nQuNG6zkzW\\nvl6ICsXXeekBper93eMXcqY/b3rAR38zNcU1wI+QreExG57+KP3HOeLh6OhEaHNs\\nkFTQRFNrhp/Hbt4CW+nIqG0Q5RpmZ6XYTV2t++xOpKlld0Ub/Dewm+aCK4VnFB26\\nMXuv6DMVnGSAmD7FsAmebG1reBrF6hT6Ui18IbRz8tR7Za0D/Ia3W0gsp83iKQcK\\n2ZYlefWX6lfmNBKXOeRK/CyiAcOwhGenbgi1ZdfbvqZ+i2fVGLp+agydaTFZRpA5\\nM1Ld/huyZzHm/sUMH0dYKEox47CkhtGzhGN+hlV3laSi/LPyCptmcTLiB3UR+xTO\\nvCMZQgddAgMBAAECggEAB4faGOkbKjJ6RFucgmSVIvqozp/sGeollebVYaQGKHfR\\nEBFw8T82pG4zuNhaHK3XCCiHdrAEPJ1QwFIx69481WYIriMF/Whu0uY6IEBznzKP\\n2r3TjQauxBtqnPzZdvaYi0mID+Q8rTImEEfnWEd2pr1qtGdJGpYyPWLl0JPMhAu6\\ntosn4gvEnN4sgAY1zxdcDRYGXrk9XI+EvCVu4OmcfFtSL2VrwzbZmt922qlxI6Yj\\n4cVvwpjIHnfCinqkEAe6NJk+FpOCkADjxpM5CfoN4PKOWP/P28v5/y+lJRCmklf2\\noyk2gUnIMnG/Pp+OWfTs7TypVV+rRJMT38xd4tpDYQKBgQDbaZmVpu9CCB8vfAt4\\n5Ta9ghPJUaBgTMshZeSw8e/jX7ei/PjILKIqcpDM7zNDfrBRSBPjzsSOOxlk+n+O\\nGkK8nz+/q3CTT2dXX2LKqhFg3ZjEI27SpUSoittLdhl2xVHBgYS4mXtoH3/1mTKx\\nhd6R5RnuXD6P+OoteXj1bRSvhQKBgQDZRPEGmpmC0ANLBPNRQwKoOjK3aw15EKEY\\npRnzsFpRAdXtJ5p7hSkdItpJkRmKGkPr4XZk592WlkHtO42FTu5f2TrZFb+2Iwnn\\nmsXIg3mbRyD8cMSSI0F6Z0Ur3q3lOdrptScDOkXuEBCgaslNtug9tqKFlvpR7uLy\\nc/zLKP3D+QKBgQDR4KxR81HP5vb1tAFBTnhTRDbrDtKK60ovE0lzXKnr5CZDmRnc\\nWJ1yXw5VicWOnYPI7FpiQenQZ4W+CsOyyTnnNamEDVjtADpI4Gwekhl3f+DeVUpv\\n4jjPw6tK/pgS/WJb6CltbxsVmXQMGNPd7cDd67knQUNy8lYG07g5g3MTBQKBgQCn\\nQK0SWlymGJ8QVwU/nwginHqCO8SpV6XLpPzvXOiJx5H4+C2xvHZD2ZqUW0B/0WxL\\n3soXL26jB5REnT16S6Kw4jas+lMUULDFO53Zl2w7nmvEKMMJhF5ZbboP5WGUPg9J\\nLYyUrX07n95MvyerzYvGRhuiHvEftXe7EJKfijSfcQKBgQCgxXt/AqNXhf0bknZz\\nvJ2imqs4yFF6bAFtCOx8SLUPqrDxs5c2hAWxsZUfJwtR2Bav3naLAf0TTqXHEBBK\\npnemY3zvq8yUdjWpWgeB0h3SJ/uCj5SI9dzPU259mvp0d1Tp/ZauWofgzMTTWPJY\\n+PRy6fYgsshUtkhmvfb+h0t+Zw==\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"firebase-adminsdk-fbsvc@gym-app-c37ed.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"115509817410061478779\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40gym-app-c37ed.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(serviceAccountJson.getBytes())))
            .build();

        return FirebaseApp.initializeApp(options);
    }
} 