package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.Wallet;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class WalletPinSeedService {

    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedDefaultPins() {
        String defaultPin = "0000";
        String hashedPin = passwordEncoder.encode(defaultPin);
        List<Wallet> wallets = walletRepository.findAll().stream()
                .filter(w -> w.getPinHash() == null || w.getPinHash().isEmpty())
                .toList();
        for (Wallet wallet : wallets) {
            wallet.setPinHash(hashedPin);
            wallet.setPinChanged(false);
            walletRepository.save(wallet);
        }
        System.out.println("Seeding complete: " + wallets.size() + " wallets updated with 0000.");
    }
}