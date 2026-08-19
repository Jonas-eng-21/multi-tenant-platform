package br.com.jonas.multitenant.auth.service;

import br.com.jonas.multitenant.auth.dto.LoginRequest;
import br.com.jonas.multitenant.tenant.entity.Tenant;
import br.com.jonas.multitenant.tenant.repository.TenantRepository;
import br.com.jonas.multitenant.user.entity.User;
import br.com.jonas.multitenant.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private AuthService authService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void authenticate_validCredentials_returnsToken() {
        Tenant tenant = new Tenant(tenantId, "Tenant A");
        User user = new User(userId, tenant, "admin", "hashed-password");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByTenantIdAndUsername(tenantId, "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
        when(jwtEncoder.encode(any())).thenReturn(createMockJwt("generated-token"));

        LoginRequest request = new LoginRequest(tenantId, "admin", "password");
        String token = authService.authenticate(request);

        assertEquals("generated-token", token);
    }

    @Test
    void authenticate_validCredentials_jwtContainsCorrectSub() {
        Tenant tenant = new Tenant(tenantId, "Tenant A");
        User user = new User(userId, tenant, "admin", "hashed-password");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByTenantIdAndUsername(tenantId, "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
        when(jwtEncoder.encode(any())).thenReturn(createMockJwt("token"));

        authService.authenticate(new LoginRequest(tenantId, "admin", "password"));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void authenticate_validCredentials_jwtContainsCorrectTenantId() {
        Tenant tenant = new Tenant(tenantId, "Tenant A");
        User user = new User(userId, tenant, "admin", "hashed-password");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByTenantIdAndUsername(tenantId, "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
        when(jwtEncoder.encode(any())).thenReturn(createMockJwt("token"));

        authService.authenticate(new LoginRequest(tenantId, "admin", "password"));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertEquals(tenantId.toString(), claims.getClaim("tenantId"));
    }

    @Test
    void authenticate_tenantNotFound_throwsBadCredentials() {
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest(tenantId, "admin", "password");

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        verifyNoInteractions(userRepository, passwordEncoder, jwtEncoder);
    }

    @Test
    void authenticate_userNotFound_throwsBadCredentials() {
        Tenant tenant = new Tenant(tenantId, "Tenant A");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByTenantIdAndUsername(tenantId, "admin")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest(tenantId, "admin", "password");

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        verifyNoInteractions(passwordEncoder, jwtEncoder);
    }

    @Test
    void authenticate_wrongPassword_throwsBadCredentials() {
        Tenant tenant = new Tenant(tenantId, "Tenant A");
        User user = new User(userId, tenant, "admin", "hashed-password");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.findByTenantIdAndUsername(tenantId, "admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        LoginRequest request = new LoginRequest(tenantId, "admin", "wrong-password");

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request));
        verifyNoInteractions(jwtEncoder);
    }

    private Jwt createMockJwt(String tokenValue) {
        return Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .claim("sub", "test")
                .build();
    }
}
