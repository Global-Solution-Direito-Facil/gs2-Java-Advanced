------------------------------------------------------------
-- PROJETO: DIREITO FACIL - CITIZENTECH (GLOBAL SOLUTION 2026/2)
-- DISCIPLINA: MASTERING RELATIONAL AND NON-RELATIONAL DATABASE
-- ARQUIVO: 03_CARGA.sql
-- DESCRICAO: BLOCOS ANONIMOS DE INSERCAO DE DADOS VIA PROCEDURES
-- EXECUCAO: 3 - executar apos 02_PROCEDURES.sql
------------------------------------------------------------


------------------------------------------------------------
-- USUARIOS (5 cidadaos + 1 assistente social/gestor)
------------------------------------------------------------
BEGIN
    PRC_CARGA_USUARIO('Ana Almeida', '111.111.111-11', 'ana.almeida@email.com', '$2a$10$7xHgKIZBIFcQEpbo8Z7Zf.upDejCIxcdle2XBO.E5O7k1n95D0Xhu', 'CIDADAO');
    PRC_CARGA_USUARIO('Carlos Menezes', '222.222.222-22', 'carlos.menezes@email.com', '$2a$10$7xHgKIZBIFcQEpbo8Z7Zf.upDejCIxcdle2XBO.E5O7k1n95D0Xhu', 'CIDADAO');
    PRC_CARGA_USUARIO('Paula Souza', '333.333.333-33', 'paula.souza@email.com', '$2a$10$7xHgKIZBIFcQEpbo8Z7Zf.upDejCIxcdle2XBO.E5O7k1n95D0Xhu', 'CIDADAO');
    PRC_CARGA_USUARIO('Marcos Teixeira', '444.444.444-44', 'marcos.teixeira@email.com', '$2a$10$7xHgKIZBIFcQEpbo8Z7Zf.upDejCIxcdle2XBO.E5O7k1n95D0Xhu', 'CIDADAO');
    PRC_CARGA_USUARIO('Rita Barbosa', '555.555.555-55', 'rita.barbosa@email.com', '$2a$10$7xHgKIZBIFcQEpbo8Z7Zf.upDejCIxcdle2XBO.E5O7k1n95D0Xhu', 'CIDADAO');
    PRC_CARGA_USUARIO('Rafaela Gomes', '666.666.666-66', 'rafaela.gomes@cras.gov.br', '$2a$10$7xHgKIZBIFcQEpbo8Z7Zf.upDejCIxcdle2XBO.E5O7k1n95D0Xhu', 'GESTOR');
END;
/

------------------------------------------------------------
-- SALARIO MINIMO (confirmar valores no site oficial antes da entrega)
------------------------------------------------------------
BEGIN
    PRC_CARGA_SALARIO_MINIMO(2025, 1518.00, 'Decreto anual do salario minimo - confirmar');
    PRC_CARGA_SALARIO_MINIMO(2026, 1621.00, 'Decreto anual do salario minimo - confirmar');
END;
/

------------------------------------------------------------
-- PESSOAS (as 3 personas da demo + 2 casos de teste negativo/limite)
------------------------------------------------------------
BEGIN
    PRC_CARGA_PESSOA(1, 'Lucas Almeida', DATE '2019-03-14', 1200.00, 4);
    PRC_CARGA_PESSOA(2, 'Carlos Menezes', DATE '1985-07-22', 6500.00, 2);
    PRC_CARGA_PESSOA(3, 'Helena Souza', DATE '1958-01-30', 750.00, 2);
    PRC_CARGA_PESSOA(4, 'Marcos Teixeira', DATE '1991-05-10', 1500.00, 1);
    PRC_CARGA_PESSOA(5, 'Rita Barbosa', DATE '1960-03-02', 906.00, 2);
END;
/

