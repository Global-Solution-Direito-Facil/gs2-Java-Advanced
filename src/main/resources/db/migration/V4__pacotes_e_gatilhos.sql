-- Flyway V4: pacotes (logica de negocio) e gatilhos. Mesmo conteudo de 06_PACKAGES.sql, sem testes.

------------------------------------------------------------
-- PACKAGE 1: PKG_AUDITORIA
------------------------------------------------------------
CREATE OR REPLACE PACKAGE PKG_AUDITORIA IS

    PROCEDURE REGISTRAR_AUDITORIA_CASO (
        p_tipo_operacao      IN VARCHAR2,
        p_id_caso_ref        IN NUMBER,
        p_status_anterior    IN VARCHAR2,
        p_status_novo        IN VARCHAR2,
        p_valores_anteriores IN VARCHAR2,
        p_valores_novos      IN VARCHAR2
    );

END PKG_AUDITORIA;
/

CREATE OR REPLACE PACKAGE BODY PKG_AUDITORIA IS

    PROCEDURE REGISTRAR_AUDITORIA_CASO (
        p_tipo_operacao      IN VARCHAR2,
        p_id_caso_ref        IN NUMBER,
        p_status_anterior    IN VARCHAR2,
        p_status_novo        IN VARCHAR2,
        p_valores_anteriores IN VARCHAR2,
        p_valores_novos      IN VARCHAR2
    )
    IS
    BEGIN
        INSERT INTO T_DF_AUDITORIA_CASO (NOME_USUARIO, TIPO_OPERACAO, ID_CASO_REF, STATUS_ANTERIOR,
                                          STATUS_NOVO, VALORES_ANTERIORES, VALORES_NOVOS)
        VALUES (USER, p_tipo_operacao, p_id_caso_ref, p_status_anterior,
                p_status_novo, p_valores_anteriores, p_valores_novos);
    END REGISTRAR_AUDITORIA_CASO;

END PKG_AUDITORIA;
/

------------------------------------------------------------
-- PACKAGE: PKG_ELEGIBILIDADE
-- Motor de regras: renda per capita, avaliacao de cada direito e abertura de casos
------------------------------------------------------------
CREATE OR REPLACE PACKAGE PKG_ELEGIBILIDADE IS

    FUNCTION FN_RENDA_PER_CAPITA_SM (
        p_id_pessoa IN NUMBER
    ) RETURN NUMBER;

    PROCEDURE PRC_AVALIAR_DIREITO (
        p_id_pessoa  IN  NUMBER,
        p_id_direito IN  NUMBER,
        p_dt_ref     IN  DATE,
        p_status     OUT VARCHAR2,
        p_motivo     OUT VARCHAR2
    );

    FUNCTION FN_AVALIAR_ELEGIBILIDADE (
        p_id_pessoa   IN NUMBER,
        p_cod_direito IN VARCHAR2,
        p_dt_ref      IN DATE DEFAULT SYSDATE
    ) RETURN VARCHAR2;

    PROCEDURE PRC_ABRIR_CASOS (
        p_id_pessoa IN NUMBER,
        p_dt_ref    IN DATE DEFAULT SYSDATE
    );

END PKG_ELEGIBILIDADE;
/

CREATE OR REPLACE PACKAGE BODY PKG_ELEGIBILIDADE IS

FUNCTION FN_RENDA_PER_CAPITA_SM (
    p_id_pessoa IN NUMBER
) RETURN NUMBER
IS
    v_renda    T_DF_PESSOA.VLR_RENDA_FAMILIAR%TYPE;
    v_membros  T_DF_PESSOA.QTD_MEMBROS_FAMILIA%TYPE;
    v_sm       T_DF_SALARIO_MINIMO.VLR_SALARIO_MINIMO%TYPE;
BEGIN
    SELECT VLR_RENDA_FAMILIAR, QTD_MEMBROS_FAMILIA
    INTO v_renda, v_membros
    FROM T_DF_PESSOA
    WHERE ID_PESSOA = p_id_pessoa;

    SELECT VLR_SALARIO_MINIMO INTO v_sm
    FROM T_DF_SALARIO_MINIMO
    WHERE ANO_REFERENCIA = (SELECT MAX(ANO_REFERENCIA) FROM T_DF_SALARIO_MINIMO);

    RETURN ROUND((v_renda / v_membros) / v_sm, 4);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20020, 'FN_RENDA_PER_CAPITA_SM: pessoa ou salario minimo nao encontrado');
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20021, 'FN_RENDA_PER_CAPITA_SM: erro inesperado - ' || SQLERRM);
END FN_RENDA_PER_CAPITA_SM;

