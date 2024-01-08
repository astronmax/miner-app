package miner;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResourceManager {

    private Connection connection;

    private List<Block> makeBlockArray(ResultSet queryResult) throws SQLException {
        List<Block> blocks = new ArrayList<>();
        while (queryResult.next()) {
            String hash = queryResult.getString("hash");
            String prevHash = queryResult.getString("prevHash");
            long nonce = queryResult.getLong("nonce");

            Block block = new Block(prevHash);
            block.setHash(hash);
            block.setNonce(nonce);
            blocks.add(block);
        }

        return blocks;
    }

    private List<Transaction> makeTransactionArray(ResultSet queryResult) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        while (queryResult.next()) {
            String data = queryResult.getString("data");
            LocalDateTime creationTime = LocalDateTime.parse(queryResult.getString("creationTime"));
            String blockHash = queryResult.getString("blockHash");

            Transaction tr = new Transaction(data);
            tr.setCreationTime(creationTime);
            tr.setBlockHash(blockHash);
            transactions.add(tr);
        }

        return transactions;
    }

    public ResourceManager(String databasePath) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            Statement stmt = this.connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS blocks (" +
                    "prevHash VARCHAR(32) NOT NULL," +
                    "hash VARCHAR(32) NOT NULL," +
                    "nonce LONG NOT NULL)";

            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "data TEXT NOT NULL," +
                    "creationTime STRING NOT NULL," +
                    "blockHash VARCHAR(32) NOT NULL)";

            stmt.executeUpdate(sql);

            if (this.getBlocks().size() == 0) {
                Block genesis = new Block("GENESIS");
                genesis.setNonce(0);
                genesis.setHash("0000000000000000000000000000000000000000000000000000000000000000");
                this.saveBlock(genesis);
            }

            stmt.close();

        } catch (final SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void close() {
        try {
            this.connection.close();
        } catch (final SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void saveBlock(Block block) {
        try {
            Statement stmt = this.connection.createStatement();
            String sql = String.format("INSERT INTO blocks (" +
                    "prevHash, hash, nonce) VALUES ('%s', '%s', %d)",
                    block.getPrevHash(), block.getHash(), block.getNonce());

            stmt.executeUpdate(sql);
            stmt.close();

        } catch (final SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void saveTransactions(List<Transaction> transactions) {
        try {
            Statement stmt = this.connection.createStatement();
            for (Transaction tr : transactions) {
                String sql = String.format("INSERT INTO transactions (" +
                        "data, creationTime, blockHash) VALUES ('%s', '%s', '%s')",
                        tr.getData(), tr.getCreationTime().toString(), tr.getBlockHash());

                stmt.executeUpdate(sql);
            }
            stmt.close();

        } catch (final SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public List<Block> getBlocks() {
        try {
            Statement stmt = this.connection.createStatement();
            String sql = "SELECT * FROM blocks";
            ResultSet rs = stmt.executeQuery(sql);

            return this.makeBlockArray(rs);

        } catch (final SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public Block getLastBlock() {
        List<Block> blocks = this.getBlocks();
        return blocks.get(blocks.size() - 1);
    }

    public List<Transaction> getTransactions(String blockHash) {
        try {
            Statement stmt = this.connection.createStatement();
            String sql = String.format("SELECT * FROM transactions WHERE blockHash = '%s'",
                    blockHash);
            ResultSet rs = stmt.executeQuery(sql);

            return this.makeTransactionArray(rs);

        } catch (final SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public int getBlocksCount() {
        return this.getBlocks().size();
    }
}
