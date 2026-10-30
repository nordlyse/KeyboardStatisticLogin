package com.nordlyse.wordfrequency.keystrike;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Compare one sentence with the signed-in account's stored pace. The check does not write the attempt back.")
@SecurityRequirement(name = "bearer")
public class MatchController {

    private final KeystrikeMatcher matcher;

    @GetMapping
    @Operation(summary = "Read whether a pace is stored", description = "Reports whether this account has any stored typing. The steps list is empty.")
    @ApiResponse(responseCode = "200", description = "Whether a pace is stored.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public MatchReport readProfile() {
        return matcher.profile();
    }

    @PostMapping
    @Operation(summary = "Score a sentence", description = "Compares the typed keys with the stored pace and does not write the attempt back. Accepted is true only when the percent is strictly above 95. A step within 35 percent of the stored mean, and at least 40 milliseconds, counts as a full match. A pair that was never stored is left out. Longer stored windows can change the percent and are not listed in steps.")
    @ApiResponse(responseCode = "200", description = "The match percent and the compared steps.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public MatchReport scoreSentence(@RequestBody MatchAttempt attempt) {
        return matcher.score(attempt);
    }
}