PROCEDURE PRC_AVALIAR_DIREITO (
    p_id_pessoa  IN  NUMBER,
    p_id_direito IN  NUMBER,
    p_dt_ref     IN  DATE,
    p_status     OUT VARCHAR2,
    p_motivo     OUT VARCHAR2
)
IS
    v_dt_nasc  DATE;
    v_idade    NUMBER;
    v_pcd      NUMBER;
    v_tea      NUMBER;
    v_fracao   NUMBER;
    v_crit     T_DF_CRITERIO_DIREITO%ROWTYPE;
    v_txt_f    VARCHAR2(10);
    v_txt_l    VARCHAR2(10);
BEGIN
    SELECT DT_NASCIMENTO INTO v_dt_nasc
    FROM T_DF_PESSOA WHERE ID_PESSOA = p_id_pessoa;

    SELECT * INTO v_crit
    FROM T_DF_CRITERIO_DIREITO WHERE ID_DIREITO = p_id_direito;

    v_idade := TRUNC(MONTHS_BETWEEN(p_dt_ref, v_dt_nasc) / 12);

    -- Lei 12.764/2012, art. 1o, par. 2o: laudo de TEA conta como deficiencia
    SELECT COUNT(*) INTO v_pcd
    FROM T_DF_LAUDO
    WHERE ID_PESSOA = p_id_pessoa AND TIPO_CONDICAO <> 'OUTRA';

    SELECT COUNT(*) INTO v_tea
    FROM T_DF_LAUDO
    WHERE ID_PESSOA = p_id_pessoa AND TIPO_CONDICAO = 'TEA';

    v_fracao := FN_RENDA_PER_CAPITA_SM(p_id_pessoa);
    v_txt_f  := TO_CHAR(v_fracao, 'FM990D0000', 'NLS_NUMERIC_CHARACTERS=''.,''');
    v_txt_l  := TO_CHAR(v_crit.FRC_SM_MAX, 'FM990D0000', 'NLS_NUMERIC_CHARACTERS=''.,''');

    IF v_idade < v_crit.NUM_IDADE_MIN
       OR (v_crit.NUM_IDADE_MAX IS NOT NULL AND v_idade > v_crit.NUM_IDADE_MAX) THEN
        p_status := 'NAO_ELEGIVEL';
        p_motivo := 'Idade ' || v_idade || ' fora da faixa exigida';
    ELSIF v_crit.STA_EXIGE_TEA = 'S' AND v_tea = 0 THEN
        p_status := 'NAO_ELEGIVEL';
        p_motivo := 'Exige laudo de TEA (Lei 12.764/2012)';
    ELSIF v_crit.STA_EXIGE_PCD = 'S' AND v_pcd = 0 THEN
        p_status := 'NAO_ELEGIVEL';
        p_motivo := 'Exige laudo de deficiencia (PcD)';
    ELSIF v_crit.FRC_SM_MAX IS NULL THEN
        p_status := 'ELEGIVEL';
        p_motivo := 'Criterios atendidos (sem limite de renda)';
    ELSIF v_fracao <= v_crit.FRC_SM_MAX THEN
        p_status := 'ELEGIVEL';
        p_motivo := 'Renda per capita ' || v_txt_f || ' SM dentro do limite de ' || v_txt_l || ' SM';
    ELSIF v_fracao <= v_crit.FRC_SM_MAX * (1 + v_crit.PCT_TOLERANCIA / 100) THEN
        p_status := 'POSSIVEL';
        p_motivo := 'Renda per capita ' || v_txt_f || ' SM proxima do limite de ' || v_txt_l
                    || ' SM; confirmar renda no CadUnico';
    ELSE
        p_status := 'NAO_ELEGIVEL';
        p_motivo := 'Renda per capita ' || v_txt_f || ' SM acima do limite de ' || v_txt_l || ' SM';
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20022, 'PRC_AVALIAR_DIREITO: pessoa, direito ou criterio nao encontrado');
END PRC_AVALIAR_DIREITO;

