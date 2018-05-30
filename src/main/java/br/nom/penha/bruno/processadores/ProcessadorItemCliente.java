package br.nom.penha.bruno.processadores;

import org.springframework.batch.item.ItemProcessor;

import br.nom.penha.bruno.entidades.Cliente;

public class ProcessadorItemCliente implements ItemProcessor<Cliente, Cliente> {

	@Override
	public Cliente process(Cliente clienteParam) throws Exception {
		return clienteParam;
	}

}
