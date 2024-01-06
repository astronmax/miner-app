/**
 * Транзакция. Содержит данные (текст) и дату создания
 */

package miner;

import java.time.LocalDateTime;

public class Transaction {

    private String data;
    private LocalDateTime creationTime;
    private String blockHash;

    public Transaction(String data) {
        this.data = data;
        this.creationTime = LocalDateTime.now();
        this.blockHash = null;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String toString() {
        return String.format("[%s;]: %s",
                this.creationTime, this.data);
    }
}
