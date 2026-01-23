package com.teklif.app.config;

import com.teklif.app.entity.Tenant;
import com.teklif.app.entity.User;
import com.teklif.app.enums.Role;
import com.teklif.app.repository.TenantRepository;
import com.teklif.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (tenantRepository.count() > 0) {
            log.info("Data already seeded, skipping...");
            return;
        }

        log.info("Seeding initial data...");

        // Create demo tenant
        Tenant tenant = Tenant.builder()
                .name("Demo Company")
                .slug("demo-company")
                .email("demo@example.com")
                .phone("+90 555 123 4567")
                .taxNumber("1234567890")
                .taxOffice("Merkez VD")
                .address("İstanbul, Türkiye")
                .packageName("Premium")
                .maxUsers(50)
                .maxOffers(1000)
                .maxCustomers(500)
                .isActive(true)
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("✅ Created demo tenant: {}", tenant.getId());

        // Create super admin
        User superAdmin = User.builder()
                .tenantId(tenant.getId())
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Super")
                .lastName("Admin")
                .role(Role.SUPER_ADMIN)
                .isActive(true)
                .build();

        userRepository.save(superAdmin);
        log.info("✅ Created super admin user: {}", superAdmin.getEmail());

        // Create tenant admin
        User tenantAdmin = User.builder()
                .tenantId(tenant.getId())
                .email("tenant@example.com")
                .password(passwordEncoder.encode("tenant123"))
                .firstName("Tenant")
                .lastName("Admin")
                .role(Role.TENANT_ADMIN)
                .isActive(true)
                .build();

        userRepository.save(tenantAdmin);
        log.info("✅ Created tenant admin user: {}", tenantAdmin.getEmail());

        // Create regular user
        User regularUser = User.builder()
                .tenantId(tenant.getId())
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .firstName("Regular")
                .lastName("User")
                .role(Role.TENANT_USER)
                .isActive(true)
                .build();

        userRepository.save(regularUser);
        log.info("✅ Created regular user: {}", regularUser.getEmail());

        log.info("==========================================");
        log.info("🎉 Initial data seeding completed!");
        log.info("==========================================");
        log.info("Login credentials:");
        log.info("  👑 Super Admin:");
        log.info("     Email: admin@example.com");
        log.info("     Password: admin123");
        log.info("  👤 Tenant Admin:");
        log.info("     Email: tenant@example.com");
        log.info("     Password: tenant123");
        log.info("  👤 Regular User:");
        log.info("     Email: user@example.com");
        log.info("     Password: user123");
        log.info("==========================================");
    }
}