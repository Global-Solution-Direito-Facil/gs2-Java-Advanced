package br.com.fiap.direitofacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/** Ponto de entrada da API; fixa o fuso de Brasília para alinhar as datas da JVM com o SYSDATE do Oracle. */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableResilientMethods
public class DireitoFacilApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(DireitoFacilApplication.class, args);
    }
}
