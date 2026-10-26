package com.nordlyse.wordfrequency.keystrike;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final KeystrikeMatcher matcher;

    @GetMapping
    public MatchReport readProfile() {
        return matcher.profile();
    }

    @PostMapping
    public MatchReport scoreSentence(@RequestBody MatchAttempt attempt) {
        return matcher.score(attempt);
    }
}
