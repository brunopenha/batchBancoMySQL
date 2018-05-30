package br.nom.penha.bruno.mapeadores;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import br.nom.penha.bruno.entidades.Cliente;

public class MapeiaRegistroCliente implements RowMapper<Cliente> {

	@Override
	public Cliente mapRow(ResultSet retornoBanco, int posicaoRegistro) throws SQLException {

		Cliente cliente = new Cliente();
		cliente.setId(retornoBanco.getLong("id"));
		cliente.setNome(retornoBanco.getString("nome"));
		cliente.setEmail(retornoBanco.getString("email"));
		cliente.setSenha(retornoBanco.getString("senha"));

		return cliente;
	}

}
