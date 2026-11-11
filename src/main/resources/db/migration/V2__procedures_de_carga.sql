------------------------------------------------------------
-- PROJETO: DIREITO FACIL - CITIZENTECH (GLOBAL SOLUTION 2026/2)
-- DISCIPLINA: MASTERING RELATIONAL AND NON-RELATIONAL DATABASE
-- ARQUIVO: 02_PROCEDURES.sql
-- DESCRICAO: PROCEDURES DE CARGA (UMA POR TABELA DE DADOS)
-- EXECUCAO: 2 - executar apos 01_DDL.sql
------------------------------------------------------------


------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_USUARIO
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_USUARIO (
    p_nome   IN VARCHAR2,
    p_cpf    IN VARCHAR2,
    p_email  IN VARCHAR2,
    p_senha  IN VARCHAR2,
    p_perfil IN VARCHAR2
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    INSERT INTO T_DF_USUARIO (NOME, CPF, EMAIL, SENHA, PERFIL)
    VALUES (p_nome, p_cpf, LOWER(p_email), p_senha, UPPER(p_perfil));
    DBMS_OUTPUT.PUT_LINE('Usuario cadastrado: ' || p_nome);
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_USUARIO', 1, 'Usuario ja cadastrado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Usuario ja cadastrado');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_USUARIO', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_USUARIO', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_SALARIO_MINIMO
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_SALARIO_MINIMO (
    p_ano   IN NUMBER,
    p_valor IN NUMBER,
    p_fonte IN VARCHAR2
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    INSERT INTO T_DF_SALARIO_MINIMO (ANO_REFERENCIA, VLR_SALARIO_MINIMO, DES_FONTE)
    VALUES (p_ano, p_valor, p_fonte);
    DBMS_OUTPUT.PUT_LINE('Salario minimo cadastrado');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_SALARIO_MINIMO', 1, 'Salario minimo do ano ja cadastrado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Salario minimo do ano ja cadastrado');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_SALARIO_MINIMO', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_SALARIO_MINIMO', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_PESSOA
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_PESSOA (
    p_id_usuario IN NUMBER,
    p_nome       IN VARCHAR2,
    p_dt_nasc    IN DATE,
    p_renda      IN NUMBER,
    p_membros    IN NUMBER,
    p_renda_individual IN NUMBER
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM T_DF_USUARIO WHERE ID_USUARIO = p_id_usuario;
    IF v_qtd = 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Usuario responsavel nao encontrado');
    END IF;
    INSERT INTO T_DF_PESSOA (ID_USUARIO, NOME, DT_NASCIMENTO, VLR_RENDA_FAMILIAR, QTD_MEMBROS_FAMILIA, VLR_RENDA_INDIVIDUAL)
    VALUES (p_id_usuario, p_nome, p_dt_nasc, p_renda, p_membros, p_renda_individual);
    DBMS_OUTPUT.PUT_LINE('Pessoa cadastrada');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_PESSOA', 1, 'Pessoa ja cadastrada', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Pessoa ja cadastrada');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_PESSOA', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_PESSOA', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_DOCUMENTO
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_DOCUMENTO (
    p_id_pessoa  IN NUMBER,
    p_id_externo IN VARCHAR2,
    p_arquivo    IN VARCHAR2,
    p_tipo_midia IN VARCHAR2,
    p_tamanho    IN NUMBER,
    p_chave_idem IN VARCHAR2,
    p_status     IN VARCHAR2,
    p_dt_receb   IN DATE,
    p_dt_proc    IN DATE
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM T_DF_PESSOA WHERE ID_PESSOA = p_id_pessoa;
    IF v_qtd = 0 THEN
        RAISE_APPLICATION_ERROR(-20013, 'Pessoa nao encontrada');
    END IF;
    INSERT INTO T_DF_DOCUMENTO (ID_PESSOA, DES_ID_EXTERNO, DES_NOME_ARQUIVO, DES_TIPO_MIDIA, NUM_TAMANHO_BYTES, DES_CHAVE_IDEMPOTENCIA, STATUS, DT_RECEBIMENTO, DT_PROCESSAMENTO)
    VALUES (p_id_pessoa, p_id_externo, p_arquivo, p_tipo_midia, p_tamanho, p_chave_idem, UPPER(p_status), p_dt_receb, p_dt_proc);
    DBMS_OUTPUT.PUT_LINE('Documento cadastrado');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_DOCUMENTO', 1, 'Documento ja registrado (id externo ou chave de idempotencia repetidos)', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Documento ja registrado (id externo ou chave de idempotencia repetidos)');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_DOCUMENTO', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_DOCUMENTO', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_LAUDO
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_LAUDO (
    p_id_pessoa    IN NUMBER,
    p_id_documento IN NUMBER,
    p_cod_cid      IN VARCHAR2,
    p_condicao     IN VARCHAR2,
    p_tipo         IN VARCHAR2,
    p_dt_laudo     IN DATE
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM T_DF_PESSOA WHERE ID_PESSOA = p_id_pessoa;
    IF v_qtd = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Pessoa nao encontrada');
    END IF;
    IF p_id_documento IS NOT NULL THEN
        SELECT COUNT(*) INTO v_qtd FROM T_DF_DOCUMENTO WHERE ID_DOCUMENTO = p_id_documento AND ID_PESSOA = p_id_pessoa;
        IF v_qtd = 0 THEN
            RAISE_APPLICATION_ERROR(-20014, 'Documento nao encontrado para a pessoa');
        END IF;
    END IF;
    INSERT INTO T_DF_LAUDO (ID_PESSOA, ID_DOCUMENTO, COD_CID, DES_CONDICAO, TIPO_CONDICAO, DT_LAUDO)
    VALUES (p_id_pessoa, p_id_documento, UPPER(p_cod_cid), p_condicao, UPPER(p_tipo), p_dt_laudo);
    DBMS_OUTPUT.PUT_LINE('Laudo cadastrado');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_LAUDO', 1, 'Laudo ja cadastrado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Laudo ja cadastrado');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_LAUDO', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_LAUDO', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_DIREITO
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_DIREITO (
    p_cod_direito IN VARCHAR2,
    p_nome        IN VARCHAR2,
    p_orgao       IN VARCHAR2,
    p_base_legal  IN VARCHAR2,
    p_grupo       IN VARCHAR2,
    p_prazo_dias  IN NUMBER,
    p_verificacao IN VARCHAR2
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    INSERT INTO T_DF_DIREITO (COD_DIREITO, NOME_DIREITO, DES_ORGAO, DES_BASE_LEGAL, GRUPO_ALVO, NUM_PRAZO_DIAS, STA_VERIFICACAO)
    VALUES (UPPER(p_cod_direito), p_nome, p_orgao, p_base_legal, UPPER(p_grupo), p_prazo_dias, UPPER(p_verificacao));
    DBMS_OUTPUT.PUT_LINE('Direito cadastrado');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_DIREITO', 1, 'Direito ja cadastrado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Direito ja cadastrado');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_DIREITO', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_DIREITO', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_CRITERIO
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_CRITERIO (
    p_cod_direito   IN VARCHAR2,
    p_idade_min     IN NUMBER,
    p_idade_max     IN NUMBER,
    p_exige_pcd    IN VARCHAR2,
    p_exige_tea    IN VARCHAR2,
    p_tipo_exigido  IN VARCHAR2,
    p_frc_sm_max    IN NUMBER,
    p_pct_tolerancia IN NUMBER,
    p_renda_individual IN VARCHAR2 DEFAULT 'N'
)
IS
    v_qtd     NUMBER;
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_qtd FROM T_DF_DIREITO WHERE COD_DIREITO = UPPER(p_cod_direito);
    IF v_qtd = 0 THEN
        RAISE_APPLICATION_ERROR(-20012, 'Direito nao encontrado');
    END IF;
    INSERT INTO T_DF_CRITERIO_DIREITO (ID_DIREITO, NUM_IDADE_MIN, NUM_IDADE_MAX, STA_EXIGE_PCD, STA_EXIGE_TEA, COD_TIPO_EXIGIDO, STA_RENDA_INDIVIDUAL, FRC_SM_MAX, PCT_TOLERANCIA)
    SELECT ID_DIREITO, p_idade_min, p_idade_max, UPPER(p_exige_pcd), UPPER(p_exige_tea), UPPER(p_tipo_exigido), UPPER(p_renda_individual), p_frc_sm_max, p_pct_tolerancia
    FROM T_DF_DIREITO WHERE COD_DIREITO = UPPER(p_cod_direito);
    DBMS_OUTPUT.PUT_LINE('Criterio cadastrado');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_CRITERIO', 1, 'Criterio do direito ja cadastrado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Criterio do direito ja cadastrado');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_CRITERIO', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_CRITERIO', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/

------------------------------------------------------------
-- PROCEDURE: PRC_CARGA_MEDICAMENTO_FP
------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_CARGA_MEDICAMENTO_FP (
    p_indicacao     IN VARCHAR2,
    p_tipo          IN VARCHAR2,
    p_principio     IN VARCHAR2,
    p_concentracao  IN VARCHAR2,
    p_observacao    IN VARCHAR2,
    p_fonte         IN VARCHAR2,
    p_dt_elenco     IN DATE
)
IS
    v_erro    VARCHAR2(4000);
    v_codigo  NUMBER;
BEGIN
    INSERT INTO T_DF_MEDICAMENTO_FP (COD_INDICACAO, TIPO_ITEM, DES_PRINCIPIO_ATIVO, DES_CONCENTRACAO, DES_OBSERVACAO, DES_FONTE, DT_ELENCO)
    VALUES (UPPER(p_indicacao), UPPER(p_tipo), p_principio, p_concentracao, p_observacao, p_fonte, p_dt_elenco);
    DBMS_OUTPUT.PUT_LINE('Item do elenco cadastrado');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_MEDICAMENTO_FP', 1, 'Item do elenco ja cadastrado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro: Item do elenco ja cadastrado');
    WHEN VALUE_ERROR THEN
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_MEDICAMENTO_FP', 2, 'Erro de valor informado', SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro de valor');
    WHEN OTHERS THEN
        v_codigo := SQLCODE;
        v_erro   := SQLERRM;
        INSERT INTO T_DF_LOG_ERRO (NOME_PROCEDURE, CODIGO_ERRO, MENSAGEM_ERRO, DATA_ERRO, USUARIO_BANCO)
        VALUES ('PRC_CARGA_MEDICAMENTO_FP', v_codigo, v_erro, SYSDATE, USER);
        DBMS_OUTPUT.PUT_LINE('Erro [' || v_codigo || ']: ' || v_erro);
END;
/
