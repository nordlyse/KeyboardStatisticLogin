package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nordlyse.wordfrequency.account.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Stored letter gaps and word pauses.")
@SecurityRequirement(name = "bearer")
public class ProfileController {

    private final PaceProfileReader profiles;
    private final AccountService accounts;

    @GetMapping
    @Operation(summary = "Read the signed-in pace", description = "Returns this account's letter gaps and word pauses. Another account's rows are left out. A word row names the letters and up to six word pairs, such as t to n with cat to nap.")
    @ApiResponse(responseCode = "200", description = "This account's pace. Empty lists mean nothing is stored yet.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    public PaceProfile readProfile() {
        String username = ActiveProfile.orElse("");
        List<String> lines = username.isEmpty()
                ? List.of()
                : accounts.lines(username).stream().map(line -> line.text()).toList();
        return profiles.read(lines);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Read one account's pace", description = "Returns the letter gaps and word pauses for the account named in the path. The username is the account id. Matching ignores letter case.")
    @ApiResponse(responseCode = "200", description = "That account's pace. Empty lists mean nothing is stored yet.")
    @ApiResponse(responseCode = "400", description = "The username is outside the allowed shape.")
    @ApiResponse(responseCode = "401", description = "The bearer token is missing or no longer valid.")
    @ApiResponse(responseCode = "404", description = "That account is not registered.")
    public PaceProfile readAccount(
            @Parameter(description = "Account id. Matching ignores letter case.", example = "Ada")
            @PathVariable String username) {
        String name = accounts.accountName(username);
        List<String> lines = accounts.lines(name).stream().map(line -> line.text()).toList();
        return profiles.readAccount(name, lines);
    }
}
