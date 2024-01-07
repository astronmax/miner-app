package miner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RequestHandler {

    @Autowired
    Validator validator;

    @PostMapping("/save")
    public void saveTransaction(@RequestBody String data) {
        this.validator.saveTransaction(new Transaction(data));
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        validator.saveChain(file);
    }

}
