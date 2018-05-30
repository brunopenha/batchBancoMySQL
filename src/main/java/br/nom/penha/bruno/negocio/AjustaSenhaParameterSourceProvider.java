package br.nom.penha.bruno.negocio;

import java.util.HashMap;

import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import br.nom.penha.bruno.entidades.Cliente;

public class AjustaSenhaParameterSourceProvider implements ItemSqlParameterSourceProvider<Cliente> {

	@Override
	public SqlParameterSource createSqlParameterSource(Cliente clienteRecebido) {
		return new MapSqlParameterSource(new HashMap<String, Object>() {

            {
				put("id", 	clienteRecebido.getId());
				put("senha", ajustaSenha(clienteRecebido));
			}

			private String ajustaSenha(Cliente clienteRecebido) {
				if(clienteRecebido.getSenha().contains("0000")) {
					clienteRecebido.setSenha(clienteRecebido.getSenha().replace("0000", ""));
				}
				return clienteRecebido.getSenha();
			}
		});
	}

}
