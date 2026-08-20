package br.com.jonas.multitenant.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityErrorHandlingTest {

    @Mock
    private HandlerExceptionResolver resolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void customAuthenticationEntryPoint_resolvesException() throws IOException, ServletException {
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint(resolver);
        BadCredentialsException ex = new BadCredentialsException("Invalid");

        entryPoint.commence(request, response, ex);

        verify(resolver).resolveException(request, response, null, ex);
    }

    @Test
    void customAccessDeniedHandler_resolvesException() throws IOException, ServletException {
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler(resolver);
        AccessDeniedException ex = new AccessDeniedException("Denied");

        handler.handle(request, response, ex);

        verify(resolver).resolveException(request, response, null, ex);
    }
}
