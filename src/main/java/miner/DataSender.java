package miner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
        this.updateMiners();
    }

    public List<String> getLocalAddresses() {
        try {
            List<String> result = new ArrayList<>();
            List<NetworkInterface> ifaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : ifaces) {
                Collections.list(iface.getInetAddresses())
                        .stream()
                        .forEach((addr) -> result.add(addr.getHostAddress()));
            }

            return result;

        } catch (final SocketException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public boolean isLocalAddress(String addr) {
        for (String localAddr : this.getLocalAddresses()) {
            if (localAddr.contains(addr)) {
                return true;
            }
        }

        return false;
    }

    public void updateMiners() {
        try {
            this.miners = Files.readAllLines(Paths.get(this.minersFile))
                    .stream()
                    .map((dnsAddr) -> {
                        try {
                            return InetAddress.getByName(dnsAddr).getHostAddress();
                        } catch (final UnknownHostException e) {
                            System.err.println(e.getMessage());
                            return null;
                        }
                    })
                    .filter((addr) -> this.isLocalAddress(addr) == false)
                    .map((addr) -> "http://" + addr + ":8080")
                    .toList();

        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void sendTransaction(Transaction tr) {
        this.updateMiners();
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Transaction> requestEntity = new HttpEntity<>(tr);
        for (String minerAddr : miners) {
            restTemplate.postForEntity(minerAddr + "/saveFromMiner",
                    requestEntity, Object.class);
        }
    }

    public void sendFile(String filename) {
        this.updateMiners();
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