FUNCTION FN_AVALIAR_ELEGIBILIDADE (
    p_id_pessoa   IN NUMBER,
    p_cod_direito IN VARCHAR2,
    p_dt_ref      IN DATE
) RETURN VARCHAR2
IS
    v_id_direito  T_DF_DIREITO.ID_DIREITO%TYPE;
    v_status      VARCHAR2(15);
    v_motivo      VARCHAR2(300);
BEGIN
    SELECT ID_DIREITO INTO v_id_direito
    FROM T_DF_DIREITO
    WHERE COD_DIREITO = UPPER(p_cod_direito);

    PRC_AVALIAR_DIREITO(p_id_pessoa, v_id_direito, p_dt_ref, v_status, v_motivo);
    RETURN v_status;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20023, 'FN_AVALIAR_ELEGIBILIDADE: direito ' || p_cod_direito || ' nao encontrado');
END FN_AVALIAR_ELEGIBILIDADE;

PROCEDURE PRC_ABRIR_CASOS (
    p_id_pessoa IN NUMBER,
    p_dt_ref    IN DATE
)
IS
    CURSOR c_direitos IS
        SELECT ID_DIREITO, COD_DIREITO, NUM_PRAZO_DIAS
        FROM T_DF_DIREITO
        ORDER BY ID_DIREITO;

    v_status    VARCHAR2(15);
    v_motivo    VARCHAR2(300);
    v_ja_existe NUMBER;
    v_abertos   NUMBER := 0;
    v_erro      VARCHAR2(4000);
    v_codigo    NUMBER;
BEGIN
    FOR r IN c_direitos LOOP
        PRC_AVALIAR_DIREITO(p_id_pessoa, r.ID_DIREITO, p_dt_ref, v_status, v_motivo);

        IF v_status IN ('ELEGIVEL', 'POSSIVEL') THEN
            SELECT COUNT(*) INTO v_ja_existe
            FROM T_DF_CASO
            WHERE ID_PESSOA = p_id_pessoa AND ID_DIREITO = r.ID_DIREITO;

            IF v_ja_existe = 0 THEN
                INSERT INTO T_DF_CASO (ID_PESSOA, ID_DIREITO, STATUS, RESULTADO_AVALIACAO,
                                        DES_MOTIVO, DT_ABERTURA, DT_PRAZO, DT_ATUALIZACAO)
                VALUES (p_id_pessoa, r.ID_DIREITO, 'ABERTO', v_status,
                        v_motivo, p_dt_ref, p_dt_ref + r.NUM_PRAZO_DIAS, p_dt_ref);
                v_abertos := v_abertos + 1;
            END IF;
        END IF;
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('Pessoa ' || p_id_pessoa || ': ' || v_abertos || ' caso(s) aberto(s)');
EXCEPTION
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_ABRIR_CASOS', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END PRC_ABRIR_CASOS;

END PKG_ELEGIBILIDADE;
/

------------------------------------------------------------
-- PACKAGE: PKG_CASOS
-- Andamento dos casos, orientacao da IA, relatorio e conversao manual para JSON (ponte Oracle -> MongoDB)
------------------------------------------------------------
CREATE OR REPLACE PACKAGE PKG_CASOS IS

    PROCEDURE PRC_ATUALIZAR_STATUS_CASO (
        p_id_caso     IN NUMBER,
        p_novo_status IN VARCHAR2,
        p_dt_ref      IN DATE DEFAULT SYSDATE
    );

    PROCEDURE PRC_REGISTRAR_ORIENTACAO (
        p_id_caso        IN NUMBER,
        p_passo_a_passo  IN VARCHAR2,
        p_fonte          IN VARCHAR2,
        p_modelo         IN VARCHAR2
    );

    FUNCTION FN_CASO_TO_JSON (
        p_id_caso IN NUMBER
    ) RETURN VARCHAR2;

    PROCEDURE PRC_EXPORTA_CASOS_JSON;

    PROCEDURE PRC_REL_CASOS_POR_STATUS;

END PKG_CASOS;
/

CREATE OR REPLACE PACKAGE BODY PKG_CASOS IS

PROCEDURE PRC_ATUALIZAR_STATUS_CASO (
    p_id_caso     IN NUMBER,
    p_novo_status IN VARCHAR2,
    p_dt_ref      IN DATE
)
IS
    v_erro     VARCHAR2(4000);
    v_codigo   NUMBER;
