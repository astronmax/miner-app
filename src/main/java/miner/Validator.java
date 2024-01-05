package miner;

import java.nio.file.*;
import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class Validator {

    private final int bucketSize = 5;
    private List<Transaction> bucket;

    public Validator() {
        this.bucket = new ArrayList<>();
    }

    public void addTransaction(Transaction tr) {
        this.bucket.add(tr);
        if (bucket.size() == this.bucketSize) {
            Runnable r = new MiningProcess(new ArrayList<>(this.bucket));
            new Thread(r).start();
            this.bucket.clear();
        }
    }
}
