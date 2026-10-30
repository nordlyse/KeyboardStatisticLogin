package com.nordlyse.wordfrequency.account;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Register an account, list accounts, and remove one account with its pace.")
public class AccountController {

    private final AccountService accounts;
    private final AccountRemoval removal;

    @GetMapping
    @Operation(summary = "List accounts", description = "Returns every account as id, username, and the time it was added. The id is the username. The password hash is left out.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponse(responseCode = "200", description = "Every registered account.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public List<AccountSummary> listAccounts() {
        return accounts.listAccounts();
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an account", description = "Removes that account, its sessions, its sent lines, and its stored pace, including the keys still held in memory. A later registration of the same username starts empty. Another account is left in place.",
            security = @SecurityRequirement(name = "bearer"))
    @ApiResponse(responseCode = "204", description = "The account and its pace are gone.")
    @ApiResponse(responseCode = "400", description = "The username is outside the allowed shape.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    @ApiResponse(responseCode = "404", description = "That account is not registered.")
    public void removeAccount(
            @Parameter(description = "Account id. Matching ignores letter case.", example = "Ada")
            @PathVariable String username) {
        removal.removeAccount(username);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register an account", description = "Stores a new account. The password is kept only as a PBKDF2-SHA256 hash. This route stays open and does not need a token.")
    @ApiResponse(responseCode = "201", description = "The account is stored.")
    @ApiResponse(responseCode = "400", description = "The username or password is outside the allowed shape.")
    @ApiResponse(responseCode = "409", description = "That username is already registered.")
    public AccountName addAccount(@RequestBody AccountRequest request) {
        return new AccountName(accounts.addAccount(request.username(), request.password()));
    }
}
