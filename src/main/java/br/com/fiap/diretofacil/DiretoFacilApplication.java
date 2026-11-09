package br.com.fiap.diretofacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/** Ponto de entrada da API; fixa o fuso de Brasília para alinhar as datas da JVM com o SYSDATE do Oracle. */
@SpringBootApplication
public class DiretoFacilApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(DiretoFacilApplication.class, args);
    }
}
