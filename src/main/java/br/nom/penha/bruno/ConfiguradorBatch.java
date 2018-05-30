package br.nom.penha.bruno;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.nom.penha.bruno.entidades.Cliente;
import br.nom.penha.bruno.mapeadores.MapeiaRegistroCliente;
import br.nom.penha.bruno.negocio.AjustaSenhaParameterSourceProvider;
import br.nom.penha.bruno.processadores.ProcessadorItemCliente;

@Configuration 
@EnableBatchProcessing
public class ConfiguradorBatch {

	@Autowired
	public JobBuilderFactory fabricaMontadorTarefas;
	
	@Autowired
	public StepBuilderFactory fabricaMontadorPassos;
	
	@Autowired
	public DataSource origemDados;
	
	@Bean
	public DataSource origemDadoUSUARIOps() {
		final DriverManagerDataSource banco = new DriverManagerDataSource();
		banco.setDriverClassName("com.mysql.jdbc.Driver");
//		banco.setUrl("jdbc:mysql://localhost:3306/<BANCO>");
//		banco.setUsername("<USUARIO>");
//		banco.setPassword("<SENHA>");
		
		return banco;
	}
	
	public JdbcCursorItemReader<Cliente> leitor(){
		JdbcCursorItemReader<Cliente> leitor = new JdbcCursorItemReader<Cliente>();
		
		leitor.setDataSource(origemDados);
		leitor.setSql("SELECT id, nome, email, senha FROM clientes");
		leitor.setRowMapper(new MapeiaRegistroCliente());
		
		return leitor;
	}
	
	public ProcessadorItemCliente processador() {
		return new ProcessadorItemCliente();
	}
	
	@Bean
	public FlatFileItemWriter<Cliente> escritorDeArquivo(){
		FlatFileItemWriter<Cliente> escritor = new FlatFileItemWriter<Cliente>();
		
		escritor.setResource(new ClassPathResource("cliente.csv"));
		escritor.setLineAggregator(new DelimitedLineAggregator<Cliente>() {
			{
				setDelimiter(",");
				setFieldExtractor(new BeanWrapperFieldExtractor<Cliente>() {
					{
						setNames(new String[] {"id", "nome", "email", "senha"});
					}
				});
			}
		});
		
		return escritor;
	}
	
	@Bean
	public JdbcBatchItemWriter<Cliente> escritorDeBanco(){
		JdbcBatchItemWriter<Cliente> escritor = new JdbcBatchItemWriter<Cliente>();
		escritor.setItemSqlParameterSourceProvider(new AjustaSenhaParameterSourceProvider());
		escritor.setSql("UPDATE clientes SET senha=:senha WHERE id=:id");
		escritor.setDataSource(origemDados);
		
		
		return escritor;
	}
	
	@Bean
	public Step passo1() {
		return fabricaMontadorPassos.get("passo1")
				.<Cliente,Cliente> chunk(10)
				.reader(leitor())
				.writer(escritorDeBanco())
				.build();
	}
		
	@Bean
	public Job tarefaAlteraDadosCliente() {
		
		return fabricaMontadorTarefas.get("tarefaAlteraDadosCliente")
				.incrementer(new RunIdIncrementer())
				.flow(passo1())
				.end()
				.build();
	}
	
	
}
