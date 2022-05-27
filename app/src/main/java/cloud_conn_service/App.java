package cloud_conn_service;

// import com.amazonaws.greengrass.javasdk.IotDataClient;
// import com.amazonaws.greengrass.javasdk.model.PublishRequest;
// import com.amazonaws.greengrass.javasdk.model.QueueFullPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.Base64;
import java.util.Date;

// import org.apache.http.HttpEntity;
// import org.apache.http.HttpResponse;
// import org.apache.http.NameValuePair;
// import org.apache.http.client.HttpClient;
// import org.apache.http.client.entity.UrlEncodedFormEntity;
// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.impl.client.HttpClients;
// import org.apache.http.message.BasicNameValuePair;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import cloud_conn_service.models.CloudReadingRequest;
import cloud_conn_service.models.Reading;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZoneId;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


// import com.amazonaws.services.lambda.runtime.Context;
// import com.amazonaws.greengrass.javasdk.IotDataClient;
// import com.amazonaws.greengrass.javasdk.model.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class App {

    public static void main(String[] args) {
        Timer timer = new Timer();
        // Repeat publishing a message every 10 seconds
        timer.scheduleAtFixedRate(new PublishHelloWorld(), 0, 20000);
    }

}


class PublishHelloWorld extends TimerTask {

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

   

    public PrivateKey readPrivateKey(File file) throws Exception {
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
    
        String privateKeyPEM = key
          .replace("-----BEGIN RSA PRIVATE KEY-----", "")
          .replaceAll(System.lineSeparator(), "")
          .replace("-----END RSA PRIVATE KEY-----", "");
    
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
    
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (PrivateKey) keyFactory.generatePrivate(keySpec);
    }

    
    public void run() {
       
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            File private_key_file = new File(classLoader.getResource("user_str.key").getFile());
            PrivateKey pvt = readPrivateKey(private_key_file);
            

            InputStream inputStream = classLoader.getResourceAsStream("user_id.txt");
            String user_id = readFromInputStream(inputStream);
            System.out.println(user_id);
            user_id = user_id.replace("\n", "");

            byte[] data = user_id.getBytes("UTF8");
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(pvt);
            sign.update(data);

            byte[] signatureBytes = sign.sign();
            
            // sign.initVerify(pub);
            // sign.update(data);
            // System.out.println(sign.verify(signatureBytes));

    
            //Creating a MongoDB client
            MongoClient mongoClient = new MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
            
            //Connecting to the database
            MongoDatabase database = mongoClient.getDatabase("test");
            database = database.withCodecRegistry(pojoCodecRegistry);
            //Creating a collection object
            MongoCollection<Reading> collection = database.getCollection("reading",Reading.class);
            //Retrieving the documents
            FindIterable<Reading> fi = collection.find(eq("sent",false));
            MongoCursor<Reading> cursor = fi.iterator();
            try {
                while(cursor.hasNext()) { 
                    Reading reading = cursor.next(); 
                    
                    CloudReadingRequest readingRequest = new CloudReadingRequest(reading.getTemperature(),reading.getHeart_rate(),reading.getSpo2(),Date.from(reading.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
                    ObjectMapper Obj = new ObjectMapper();
                    String jsonStr = Obj.writeValueAsString(readingRequest);
                    

                    String publishMessage = String.format("%s|%s|%s",user_id,Base64.getEncoder().encodeToString(signatureBytes),jsonStr);
                    publishMessage = publishMessage.replace("\n", "");
                    System.out.println(publishMessage);

                    collection.updateOne(eq("_id", reading.getId()),set("sent",true));
                    

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://digicare-rest.herokuapp.com/readings/cloud"))
                            .POST(HttpRequest.BodyPublishers.ofString(publishMessage))
                            .build();
            
                    HttpResponse<String> response = client.send(request,
                            HttpResponse.BodyHandlers.ofString());
            
                    System.out.println(response.body());
                    TimeUnit.SECONDS.sleep(5);


                }
            } finally {
                
                cursor.close();
            }
        } catch (Exception ex) {
            System.err.print(ex.getMessage());
            System.err.println(ex);
        }
    }
}