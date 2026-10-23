package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/keystrikes")
@RequiredArgsConstructor
public class KeystrikeController {

    private final KeystrikeAnalysis analysis;

    @PostMapping
    public List<NgramTransition> addKeystrike(@RequestBody KeystrikeRequest request) {
        return analysis.addKeystrike(request);
    }
}