BEGIN
    UPDATE T_DF_CASO
    SET STATUS = UPPER(p_novo_status),
        DT_ATUALIZACAO = p_dt_ref
    WHERE ID_CASO = p_id_caso;

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20040, 'Caso ' || p_id_caso || ' nao encontrado');
    END IF;

    DBMS_OUTPUT.PUT_LINE('Caso ' || p_id_caso || ' -> ' || UPPER(p_novo_status));
EXCEPTION
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_ATUALIZAR_STATUS_CASO', v_codigo, v_erro, SYSDATE, USER);
        RAISE;
END PRC_ATUALIZAR_STATUS_CASO;

PROCEDURE PRC_REGISTRAR_ORIENTACAO (
    p_id_caso        IN NUMBER,
    p_passo_a_passo  IN VARCHAR2,
    p_fonte          IN VARCHAR2,
    p_modelo         IN VARCHAR2
)
IS
    v_qtd      NUMBER;
    v_erro     VARCHAR2(4000);
    v_codigo   NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM T_DF_CASO WHERE ID_CASO = p_id_caso;
    IF v_qtd = 0 THEN
        RAISE_APPLICATION_ERROR(-20041, 'Caso ' || p_id_caso || ' nao encontrado');
    END IF;

    -- insere a orientacao ou, se o caso ja tem uma, substitui pela nova (IA regenerou)
    MERGE INTO T_DF_ORIENTACAO o
    USING (SELECT p_id_caso AS ID_CASO FROM DUAL) s
    ON (o.ID_CASO = s.ID_CASO)
    WHEN MATCHED THEN
        UPDATE SET o.DES_PASSO_A_PASSO = p_passo_a_passo,
                   o.DES_FONTE = p_fonte,
                   o.DES_MODELO = p_modelo,
                   o.DT_GERACAO = SYSDATE
    WHEN NOT MATCHED THEN
        INSERT (ID_CASO, DES_PASSO_A_PASSO, DES_FONTE, DES_MODELO)
        VALUES (p_id_caso, p_passo_a_passo, p_fonte, p_modelo);

    DBMS_OUTPUT.PUT_LINE('Orientacao registrada para o caso ' || p_id_caso);
EXCEPTION
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_REGISTRAR_ORIENTACAO', v_codigo, v_erro, SYSDATE, USER);
        RAISE;
END PRC_REGISTRAR_ORIENTACAO;

FUNCTION FN_CASO_TO_JSON (
    p_id_caso IN NUMBER
) RETURN VARCHAR2
IS
    v_caso      T_DF_CASO%ROWTYPE;
    v_nome      T_DF_PESSOA.NOME%TYPE;
    v_cod       T_DF_DIREITO.COD_DIREITO%TYPE;
    v_nome_dir  T_DF_DIREITO.NOME_DIREITO%TYPE;
    v_base      T_DF_DIREITO.DES_BASE_LEGAL%TYPE;
    v_grupo     T_DF_DIREITO.GRUPO_ALVO%TYPE;
    v_hist      VARCHAR2(3000) := '';
    v_sep       VARCHAR2(1) := '';

    CURSOR c_hist IS
        SELECT TIPO_OPERACAO, STATUS_ANTERIOR, STATUS_NOVO
        FROM T_DF_AUDITORIA_CASO
        WHERE ID_CASO_REF = p_id_caso
        ORDER BY ID_AUDITORIA;

    -- texto entre aspas, com escape manual de aspas duplas; NULL vira null
    FUNCTION f_str (p IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        IF p IS NULL THEN
            RETURN 'null';
        END IF;
        RETURN '"' || REPLACE(p, '"', '\"') || '"';
    END f_str;

    -- data no formato Extended JSON do MongoDB
    FUNCTION f_data (p IN DATE) RETURN VARCHAR2 IS
    BEGIN
        RETURN '{"$date":"' || TO_CHAR(p, 'YYYY-MM-DD"T"HH24:MI:SS') || '.000Z"}';
    END f_data;
BEGIN
    IF p_id_caso IS NULL OR p_id_caso <= 0 THEN
        RAISE_APPLICATION_ERROR(-20050, 'FN_CASO_TO_JSON: id do caso invalido (nulo ou <= 0)');
    END IF;

    SELECT * INTO v_caso FROM T_DF_CASO WHERE ID_CASO = p_id_caso;
    SELECT NOME INTO v_nome FROM T_DF_PESSOA WHERE ID_PESSOA = v_caso.ID_PESSOA;
    SELECT COD_DIREITO, NOME_DIREITO, DES_BASE_LEGAL, GRUPO_ALVO
    INTO v_cod, v_nome_dir, v_base, v_grupo
    FROM T_DF_DIREITO WHERE ID_DIREITO = v_caso.ID_DIREITO;

    FOR h IN c_hist LOOP
        v_hist := v_hist || v_sep
            || '{"operacao":' || f_str(h.TIPO_OPERACAO)
            || ',"statusAnterior":' || f_str(h.STATUS_ANTERIOR)
            || ',"statusNovo":' || f_str(h.STATUS_NOVO) || '}';
        v_sep := ',';
    END LOOP;

    RETURN '{"_id":' || v_caso.ID_CASO
        || ',"pessoa":{"idPessoa":' || v_caso.ID_PESSOA || ',"nome":' || f_str(v_nome) || '}'
        || ',"direito":{"idDireito":' || v_caso.ID_DIREITO
            || ',"codigo":' || f_str(v_cod)
            || ',"nome":' || f_str(v_nome_dir)
            || ',"baseLegal":' || f_str(v_base)
            || ',"grupoAlvo":' || f_str(v_grupo) || '}'
        || ',"status":' || f_str(v_caso.STATUS)
        || ',"avaliacao":{"resultado":' || f_str(v_caso.RESULTADO_AVALIACAO)
            || ',"motivo":' || f_str(v_caso.DES_MOTIVO) || '}'
        || ',"dtAbertura":' || f_data(v_caso.DT_ABERTURA)
        || ',"dtPrazo":' || f_data(v_caso.DT_PRAZO)
        || ',"dtAtualizacao":' || f_data(v_caso.DT_ATUALIZACAO)
        || ',"historico":[' || v_hist || ']}';
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20051, 'FN_CASO_TO_JSON: caso ' || p_id_caso || ' nao encontrado');
END FN_CASO_TO_JSON;

