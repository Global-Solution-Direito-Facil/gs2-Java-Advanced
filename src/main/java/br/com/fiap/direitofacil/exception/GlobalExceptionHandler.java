package br.com.fiap.direitofacil.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validação falhou", "Campos inválidos na requisição", request, detalhes);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> handleMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<String> detalhes = ex.getAllErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validação falhou", "Parâmetros inválidos na requisição", request, detalhes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> detalhes = ex.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validação falhou", "Dados inválidos", request, detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Corpo inválido", "JSON malformado ou com valor fora do esperado", request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro ausente", "Parâmetro obrigatório: " + ex.getParameterName(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Tipo de parâmetro inválido", "Parâmetro inválido: " + ex.getName(),
                request, List.of("Valor recebido: " + ex.getValue()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, status.getReasonPhrase(), ex.getReason(), request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Email ou senha inválidos.", request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", "Você não tem permissão para esta operação.", request, List.of());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErroResponse> handleRotaInexistente(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found",
                "Rota não encontrada: " + request.getMethod() + " " + request.getRequestURI(), request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String suportados = ex.getSupportedHttpMethods() == null ? "" : " Métodos aceitos: " + ex.getSupportedHttpMethods() + ".";
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "O método " + ex.getMethod() + " não é aceito nesta rota." + suportados, request, List.of());
    }

    /** Erros vindos do banco: RAISE_APPLICATION_ERROR do PL/SQL vira 404, 409 ou 422; chave duplicada vira 409. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErroResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        SQLException sql = encontrarSqlException(ex);
        if (sql != null) {
            HttpStatus status = statusDoErroOracle(sql.getErrorCode());
            if (status != null) {
                return build(status, status.getReasonPhrase(), mensagemOracle(sql), request, List.of("ORA-" + sql.getErrorCode()));
            }
        }
        if (ex instanceof DataIntegrityViolationException) {
            return build(HttpStatus.CONFLICT, "Conflito de dados",
                    "Registro viola uma regra de integridade ou unicidade", request, List.of());
        }
        log.error("Erro de acesso a dados em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Falha ao acessar o banco de dados", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado",
                request, List.of(ex.getClass().getSimpleName()));
    }

    /** Códigos definidos nos packages e gatilhos do banco (README do DireitoFacil-DB, seção 7). */
    private HttpStatus statusDoErroOracle(int codigo) {
        return switch (codigo) {
            case 20020, 20022, 20023, 20040, 20041 -> HttpStatus.NOT_FOUND;
            case 20031, 20034 -> HttpStatus.UNPROCESSABLE_ENTITY;
            case 20032, 20033 -> HttpStatus.CONFLICT;
            case 1 -> HttpStatus.CONFLICT;
            default -> null;
        };
    }

    /** Primeira linha da mensagem do Oracle, sem o prefixo ORA-nnnnn e sem a pilha ORA-06512. */
    private String mensagemOracle(SQLException sql) {
        if (sql.getErrorCode() == 1) {
            return "Registro já existe.";
        }
        String mensagem = sql.getMessage() == null ? "" : sql.getMessage().lines().findFirst().orElse("");
        return mensagem.replaceFirst("^ORA-\\d+:\\s*", "").trim();
    }

    private SQLException encontrarSqlException(Throwable ex) {
        Throwable atual = ex;
        while (atual != null) {
            if (atual instanceof SQLException sql) {
                return sql;
            }
            atual = atual.getCause();
        }
        return null;
    }

    private String formatarErroCampo(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErroResponse> build(HttpStatus status, String erro, String mensagem,
                                               HttpServletRequest request, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                erro,
                mensagem,
                request.getRequestURI(),
                detalhes
        ));
    }
}
