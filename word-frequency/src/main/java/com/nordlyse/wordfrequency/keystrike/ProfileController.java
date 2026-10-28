package com.nordlyse.wordfrequency.keystrike;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final PaceProfileReader profiles;

    @GetMapping
    public PaceProfile readProfile() {
        return profiles.read();
    }
}
