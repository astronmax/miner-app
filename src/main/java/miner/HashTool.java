package miner;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HashTool {
    public static byte[] getHash(final String data) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(data.getBytes("UTF-8"));

            return hash;

        } catch (final NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (final UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static String getHashString(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            final String hex = Integer.toHexString(Byte.toUnsignedInt(b));
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static byte[] getBlockHash(Block block, List<Transaction> trans) {
        StringBuilder data = new StringBuilder();
        data.append(block.toString());
        trans.forEach(tr -> data.append(tr.toString()));

        return HashTool.getHash(data.toString());
    }
}
