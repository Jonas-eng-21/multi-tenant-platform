package br.com.jonas.multitenant.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "cpf", "CPF inválido");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("/api/test", response.getBody().path());
        assertNotNull(response.getBody().fieldErrors());
        assertEquals("CPF inválido", response.getBody().fieldErrors().get("cpf"));
    }

    @Test
    void handleHttpMessageNotReadable_returns400() {
        org.springframework.http.HttpInputMessage inputMessage = mock(org.springframework.http.HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", inputMessage);
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Corpo da requisição inválido ou malformado", response.getBody().message());
        assertNull(response.getBody().fieldErrors());
    }

    @Test
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("'id'"));
    }

    @Test
    void handleMissingParameter_returns400() {
        MissingServletRequestParameterException ex = mock(MissingServletRequestParameterException.class);
        when(ex.getParameterName()).thenReturn("tenantId");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMissingParameter(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("'tenantId'"));
    }

    @Test
    void handleConstraintViolation_returns400WithFieldErrors() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("metodo.parametro");
        when(violation.getMessage()).thenReturn("não pode ser nulo");
        when(ex.getConstraintViolations()).thenReturn(Set.of((ConstraintViolation) violation));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("não pode ser nulo", response.getBody().fieldErrors().get("parametro"));
    }

    @Test
    void handleNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Registro não encontrado");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Registro não encontrado", response.getBody().message());
    }

    @Test
    void handleNoResourceFound_returns404() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleNoResourceFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().message().contains("não encontrado para a rota"));
    }

    @Test
    void handleConflict_returns409() {
        ConflictException ex = new ConflictException("CPF já cadastrado");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CPF já cadastrado", response.getBody().message());
    }

    @Test
    void handleDataIntegrity_returns409Sanitized() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation at public.pessoa uk_cpf_pessoa");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDataIntegrity(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Violação de integridade de dados. A operação não pôde ser concluída.", response.getBody().message());
        assertFalse(response.getBody().message().contains("uk_cpf_pessoa")); // Garante sanitização
    }

    @Test
    void handleMethodNotSupported_returns405() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertTrue(response.getBody().message().contains("POST"));
    }

    @Test
    void handleMediaTypeNotSupported_returns415() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMediaTypeNotSupported(ex, request);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("Tipo de mídia não suportado", response.getBody().message());
    }

    @Test
    void handleAuthenticationException_returns401() {
        BadCredentialsException ex = new BadCredentialsException("Senha incorreta");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciais inválidas ou token de acesso ausente/expirado", response.getBody().message());
    }

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("Faltam privilégios");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acesso negado. Você não possui permissão para executar esta operação.", response.getBody().message());
    }

    @Test
    void handleUnexpectedException_returns500Sanitized() {
        RuntimeException ex = new RuntimeException("NullPointerException at ClassXYZ.java:123");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnexpectedException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.", response.getBody().message());
        assertFalse(response.getBody().message().contains("ClassXYZ")); // Garante sanitização
    }
}
