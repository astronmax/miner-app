package miner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class Validator {

    private final int bucketSize = 5;
    private final String localDB = "data.db";
    private final String uploadDir = "uploads";

    private List<Transaction> bucket;
    private int filesCount;
    private boolean mutex;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public Validator() {
        this.bucket = new ArrayList<>();
        this.filesCount = 0;
        this.mutex = false;

        try {
            Files.createDirectories(Paths.get(this.uploadDir));
        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void saveTransaction(Transaction tr) {
        this.bucket.add(tr);
        if (bucket.size() == this.bucketSize) {
            this.mutex = false;
            List<Transaction> stage = new ArrayList<>(this.bucket);
            Future<Block> newBlock = this.makeMining(stage);
            this.saveChanges(newBlock, stage);
            this.bucket.clear();
        }
    }

    public Future<Block> makeMining(List<Transaction> stage) {
        return this.executor.submit(() -> {
            final ResourceManager rm = new ResourceManager(this.localDB);
            long nonce = 0;
            Block lastBlock = rm.getLastBlock();
            rm.close();

            Block newBlock = new Block(lastBlock.getHash());
            newBlock.setNonce(nonce);

            byte[] hash = HashTool.getBlockHash(newBlock, stage);
            while ((hash[0] != 0) || (hash[1] != 0)) {
                nonce++;
                newBlock.setNonce(nonce);
                hash = HashTool.getBlockHash(newBlock, stage);
            }

            newBlock.setHash(HashTool.getHashString(hash));
            return newBlock;
        });
    }

    public void saveChanges(Future<Block> newBlock, List<Transaction> stage) {
        this.executor.submit(() -> {
            try {
                ResourceManager rm = new ResourceManager(this.localDB);
                final Block block = newBlock.get();
                stage.forEach((tr) -> tr.setBlockHash(block.getHash()));

                if (this.validateChain(this.localDB)) {
                    if (this.mutex == false) {
                        System.out.println("[+] NEW BLOCK: " + block.getHash());
                        rm = new ResourceManager(this.localDB);
                        rm.saveBlock(block);
                        rm.saveTransactions(stage);
                        rm.close();

                        DataSender sender = new DataSender();
                        sender.sendFile(this.localDB);
                    }
                } else {
                    System.out.println("[-] CHAIN IS INVALID");
                }

            } catch (final InterruptedException e) {
                System.err.println(e.getMessage());
            } catch (final ExecutionException e) {
                System.err.println(e.getMessage());
            }

        });
    }

    public void saveChain(MultipartFile file) {
        try {
            String filename = "data_" + (++this.filesCount) + ".db";
            Files.copy(file.getInputStream(), Paths.get(this.uploadDir).resolve(filename));
            this.executor.submit(() -> {
                List<String> files = this.getUploadedChains();
                this.mutex = this.validateUploadedChains(files);
            });

        } catch (final IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<String> getUploadedChains() {
        return Stream.of(new File(this.uploadDir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    public boolean validateUploadedChains(List<String> files) {
        final ResourceManager rm = new ResourceManager(this.localDB);
        int blocksCount = rm.getBlocksCount();
        rm.close();
        String newChainName = "";
        for (String filename : files) {
            ResourceManager uploadRM = new ResourceManager(filename);
            int chainBlocksCount = uploadRM.getBlocksCount();
            if (this.validateChain(filename) && chainBlocksCount > blocksCount) {
                blocksCount = chainBlocksCount;
                newChainName = filename;

            } else {
                new File(filename).delete();
            }
        }

        this.filesCount = 0;
        if (!newChainName.isEmpty()) {
            System.out.println("[+] FOUND CHAIN: " + newChainName);
            this.bucket.clear();
            try {
                Files.move(Paths.get(newChainName), Paths.get(this.localDB),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                System.err.println(e.getMessage());
            }

            return true;

        } else {
            return false;
        }
    }

    public boolean validateChain(String databasePath) {
        final ResourceManager rm = new ResourceManager(databasePath);
        List<Block> blocks = rm.getBlocks();

        String prevHash = blocks.get(0).getHash();
        for (Block block : blocks.subList(1, blocks.size())) {
            List<Transaction> trs = rm.getTransactions(block.getHash());
            byte[] hash = HashTool.getBlockHash(block, trs);

            if ((hash[0] != 0) || hash[1] != 0) {
                return false;
            }

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

        rm.close();
        return true;
    }

    public Transaction getTransactionByHash(String hash) {
        final ResourceManager rm = new ResourceManager(this.localDB);
        List<Block> blocks = rm.getBlocks();

        for (Transaction tr : this.bucket) {
            if (HashTool.getHashString(HashTool.getHash(tr.toString())).equals(hash)) {
                return tr;
            }
        }

        for (Block block : blocks) {
            List<Transaction> trs = rm.getTransactions(block.getHash());
            for (Transaction tr : trs) {
                if (HashTool.getHashString(HashTool.getHash(tr.toString())).equals(hash)) {
                    return tr;
                }
            }
        }

        return null;
    }
}
