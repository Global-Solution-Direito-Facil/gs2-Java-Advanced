package br.com.fiap.direitofacil.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 401 e 403 barrados no filtro de segurança, antes de chegar ao controller, com o mesmo corpo
 * do ErroResponse do GlobalExceptionHandler (o @RestControllerAdvice não alcança os filtros).
 */
@Component
public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        escrever(response, request, HttpStatus.UNAUTHORIZED, "Token ausente, inválido ou expirado.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        escrever(response, request, HttpStatus.FORBIDDEN, "Você não tem permissão para esta operação.");
    }

    private void escrever(HttpServletResponse response, HttpServletRequest request,
                          HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status.value() + ","
                + "\"erro\":\"" + status.getReasonPhrase() + "\","
                + "\"mensagem\":\"" + mensagem + "\","
                + "\"path\":\"" + json(request.getRequestURI()) + "\","
                + "\"detalhes\":[]}");
    }

    private String json(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
