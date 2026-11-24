# JDBC Sample

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

Projeto didático em Java para aprender a usar JDBC com MySQL na prática. Ele demonstra conexões, consultas com `Statement` e `PreparedStatement`, procedimentos armazenados, transações em lote, relacionamentos entre tabelas e auditoria automática por gatilhos.

## O que o projeto faz

- Cria e atualiza o banco automaticamente com migrações Flyway.
- Executa operações CRUD com JDBC sobre funcionários.
- Demonstra SQL comum e SQL com parâmetros.
- Usa uma `stored procedure` para inserir funcionários.
- Gera dados fictícios em português com Datafaker.
- Associa funcionários a módulos por meio de um relacionamento muitos-para-muitos.
- Registra alterações de funcionários em uma tabela de auditoria usando gatilhos.
- Consulta a auditoria por uma view do MySQL.
- Demonstra relacionamento um-para-muitos entre funcionários e contatos.

## Recursos do banco

| Recurso | Descrição |
|---|---|
| `employees` | Armazena funcionários, salário e data de nascimento |
| `contacts` | Contatos associados a um funcionário |
| `modules` | Módulos aos quais um funcionário pode ter acesso |
| `accesses` | Relaciona funcionários e módulos |
| `employees_audit` | Histórico de inserções, atualizações e exclusões |
| `tgr_employee_audit_insert` | Auditador automático de inserções |
| `tgr_employee_audit_update` | Auditador automático de atualizações |
| `tgr_employee_audit_delete` | Auditador automático de exclusões |
| `view_employee_audit` | Facilita a consulta das operações de auditoria |
| `prc_insert_employee` | Insere um funcionário e devolve o ID gerado |

## Tecnologias

- Java
- JDBC
- MySQL 8.3
- Gradle 9.6
- Flyway
- Docker Compose
- Lombok
- Datafaker

## Como executar

### 1. Inicie o MySQL

Na raiz do projeto:

```bash
docker compose -f docker-compose-mysql.yml up -d
```

O Compose publica o banco em `localhost:3306`, usando:

- Banco: `jdbc-sample`
- Usuário: `jdbc-sample`
- Senha: `jdbc-sample`
- Senha de root: `root`

### 2. Execute o projeto

```bash
./gradlew run
```

Ao iniciar, o `Main` configura o Flyway e cria as migrações pendentes no banco. Em seguida, executa o trecho ativo de geração de funcionários.

**Observação:** a execução atual gera três funcionários e associa IDs de módulos aleatórios. Como o projeto não cria registros iniciais em `modules`, cadastre os módulos necessários antes de executar essa parte ou ajuste o trecho ativo em `Main.java`.

### 3. Pare o banco

```bash
docker compose -f docker-compose-mysql.yml down
```

Para também apagar os dados persistidos pelo container:

```bash
docker compose -f docker-compose-mysql.yml down -v
```

## Estrutura principal

```text
src/main/java/br/com/dio/
├── Main.java                         # Execução e exemplos
└── persistence/
    ├── ConnectionUtil.java           # Criação das conexões JDBC
    ├── EmployeeDAO.java              # CRUD com Statement
    ├── EmployeeParamDAO.java         # CRUD, lote e procedure
    ├── ContactDAO.java               # Contatos por funcionário
    ├── AccessDAO.java                # Acessos funcionário-módulo
    ├── EmployeeAuditDAO.java         # Consulta da auditoria
    └── entity/                       # Entidades do domínio

src/main/resources/db/migration/
└── V*.sql                            # Migrações Flyway

docs/
└── guia-do-projeto.md                # Documentação detalhada
```

## Documentação

Consulte o [guia detalhado do projeto](docs/guia-do-projeto.md) para entender a arquitetura, os conceitos JDBC utilizados, o fluxo de execução, os relacionamentos do banco e as principais classes.

## Licença

Este projeto está sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE).
 