------------------------------------------------------------
-- DOCUMENTOS (registro dos laudos anunciados pelo evento DocumentoRecebido; id externo = id na API .NET)
------------------------------------------------------------
BEGIN
    PRC_CARGA_DOCUMENTO(1, 'dotnet-doc-0001', 'laudo_lucas_almeida.jpg', 'image/jpeg', 482113, 'demo-idem-0001', 'PROCESSADO', DATE '2026-09-10', DATE '2026-09-10');
    PRC_CARGA_DOCUMENTO(2, 'dotnet-doc-0002', 'laudo_carlos_menezes.jpg', 'image/jpeg', 391204, 'demo-idem-0002', 'PROCESSADO', DATE '2025-06-18', DATE '2025-06-18');
    PRC_CARGA_DOCUMENTO(3, 'dotnet-doc-0003', 'laudo_helena_souza.jpg', 'image/jpeg', 455870, 'demo-idem-0003', 'PROCESSADO', DATE '2026-08-03', DATE '2026-08-03');
    PRC_CARGA_DOCUMENTO(4, 'dotnet-doc-0004', 'laudo_marcos_teixeira.pdf', 'application/pdf', 210544, 'demo-idem-0004', 'PROCESSADO', DATE '2026-10-01', DATE '2026-10-01');
    PRC_CARGA_DOCUMENTO(5, 'dotnet-doc-0005', 'laudo_rita_barbosa.jpg', 'image/jpeg', 503311, 'demo-idem-0005', 'PROCESSADO', DATE '2026-07-21', DATE '2026-07-21');
END;
/

------------------------------------------------------------
-- LAUDOS (ficticios, cada um ligado ao seu documento)
------------------------------------------------------------
BEGIN
    PRC_CARGA_LAUDO(1, 1, 'F84.0', 'Transtorno do espectro autista (autismo infantil)', 'TEA', DATE '2026-09-10');
    PRC_CARGA_LAUDO(2, 2, 'G82.2', 'Paraplegia - usuario de cadeira de rodas', 'DEF_FISICA', DATE '2025-06-18');
    PRC_CARGA_LAUDO(3, 3, 'I10', 'Hipertensao arterial essencial', 'OUTRA', DATE '2026-08-03');
    PRC_CARGA_LAUDO(4, 4, 'F41.1', 'Transtorno de ansiedade generalizada', 'OUTRA', DATE '2026-10-01');
    PRC_CARGA_LAUDO(5, 5, 'M17.0', 'Gonartrose primaria bilateral', 'OUTRA', DATE '2026-07-21');
END;
/

