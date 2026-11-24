# Guia detalhado do projeto JDBC Sample

## 1. Visão geral

O **JDBC Sample** é um laboratório Java para estudar acesso a banco de dados relacional sem usar um framework ORM. A aplicação se comunica diretamente com MySQL por meio da API JDBC, permitindo visualizar cada etapa da execução:

1. abrir uma conexão;
2. montar uma consulta ou comando;
3. enviar os parâmetros;
4. executar;
5. percorrer resultados;
6. fechar os recursos.

O projeto é organizado como um exemplo didático: no `Main`, vários trechos estão ativos ou comentados para representar cenários diferentes de CRUD, relacionamentos e auditoria.

## 2. Arquitetura

```text
Main
  |
  +--> Flyway
  |      |
  |      +--> Executa migrações SQL
  |
  +--> DAOs
         |
         +--> ConnectionUtil
                |
                +--> Driver JDBC
                       |
                       +--> MySQL
```

### Entidades

As entidades representam os dados usados pela aplicação:

- `EmployeeEntity`: funcionário com nome, salário, data de nascimento, contatos e módulos.
- `ContactEntity`: contato associado a um funcionário.
- `ModuleEntity`: módulo disponível no sistema.
- `EmployeeAuditEntity`: registro organizado de uma operação realizada sobre um funcionário.
- `OperationEnum`: transforma as letras `I`, `U` e `D` da base em `INSERT`, `UPDATE` e `DELETE`.

Lombok gera automaticamente métodos como getters, setters e `toString` com a anotação `@Data`.

### DAOs

DAO significa **Data Access Object**. Cada DAO concentra o código responsável por acessar uma parte do banco:

| Classe | Responsabilidade |
|---|---|
| `ConnectionUtil` | Abre conexões JDBC |
| `EmployeeDAO` | CRUD usando `Statement` e concatenação de SQL |
| `EmployeeParamDAO` | CRUD seguro com parâmetros, lote e procedure |
| `ContactDAO` | Insere e consulta contatos |
| `AccessDAO` | Cria vínculos entre funcionários e módulos |
| `ModuleDAO` | Estrutura disponível para consulta de módulos |
| `EmployeeAuditDAO` | Consulta a view de auditoria |

## 3. Conexão com o banco

`ConnectionUtil` utiliza `DriverManager.getConnection`:

```text
jdbc:mysql://localhost/jdbc-sample
```

A URL informa ao driver:

- `jdbc:mysql`: o tipo de conexão;
- `localhost`: o servidor;
- `jdbc-sample`: o banco utilizado.

O usuário e a senha usados são `jdbc-sample`.

O projeto usa as interfaces `try-with-resources` para fechar automaticamente a conexão e os objetos dependentes ao terminar o bloco.

## 4. Migrações Flyway

As migrações ficam em `src/main/resources/db/migration`. No início de `Main`, o seguinte código aplica todas as versões pendentes:

```java
Flyway.configure()
    .dataSource("jdbc:mysql://localhost/jdbc-sample", "root", "root")
    .load()
    .migrate();
```

Flyway cria uma tabela de controle no banco e registra quais arquivos SQL já foram aplicados. Isso evita executar duas vezes uma migração já instalada.

Os arquivos são executados em ordem pelo timestamp presente no nome:

```text
V202608222022__create_employee_table.sql
V202608230047__create_employees_audit_table.sql
...
```

O nome permite entender a finalidade de cada migração e manter um histórico versionado do esquema.

## 5. Tabelas e relacionamentos

### Funcionários

`employees` contém:

- `id`: chave primária gerada automaticamente;
- `name`: nome do funcionário;
- `salary`: salário com duas casas decimais;
- `birthday`: data e hora armazenadas como `TIMESTAMP`.

### Contatos

`contacts` usa `employee_id` como chave estrangeira que aponta para `employees.id`. Assim, vários contatos podem pertencer ao mesmo funcionário.

Representação:

```text
1 funcionário -> muitos contatos
```

### Módulos e acessos

`modules` representa os módulos disponíveis. `accesses` possui:

- `employee_id`;
- `module_id`;

As duas colunas formam a chave primária composta, impedindo o mesmo vínculo repetido.

Representação:

```text
muitos funcionários <-> muitos módulos
```

Na prática, a relação é criada por `AccessDAO.insert(employeeId, moduleId)`.

### Auditoria

`employees_audit` guarda um registro antes ou depois de uma alteração em `employees`.

- `I`: inserção;
- `U`: atualização;
- `D`: exclusão.

Os gatilhos inserem dados nessa tabela automaticamente:

- `tgr_employee_audit_insert`: executado após inserir;
- `tgr_employee_audit_update`: executado antes de atualizar;
- `tgr_employee_audit_delete`: executado antes de excluir.

Uma alteração ainda pode acontecer diretamente no banco: a auditoria não depende do Java.

## 6. JDBC com Statement

`EmployeeDAO` usa `Statement` e `createStatement()`. O SQL é montado por concatenação de texto.

Exemplo conceitual:

```sql
INSERT INTO employees (name, salary, birthday)
VALUES ('Nome', 50000.00, '2026-01-01 00:00:00');
```

Esse formato ajuda a entender JDBC de maneira simples, mas apresenta o risco de:

- SQL malformado;
- valores especiais dentro dos campos;
- injeção de SQL quando os dados vierem de um usuário.

Por isso, o projeto também demonstra a alternativa segura.

## 7. JDBC com PreparedStatement

`EmployeeParamDAO` usa placeholders `?` com `PreparedStatement`. Os valores são enviados pelos métodos `setString`, `setBigDecimal`, `setLong` e `setTimestamp`.

Exemplo conceitual:

```sql
UPDATE employees
SET name = ?, salary = ?, birthday = ?
WHERE id = ?
```

Isso separa a estrutura do SQL dos valores fornecidos pela aplicação. Além de facilitar os tipos Java, evita problemas comuns com aspas e reduz o risco de SQL injection.

## 8. Operação em lote

No método `insert(List<EmployeeEntity>)`, o projeto demonstra batch insert:

1. desativa o commit automático;
2. prepara um `PreparedStatement`;
3. adiciona cada funcionário ao lote;
4. executa o lote;
5. confirma a transação com `commit`;
6. desfaz com `rollback` em caso de erro.

O batch é útil quando várias linhas precisam ser inseridas. As operações ficam relacionadas a uma transação: ou todas são confirmadas, ou todas são revertidas.

## 9. Procedure

`prc_insert_employee` recebe nome, salário e nascimento como parâmetros de entrada e devolve o ID gerado como parâmetro de saída.

No Java, `CallableStatement` é criado por `prepareCall`:

```java
statement = connection.prepareCall("call prc_insert_employee(?, ?, ?, ?)")
```

O método `insertWithProcedure` registra o primeiro parâmetro como saída, envia os demais valores e obtém o ID com `getLong(1)`.

Stored procedures centralizam uma operação no banco, mas deixam o SQL mais acoplado ao MySQL e ao esquema específico da aplicação.

## 10. Leitura de resultados

DAOs como `EmployeeDAO` e `EmployeeParamDAO` usam `ResultSet`.

O padrão geral é:

```java
while (resultSet.next()) {
    long id = resultSet.getLong("id");
    String name = resultSet.getString("name");
    // criar e preencher a entidade
}
```

`next()` move o cursor para a próxima linha. Quando não existem mais linhas, o método retorna `false`.

Datas são convertidas de `Timestamp` para `OffsetDateTime` usando o fuso UTC. Isso padroniza a leitura das datas que chegam do banco.

## 11. Fluxo atual de Main

A execução ativa em `Main` realiza este fluxo:

1. executa as migrações Flyway;
2. cria um objeto `Faker` configurado para português do Brasil;
3. gera três funcionários fictícios;
4. atribui de um a três IDs de módulo a cada funcionário;
5. insere os funcionários por `EmployeeParamDAO.insert`;
6. para cada funcionário inserido, `AccessDAO` grava seus vínculos.

O trecho ativo demonstra a combinação de geração de dados, insert parametrizado e relacionamento muitos-para-muitos.

Vários outros exemplos estão comentados no arquivo:

- insert simples;
- consulta por ID;
- consulta de todos;
- atualização;
- exclusão;
- insert por lote;
- relacionamento um-para-um;
- relacionamento um-para-muitos;
- consulta de auditoria;
- teste básico da conexão.

Os comentários permitem alternar os cenários sem apagar o código dos exemplos anteriores.

## 12. Como executar

### Pré-requisitos

- Java compatível com Gradle;
- Docker;
- Docker Compose;
- porta `3306` disponível.

### Banco

```bash
docker compose -f docker-compose-mysql.yml up -d
```

Conexão principal da aplicação:

- URL: `jdbc:mysql://localhost/jdbc-sample`
- usuário: `jdbc-sample`
- senha: `jdbc-sample`

Conexão usada pelo Flyway:

- usuário: `root`
- senha: `root`

### Aplicação

```bash
./gradlew run
```

Como `Main` não é conectado a uma interface gráfica ou API, os resultados são exibidos no terminal por objetos Java e por `println` ou são persistidos no banco.

## 13. Dados iniciais necessários

As migrações criam `modules`, mas não inserem registros iniciais nela. O trecho ativo de `Main` atribui módulos com IDs entre `1` e `4`, portanto o banco precisa conter módulos correspondentes antes dessa execução.

Exemplo no MySQL:

```sql
INSERT INTO modules (name)
VALUES ('Financeiro'), ('RH'), ('Estoque'), ('Vendas');
```

Esse SQL não faz parte das migrações atuais e pode ser executado manualmente após o banco iniciar.

## 14. Conceitos demonstrados

- API JDBC;
- `DriverManager`;
- `Connection`;
- `Statement`;
- `PreparedStatement`;
- `CallableStatement`;
- `ResultSet`;
- transações;
- batch insert;
- chave primária;
- chave estrangeira;
- relacionamentos um-para-muitos;
- relacionamentos muitos-para-muitos;
- migrações de banco;
- gatilhos;
- views;
- stored procedures;
- auditoria;
- separação entre entidades e DAOs.

## 15. Pontos de atenção

O projeto prioriza aprendizado, portanto existem algumas limitações intencionais:

- não possui interface gráfica ou API REST;
- parte da `Main` está comentada;
- `ModuleDAO` ainda não implementa uma consulta própria para módulos;
- `EmployeeParamDAO.update` contém uma vírgula extra antes de `WHERE`; corrigir esse SQL é parte natural de um exercício;
- as exceções SQL são exibidas no terminal, sem uma estratégia de logging mais completa;
- o projeto não valida todos os dados enviados ao banco;
- o banco criado pelo Docker não possui volume configurado no Compose atual;
- o trecho ativo pressupõe que já existam registros em `modules`.

## 16. Próximos exercícios sugeridos

Uma boa sequência de evolução é:

1. inserir dados iniciais em `modules` por Flyway;
2. corrigir o SQL de atualização;
3. completar `ModuleDAO`;
4. adicionar métodos de consulta e exclusão para contatos;
5. listar acessos de cada funcionário;
6. criar testes unitários e de integração;
7. adicionar uma camada de serviço;
8. substituir os prints por logging;
9. mover credenciais para variáveis de ambiente;
10. adicionar uma interface CLI ou API REST.
