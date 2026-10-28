package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nordlyse.wordfrequency.account.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final PaceProfileReader profiles;
    private final AccountService accounts;

    @GetMapping
    public PaceProfile readProfile() {
        String username = ActiveProfile.orElse("");
        List<String> lines = username.isEmpty()
                ? List.of()
                : accounts.lines(username).stream().map(line -> line.text()).toList();
        return profiles.read(lines);
    }
}