------------------------------------------------------------
-- CATALOGO DE DIREITOS (15) - STA_VERIFICACAO = CONFIRMAR ate o Compliance revisar
------------------------------------------------------------
BEGIN
    PRC_CARGA_DIREITO('BPC_IDOSO', 'BPC/LOAS - Idoso 65+', 'INSS', 'Lei 8.742/1993 art. 20', 'IDOSO', 45, 'CONFIRMAR');
    PRC_CARGA_DIREITO('BPC_PCD', 'BPC/LOAS - Pessoa com Deficiencia', 'INSS', 'Lei 8.742/1993 art. 20', 'PCD', 45, 'CONFIRMAR');
    PRC_CARGA_DIREITO('PASSE_LIVRE_PCD', 'Passe Livre Interestadual - PcD', 'Ministerio dos Transportes', 'Lei 8.899/1994', 'PCD', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('PASSE_IDOSO_INTERESTADUAL', 'Vagas gratuitas interestaduais - Idoso', 'ANTT / transportadora', 'Lei 10.741/2003 art. 40', 'IDOSO', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('ISENCAO_IPI_VEICULO_PCD', 'Isencao de IPI na compra de veiculo - PcD', 'Receita Federal', 'Lei 8.989/1995', 'PCD', 60, 'CONFIRMAR');
    PRC_CARGA_DIREITO('ISENCAO_IOF_VEICULO_PCD', 'Isencao de IOF em financiamento de veiculo - PcD', 'Receita Federal', 'Lei 8.383/1991 art. 72', 'PCD', 60, 'CONFIRMAR');
    PRC_CARGA_DIREITO('CIPTEA', 'Carteira de Identificacao da Pessoa com TEA', 'Governo estadual ou municipal', 'Lei 13.977/2020', 'AUTISMO', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('TARIFA_SOCIAL_ENERGIA', 'Tarifa Social de Energia Eletrica', 'ANEEL / distribuidora', 'Lei 12.212/2010', 'BAIXA_RENDA', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('BOLSA_FAMILIA', 'Bolsa Familia', 'MDS / Caixa', 'Lei 14.601/2023', 'BAIXA_RENDA', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('CADUNICO', 'Cadastro Unico (CadUnico)', 'CRAS municipal', 'Decreto 11.016/2022', 'BAIXA_RENDA', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('COTA_PCD_ENSINO_SUPERIOR', 'Reserva de vagas PcD - ensino superior federal', 'MEC / instituicao', 'Lei 13.409/2016', 'PCD', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('COTA_PCD_CONCURSO', 'Reserva de vagas PcD - concurso publico federal', 'Orgao / banca', 'Decreto 9.508/2018', 'PCD', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('COTA_PCD_EMPREGO', 'Reserva de vagas PcD em empresas', 'Ministerio do Trabalho', 'Lei 8.213/1991 art. 93', 'PCD', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('PRIORIDADE_IDOSO', 'Atendimento prioritario - Idoso 60+', 'Qualquer orgao publico', 'Lei 10.048/2000', 'IDOSO', 30, 'CONFIRMAR');
    PRC_CARGA_DIREITO('ISENCAO_IR_65MAIS', 'Parcela isenta adicional de IR - 65+', 'Receita Federal', 'Lei 7.713/1988 art. 6 XV', 'IDOSO', 30, 'CONFIRMAR');
END;
/

------------------------------------------------------------
-- CRITERIOS (frc_sm_max = fracao do salario minimo per capita)
------------------------------------------------------------
BEGIN
    PRC_CARGA_CRITERIO('BPC_IDOSO', 65, NULL, 'N', 'N', 0.25, 20);
    PRC_CARGA_CRITERIO('BPC_PCD', 0, NULL, 'S', 'N', 0.25, 20);
    PRC_CARGA_CRITERIO('PASSE_LIVRE_PCD', 0, NULL, 'S', 'N', 1.0, 20);
    PRC_CARGA_CRITERIO('PASSE_IDOSO_INTERESTADUAL', 60, NULL, 'N', 'N', 2.0, 0);
    PRC_CARGA_CRITERIO('ISENCAO_IPI_VEICULO_PCD', 0, NULL, 'S', 'N', NULL, 0);
    PRC_CARGA_CRITERIO('ISENCAO_IOF_VEICULO_PCD', 0, NULL, 'S', 'N', NULL, 0);
    PRC_CARGA_CRITERIO('CIPTEA', 0, NULL, 'N', 'S', NULL, 0);
    PRC_CARGA_CRITERIO('TARIFA_SOCIAL_ENERGIA', 0, NULL, 'N', 'N', 0.5, 20);
    PRC_CARGA_CRITERIO('BOLSA_FAMILIA', 0, NULL, 'N', 'N', 0.135, 20);
    PRC_CARGA_CRITERIO('CADUNICO', 0, NULL, 'N', 'N', 0.5, 20);
    PRC_CARGA_CRITERIO('COTA_PCD_ENSINO_SUPERIOR', 0, NULL, 'S', 'N', NULL, 0);
    PRC_CARGA_CRITERIO('COTA_PCD_CONCURSO', 0, NULL, 'S', 'N', NULL, 0);
    PRC_CARGA_CRITERIO('COTA_PCD_EMPREGO', 0, NULL, 'S', 'N', NULL, 0);
    PRC_CARGA_CRITERIO('PRIORIDADE_IDOSO', 60, NULL, 'N', 'N', NULL, 0);
    PRC_CARGA_CRITERIO('ISENCAO_IR_65MAIS', 65, NULL, 'N', 'N', NULL, 0);
END;
/
