package miner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RequestHandler {

    @Autowired
    private Validator validator;

    private final DataSender dataSender = new DataSender();

    @PostMapping("/save")
    public String saveTransaction(@RequestBody String data) {
        Transaction tr = new Transaction(data);
        this.validator.saveTransaction(tr);
        this.dataSender.sendTransaction(tr);

        return HashTool.getHashString(HashTool.getHash(tr.toString()));
    }

    @GetMapping("/get/{hash}")
    public Transaction getTransaction(@PathVariable String hash) {
        return this.validator.getTransactionByHash(hash);
    }

    @PostMapping("/saveFromMiner")
    public void saveTransactionFromMiner(@RequestBody Transaction tr) {
        validator.saveTransaction(tr);
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        validator.saveChain(file);
    }

}
