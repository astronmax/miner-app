/**
 * Транзакция. Содержит данные (текст) и дату создания
 */

package miner;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {

    private String data;
    private LocalDateTime creationTime;
    private String blockHash;

    public Transaction() {
    }

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

    @Override
    public String toString() {
        return String.format("[%s]: %s",
                this.creationTime, this.data);
    }

    @Override
    public boolean equals(Object obj) {
        Transaction tr = (Transaction) obj;
        if (!this.data.equals(tr.getData())) {
            return false;
        }

        if (!this.creationTime.equals(tr.getCreationTime())) {
            return false;
        }

        if (this.blockHash != null) {
            if (!this.blockHash.equals(tr.getBlockHash())) {
                return false;
            }
        }

        return true;
    }
}
