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

    public static boolean validateChain(String databasePath) {
        final ResourceManager rm = new ResourceManager(databasePath);
        List<Block> blocks = rm.getBlocks();

        String prevHash = blocks.get(0).getHash();
        for (Block block : blocks.subList(1, blocks.size())) {
            List<Transaction> trs = rm.getTransactions(block.getHash());
            byte[] hash = HashTool.getBlockHash(block, trs);
            String realHash = HashTool.getHashString(hash);

            if (!prevHash.equals(block.getPrevHash())) {
                return false;
            }

            if (!realHash.equals(block.getHash())) {
                System.out.println(realHash + " " + block.getHash());
                return false;
            }

            prevHash = block.getHash();
        }

        return true;
    }
}
