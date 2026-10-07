package org.project;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // Nur anlegen, wenn es noch nicht existiert (wichtig für spätere Restarts)
        if (productRepository.findBySku("PS5-PRO").isEmpty()) {
            productRepository.save(new Product("PS5-PRO", "PlayStation 5 Pro", 10));
            System.out.println(" Test-Daten geladen: 10x PS5-PRO im Lager.");
        }
    }
}

