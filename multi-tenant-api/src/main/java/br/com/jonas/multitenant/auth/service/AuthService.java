package br.com.jonas.multitenant.auth.service;

import br.com.jonas.multitenant.auth.dto.LoginRequest;
import br.com.jonas.multitenant.tenant.entity.Tenant;
import br.com.jonas.multitenant.tenant.repository.TenantRepository;
import br.com.jonas.multitenant.user.entity.User;
import br.com.jonas.multitenant.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional(readOnly = true)
    public String authenticate(LoginRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        User user = userRepository.findByTenantIdAndUsername(tenant.getId(), request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return generateToken(user.getId(), tenant.getId());
    }

    private String generateToken(UUID userId, UUID tenantId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
