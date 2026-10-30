package com.nordlyse.wordfrequency.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Open a visit, read who is signed in, and close the visit.")
public class SessionController {

    private final AccountService accounts;

    @PostMapping
    @Operation(summary = "Open a session", description = "Checks the username and password and returns a bearer token. An unknown user and a wrong password get the same refusal. This route stays open.")
    @ApiResponse(responseCode = "200", description = "The visit is open.")
    @ApiResponse(responseCode = "401", description = "That username or password does not match.")
    public SessionOpened openSession(@RequestBody AccountRequest request) {
        return accounts.openSession(request.username(), request.password());
    }

    @GetMapping
    @Operation(summary = "Read the current session", description = "Returns the username for the bearer token.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponse(responseCode = "200", description = "The signed-in account.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public AccountName readSession(@Parameter(hidden = true) HttpServletRequest request) {
        return new AccountName((String) request.getAttribute(AccountGate.ACCOUNT));
    }

    @DeleteMapping
    @Operation(summary = "Close the session", description = "Forgets the bearer token. The account, lines, and pace stay.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponse(responseCode = "204", description = "The visit is closed.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public ResponseEntity<Void> closeSession(@Parameter(hidden = true) HttpServletRequest request) {
        accounts.closeSession((String) request.getAttribute(AccountGate.TOKEN));
        return ResponseEntity.noContent().build();
    }
}
