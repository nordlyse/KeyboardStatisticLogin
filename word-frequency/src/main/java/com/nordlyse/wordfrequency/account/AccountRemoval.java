package com.nordlyse.wordfrequency.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nordlyse.wordfrequency.keystrike.KeystrikeAnalysis;
import com.nordlyse.wordfrequency.keystrike.NgramTransitionStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountRemoval {

    private final AccountService accounts;
    private final AccountStore store;
    private final NgramTransitionStore ngrams;
    private final KeystrikeAnalysis analysis;

    @Transactional
    public void removeAccount(String username) {
        String name = accounts.accountName(username);
        store.removeAccount(name);
        ngrams.removeProfile(name);
        analysis.dropWindow(name);
    }
}
