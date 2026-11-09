package br.com.fiap.diretofacil.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Converte Boolean para CHAR(1) 'S'/'N' no Oracle. */
@Converter
public class BooleanToCharacterConverter implements AttributeConverter<Boolean, Character> {

    @Override
    public Character convertToDatabaseColumn(Boolean valor) {
        if (valor == null) {
            return null;
        }
        return valor ? 'S' : 'N';
    }

    @Override
    public Boolean convertToEntityAttribute(Character coluna) {
        if (coluna == null) {
            return null;
        }
        return Character.toUpperCase(coluna) == 'S';
    }
}
