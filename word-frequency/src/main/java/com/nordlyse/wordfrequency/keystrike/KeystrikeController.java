package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

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
@RequestMapping("/keystrikes")
@RequiredArgsConstructor
@Tag(name = "Keystrikes", description = "Record one key on the signed-in account's pace.")
@SecurityRequirement(name = "bearer")
public class KeystrikeController {

    private final KeystrikeAnalysis analysis;

    @PostMapping
    @Operation(summary = "Record a key", description = "Updates every sequence length from 1 through 10 that ends on this key. A space is kept. Backspace, Delete, or correction true counts an error. A corrected character is left out of the following chain. The response lists the sequences updated by this key.")
    @ApiResponse(responseCode = "200", description = "The sequences updated by this key.")
    @ApiResponse(responseCode = "400", description = "The symbol is missing or is more than one character.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public List<NgramTransition> addKeystrike(@RequestBody KeystrikeRequest request) {
        return analysis.addKeystrike(request);
    }
}