PROCEDURE PRC_EXPORTA_CASOS_JSON
IS
    v_primeiro BOOLEAN := TRUE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('[');
    FOR r IN (SELECT ID_CASO FROM T_DF_CASO ORDER BY ID_CASO) LOOP
        IF v_primeiro THEN
            v_primeiro := FALSE;
            DBMS_OUTPUT.PUT_LINE(FN_CASO_TO_JSON(r.ID_CASO));
        ELSE
            DBMS_OUTPUT.PUT_LINE(',' || FN_CASO_TO_JSON(r.ID_CASO));
        END IF;
    END LOOP;
    DBMS_OUTPUT.PUT_LINE(']');
END PRC_EXPORTA_CASOS_JSON;

PROCEDURE PRC_REL_CASOS_POR_STATUS
IS
    v_resultado_atual  VARCHAR2(15) := NULL;
    v_subtotal         NUMBER := 0;
    v_total            NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== CASOS POR RESULTADO DA AVALIACAO E STATUS (subtotal manual) ===');
    FOR r IN (SELECT RESULTADO_AVALIACAO, STATUS, COUNT(*) AS QTD
              FROM T_DF_CASO
              GROUP BY RESULTADO_AVALIACAO, STATUS
              ORDER BY RESULTADO_AVALIACAO, STATUS) LOOP
        IF v_resultado_atual IS NOT NULL AND v_resultado_atual <> r.RESULTADO_AVALIACAO THEN
            DBMS_OUTPUT.PUT_LINE('   Subtotal ' || v_resultado_atual || ': ' || v_subtotal);
            v_subtotal := 0;
        END IF;
        v_resultado_atual := r.RESULTADO_AVALIACAO;
        DBMS_OUTPUT.PUT_LINE(RPAD(r.RESULTADO_AVALIACAO, 10) || RPAD(r.STATUS, 24) || r.QTD);
        v_subtotal := v_subtotal + r.QTD;
        v_total    := v_total + r.QTD;
    END LOOP;
    IF v_resultado_atual IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE('   Subtotal ' || v_resultado_atual || ': ' || v_subtotal);
    END IF;
    DBMS_OUTPUT.PUT_LINE('TOTAL GERAL: ' || v_total);
END PRC_REL_CASOS_POR_STATUS;

END PKG_CASOS;
/

