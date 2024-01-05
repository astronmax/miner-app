package miner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestHandler {

    @Autowired
    Validator validator;

    @PostMapping("/save")
    public void saveTransaction(@RequestBody String data) {
        this.validator.addTransaction(new Transaction(data));
    }

}
