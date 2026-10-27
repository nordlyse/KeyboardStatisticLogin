package com.nordlyse.wordfrequency.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final AccountService accounts;

    @PostMapping
    public SessionOpened openSession(@RequestBody AccountRequest request) {
        return accounts.openSession(request.username(), request.password());
    }

    @GetMapping
    public AccountName readSession(HttpServletRequest request) {
        return new AccountName((String) request.getAttribute(AccountGate.ACCOUNT));
    }

    @DeleteMapping
    public ResponseEntity<Void> closeSession(HttpServletRequest request) {
        accounts.closeSession((String) request.getAttribute(AccountGate.TOKEN));
        return ResponseEntity.noContent().build();
    }
}
