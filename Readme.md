Passos para criar um projeto Spring Batch

1 - Crie um novo projeto Spring Batch utilizando o plugin STS da Spring (no Eclipse foi instalado pelo MarketPlace)

1.1 - File -> New -> Project -> Others -> Spring Boot -> Spring Starter Project

1.2 - Dê um nome ao projeto e na tela seguinte, selecione apenas em I/O 
o Batch

2 - Abra o pom.xml e inclua a dependencia:

```xml
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
</dependency>
```
3 - Crie uma entidade que ira receber os dados do banco, sendo que aqui foi criada a entidade, sendo que aqui foi criada a classe `Cliente`

3.1 Inclua os atributos principais, como `nome` e `email`, criando os seus _getters_ e _setters_

<a name="4">4</a> - Crie uma classe que ira mapear os registros dessa entidade `Cliente` e que implemente a _interface_ `RowMapper<Cliente>`, sendo que aqui foi chamado de `MapeiaRegistroCliente`

4.1 - Inclua o método `mapRow(ResultSet retornoBanco, int posicaoRegistro)` que retorne a Entidade `Cliente`

4.2 - Nesse método, crie uma nova instância da entidade `Cliente` e utilize o parâmetro `retornoBanco` para obter os dados do banco, conforme abaixo:

```java
@Override
public Cliente mapRow(ResultSet retornoBanco, int posicaoRegistro) throws SQLException {

	Cliente cliente = new Cliente();
	cliente.setId(retornoBanco.getLong("id"));
	cliente.setNome(retornoBanco.getString("nome"));
	cliente.setEmail(retornoBanco.getString("email"));
	cliente.setSenha(retornoBanco.getString("senha"));

	return cliente;
}
```

<a name="5">5</a> - Crie uma classe que fará a configuração do Batch, aqui dei o nome de `ConfiguradorBatch` e implemente o seguinte:

5.1 Inclua na declaração da classe as anotações `@Configuration` e `@EnableBatchProcessing`

<a name="5.2">5.2</a> Inclua um atributo do tipo `JobBuilderFactory` e coloque a anotação `@Autowired`

<a name="5.3">5.3</a> Inclua um atributo do tipo `StepBuilderFactory` e coloque a anotação `@Autowired`

<a name="5.4">5.4</a> Inclua um atributo do tipo `javax.sql.DataSource` e coloque a anotação `@Autowired`

5.5 Crie um método que retorne `javax.sql.DataSource`, coloque a anotação `@Bean` e inclua as configurações de acesso ao banco, podendo ficar como abaixo

```java
@Bean
public DataSource origemDados() {
	final DriverManagerDataSource banco = new DriverManagerDataSource();
	banco.setDriverClassName("com.mysql.jdbc.Driver");
	banco.setUrl("jdbc:mysql://localhost:3306/<NOME_BANCO>");
	banco.setUsername("<USUARIO_BANCO>");
	banco.setPassword("<SENHA_BANCO>");
	
	return banco;
}
```
<a name="5.6">5.6</a> Crie um método que retorne um `JdbcCursorItemReader<Cliente>`, coloque a anotação `@Bean` e inclua o seguinte:

5.6.1 - Crie um objeto `JdbcCursorItemReader<Cliente>` e atribua a ele uma nova instância desse mesmo tipo

5.6.2 - Nesse objeto, atribua o _dataSource_ o objeto criado em [5.4](#5.4)

5.6.3 - Ainda nesse objeto, atribua o _sql_ com essa consulta simples `SELECT nome, email, senha FROM clientes`

5.6.4 - Também nesse objeto, atribua o _rowMapper_ com um nova instância, conforme criado no item [4](#4), podendo ficar assim no final:

```java
public JdbcCursorItemReader<Cliente> leitor(){
	JdbcCursorItemReader<Cliente> leitor = new JdbcCursorItemReader<Cliente>();
	
	leitor.setDataSource(origemDados);
	leitor.setSql("SELECT nome, email, senha FROM clientes");
	leitor.setRowMapper(new MapeiaRegistroCliente());
	
	return leitor;
}
```
<a name="6">6</a> Crie uma classe que implemente a _interface_  `org.springframework.batch.item.ItemProcessor<Cliente,Cliente>`

6.1 Inclua o método `process(Cliente clienteParam)` que retorne a entidade `Cliente`

6.2 Por enquanto, faça com que esse método retorne o objeto passado por parâmentro, nesse caso, o `clienteParam`


.<a name="7">7</a> - De volta para a classe criada no item [5](#5), inclua o método `processador()` que retorne o tipo criado no item [6](#6), ficando:

```java
public ProcessadorItemCliente processador() {
	return new ProcessadorItemCliente();
}
```

.<a name="8">8</a> - Aqui deve ser implementado o tratamento para escrever no banco...

.<a name="9>9</a> - Na classe criado no item [4](#4), inclua o método `passo1()` que retorno o tipo `Step`, de forma que ele utilize o atributo criado em [5.3](#5.3), fazendo com que ele tenha um _chunk_ de 10, use como _reader_ o metodo criado em [5.6](#5.6), como _processor_ o método criado [7](#7), como _writer_ o metodo criado em [8](#8) e ao final, chamando o método _build_. Esse método tem que ser anotado com `@Bean`, ficando assim:

```java
@Bean
public Step passo1() {
	return fabricaMontadorPassos.get("passo1")
			.<Cliente,Cliente> chunk(10)
			.reader(leitor())
			.writer(escritor())
			.build();
}
```

10 - Ainda nessa classe, crie um método `tarefaAlteraDadosCliente` que retorne o tipo `Job` que utilize como fluxo o método criado no item [9](#9), que chame o método `end()` e ao final o `build()`, ficando assim:

```java
@Bean
public Job tarefaAlteraDadosCliente() {
	
	return fabricaMontadorTarefas.get("tarefaAlteraDadosCliente")
			.incrementer(new RunIdIncrementer())
			.flow(passo1())
			.end()
			.build();
}
```

11 - Agora é necessário apenas selecionar a classe `BatchBancoMySqlApplication` e mandar executar como `Java Application`

---
#Soluções conhecidas

---

Se acontecer o erro:

```
Caused by: org.springframework.jdbc.BadSqlGrammarException: PreparedStatementCallback; bad SQL grammar [SELECT JOB_INSTANCE_ID, JOB_NAME from BATCH_JOB_INSTANCE where JOB_NAME = ? order by JOB_INSTANCE_ID desc]; nested exception is com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Table 'treinamento_java.BATCH_JOB_INSTANCE' doesn't exist
```

Abra a biblioteca `spring-batch-core-XX.jar` e dentro do pacote `org.springframework.batch.core` existem os scripts para a criação dessas tabelas no banco utilizado

---
Abra a classe 