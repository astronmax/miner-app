package miner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

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
        for (String minerAddr : miners) {
            HttpEntity<Transaction> requestEntity = new HttpEntity<>(tr);
            restTemplate.postForEntity(minerAddr + "/saveFromMiner",
                    requestEntity, Object.class);
        }
    }
}
