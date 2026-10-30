package com.nordlyse.wordfrequency.account;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lines")
@RequiredArgsConstructor
@Tag(name = "Lines", description = "Lines the signed-in account has sent.")
@SecurityRequirement(name = "bearer")
public class SentLineController {

    private final AccountService accounts;

    @GetMapping
    @Operation(summary = "List sent lines", description = "Returns this account's lines in the order they were sent. Another account's lines are left out.")
    @ApiResponse(responseCode = "200", description = "The signed-in account's lines.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public List<SentLine> readLines(@Parameter(hidden = true) HttpServletRequest request) {
        return accounts.lines((String) request.getAttribute(AccountGate.ACCOUNT));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a line", description = "Stores one line for the signed-in account. The language is en, tr, de, fr, es, or sv.")
    @ApiResponse(responseCode = "201", description = "The line is stored.")
    @ApiResponse(responseCode = "400", description = "The language is unsupported, or the line is empty or longer than 1000 characters.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public SentLine addLine(@Parameter(hidden = true) HttpServletRequest request, @RequestBody SentLine line) {
        return accounts.addLine(
                (String) request.getAttribute(AccountGate.ACCOUNT),
                line.language(),
                line.text());
    }
}