------------------------------------------------------------
-- TRIGGER recriada: agora delega a gravacao para PKG_AUDITORIA
------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_DF_AUDITORIA_CASO
AFTER INSERT OR UPDATE OR DELETE ON T_DF_CASO
FOR EACH ROW
DECLARE
    v_tipo     VARCHAR2(10);
    v_anterior VARCHAR2(1000);
    v_novo     VARCHAR2(1000);
    v_st_ant   VARCHAR2(25);
    v_st_novo  VARCHAR2(25);
    v_id_ref   NUMBER;
BEGIN
    IF INSERTING THEN
        v_tipo     := 'INSERT';
        v_id_ref   := :NEW.ID_CASO;
        v_st_novo  := :NEW.STATUS;
        v_novo     := 'PESSOA=' || :NEW.ID_PESSOA || ';DIREITO=' || :NEW.ID_DIREITO
                      || ';STATUS=' || :NEW.STATUS || ';RESULTADO=' || :NEW.RESULTADO_AVALIACAO
                      || ';PRAZO=' || TO_CHAR(:NEW.DT_PRAZO, 'YYYY-MM-DD');
    ELSIF UPDATING THEN
        v_tipo     := 'UPDATE';
        v_id_ref   := :NEW.ID_CASO;
        v_st_ant   := :OLD.STATUS;
        v_st_novo  := :NEW.STATUS;
        v_anterior := 'STATUS=' || :OLD.STATUS || ';PRAZO=' || TO_CHAR(:OLD.DT_PRAZO, 'YYYY-MM-DD');
        v_novo     := 'STATUS=' || :NEW.STATUS || ';PRAZO=' || TO_CHAR(:NEW.DT_PRAZO, 'YYYY-MM-DD');
    ELSIF DELETING THEN
        v_tipo     := 'DELETE';
        v_id_ref   := :OLD.ID_CASO;
        v_st_ant   := :OLD.STATUS;
        v_anterior := 'PESSOA=' || :OLD.ID_PESSOA || ';DIREITO=' || :OLD.ID_DIREITO
                      || ';STATUS=' || :OLD.STATUS;
    END IF;

    PKG_AUDITORIA.REGISTRAR_AUDITORIA_CASO(v_tipo, v_id_ref, v_st_ant, v_st_novo, v_anterior, v_novo);
END;
/

------------------------------------------------------------
-- GATILHO TRG_DF_CASO_VALIDA (fluxo de status e prazo)
------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_DF_CASO_VALIDA
BEFORE INSERT OR UPDATE ON T_DF_CASO
FOR EACH ROW
BEGIN
    -- regra 1: prazo nunca anterior a abertura
    IF :NEW.DT_PRAZO < :NEW.DT_ABERTURA THEN
        RAISE_APPLICATION_ERROR(-20034, 'Prazo do caso nao pode ser anterior a data de abertura');
    END IF;

    -- regra 2: todo caso nasce ABERTO
    IF INSERTING AND :NEW.STATUS <> 'ABERTO' THEN
        RAISE_APPLICATION_ERROR(-20031, 'Caso novo deve ser criado com status ABERTO');
    END IF;

    -- regra 3: fluxo de status
    --   ABERTO / EM_ANALISE / DOCUMENTOS_PENDENTES -> avancam entre si ou para PROTOCOLADO
    --   PROTOCOLADO -> somente CONCEDIDO ou NEGADO
    --   CONCEDIDO / NEGADO -> estados finais (nao mudam mais)
    IF UPDATING AND :OLD.STATUS <> :NEW.STATUS THEN
        IF :OLD.STATUS IN ('CONCEDIDO', 'NEGADO') THEN
            RAISE_APPLICATION_ERROR(-20032, 'Caso ' || :OLD.STATUS || ' e final e nao pode mudar de status');
        ELSIF :OLD.STATUS = 'PROTOCOLADO' THEN
            IF :NEW.STATUS NOT IN ('CONCEDIDO', 'NEGADO') THEN
                RAISE_APPLICATION_ERROR(-20033, 'De PROTOCOLADO so e permitido CONCEDIDO ou NEGADO');
            END IF;
        ELSE
            IF :NEW.STATUS IN ('CONCEDIDO', 'NEGADO', 'ABERTO') THEN
                RAISE_APPLICATION_ERROR(-20033, 'Transicao invalida: ' || :OLD.STATUS || ' -> ' || :NEW.STATUS
                    || ' (e preciso passar por PROTOCOLADO)');
            END IF;
        END IF;
    END IF;
END;
/
