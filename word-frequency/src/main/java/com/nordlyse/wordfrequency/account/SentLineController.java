package com.nordlyse.wordfrequency.account;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lines")
@RequiredArgsConstructor
public class SentLineController {

    private final AccountService accounts;

    @GetMapping
    public List<SentLine> readLines(HttpServletRequest request) {
        return accounts.lines((String) request.getAttribute(AccountGate.ACCOUNT));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SentLine addLine(HttpServletRequest request, @RequestBody SentLine line) {
        return accounts.addLine(
                (String) request.getAttribute(AccountGate.ACCOUNT),
                line.language(),
                line.text());
    }
}
