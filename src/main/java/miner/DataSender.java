package miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public class DataSender {

    private final String minersFile = "miners.txt";
    private List<String> miners;

    public DataSender() {
        try {
            this.miners = Files.readAllLines(Paths.get(this.minersFile));

        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void sendTransaction(Transaction tr) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Transaction> requestEntity = new HttpEntity<>(tr);
        for (String minerAddr : miners) {
            restTemplate.postForEntity(minerAddr + "/saveFromMiner",
                    requestEntity, Object.class);
        }
    }

    public void sendFile(String filename) {
        MultipartFile file = new FileMultipartFile(Paths.get(filename));

        Resource resource = file.getResource();
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", resource);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        RestTemplate restTemplate = new RestTemplate();
        for (String minerAdd : miners) {
            restTemplate.postForEntity(minerAdd + "/upload", httpEntity, Object.class);
        }
    }
}
