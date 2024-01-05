package miner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiningProcess implements Runnable {

    private Logger logger = LoggerFactory.getLogger(Validator.class);
    private List<Transaction> stage;
    private Block lastBlock;
    private final ResourceManager rm = new ResourceManager("data.db");

    public MiningProcess(List<Transaction> stage) {
        this.stage = stage;
        this.lastBlock = this.rm.getLastBlock();
    }

    public void run() {
        long nonce = 0;

        Block newBlock = new Block(this.lastBlock.getHash());
        newBlock.setNonce(nonce);

        byte[] hash = HashTool.getBlockHash(newBlock, this.stage);
        while ((hash[31] != 0) || (hash[30] != 0)) {
            nonce++;
            newBlock.setNonce(nonce);
            hash = HashTool.getBlockHash(newBlock, this.stage);
        }

        newBlock.setHash(HashTool.getHashString(hash));
        this.stage.forEach((tr) -> tr.setBlockHash(newBlock.getHash()));

        rm.saveBlock(newBlock);
        rm.saveTransactions(this.stage);

        logger.info("GET NEW BLOCK: " + newBlock.toString());
    }
}
