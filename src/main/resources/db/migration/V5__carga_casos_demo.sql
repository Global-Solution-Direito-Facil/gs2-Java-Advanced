-- Flyway V5: abre os casos da demo (data de referencia 05/11/2026) e simula o andamento

------------------------------------------------------------
-- CARGA DOS CASOS: avalia as 15 regras para cada pessoa e abre um caso
-- para cada direito ELEGIVEL ou POSSIVEL (data de referencia fixa)
------------------------------------------------------------
BEGIN
    PKG_ELEGIBILIDADE.PRC_ABRIR_CASOS(1, DATE '2026-11-05');
    PKG_ELEGIBILIDADE.PRC_ABRIR_CASOS(2, DATE '2026-11-05');
    PKG_ELEGIBILIDADE.PRC_ABRIR_CASOS(3, DATE '2026-11-05');
    PKG_ELEGIBILIDADE.PRC_ABRIR_CASOS(4, DATE '2026-11-05');
    PKG_ELEGIBILIDADE.PRC_ABRIR_CASOS(5, DATE '2026-11-05');
END;
/

------------------------------------------------------------
-- ANDAMENTO DOS CASOS: atualiza status via procedure (dispara os gatilhos)
------------------------------------------------------------
DECLARE
    PROCEDURE atualiza (p_pessoa IN NUMBER, p_cod IN VARCHAR2, p_status IN VARCHAR2, p_data IN DATE) IS
        v_id NUMBER;
    BEGIN
        SELECT c.ID_CASO INTO v_id
        FROM T_DF_CASO c
        JOIN T_DF_DIREITO d ON d.ID_DIREITO = c.ID_DIREITO
        WHERE c.ID_PESSOA = p_pessoa AND d.COD_DIREITO = p_cod;
        PKG_CASOS.PRC_ATUALIZAR_STATUS_CASO(v_id, p_status, p_data);
    END;
BEGIN
    atualiza(1, 'BPC_PCD', 'EM_ANALISE', DATE '2026-11-06');
    atualiza(1, 'BPC_PCD', 'DOCUMENTOS_PENDENTES', DATE '2026-11-07');
    atualiza(1, 'CIPTEA', 'PROTOCOLADO', DATE '2026-11-06');
    atualiza(1, 'CIPTEA', 'CONCEDIDO', DATE '2026-11-08');
    atualiza(2, 'ISENCAO_IPI_VEICULO_PCD', 'EM_ANALISE', DATE '2026-11-06');
    atualiza(3, 'BPC_IDOSO', 'PROTOCOLADO', DATE '2026-11-06');
    atualiza(3, 'TARIFA_SOCIAL_ENERGIA', 'PROTOCOLADO', DATE '2026-11-06');
    atualiza(3, 'TARIFA_SOCIAL_ENERGIA', 'CONCEDIDO', DATE '2026-11-07');
    atualiza(5, 'BPC_IDOSO', 'DOCUMENTOS_PENDENTES', DATE '2026-11-06');
    atualiza(5, 'CADUNICO', 'PROTOCOLADO', DATE '2026-11-06');
    atualiza(5, 'CADUNICO', 'NEGADO', DATE '2026-11-08');
END;
/
