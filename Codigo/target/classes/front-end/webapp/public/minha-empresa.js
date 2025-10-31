
$(document).ready(function() {
    console.log("Carregando dados da empresa...");
    
    let empresaAtual = null; // Armazenar dados da empresa atual

    // Função para preencher o formulário
    function preencherFormulario(empresa) {
        console.log("Dados recebidos:", empresa);
        
        empresaAtual = empresa; // Salvar referência da empresa
        
        // Dados básicos
        $("#razaoSocial").text(empresa.razaoSocial || "Não informado");
        $("#nomeFantasia").text(empresa.nomeFantasia || "Não informado");
        $("#cnpj").text(empresa.cnpj || "Não informado");
        $("#segmento").text(empresa.segmento || "Não informado");

        // Endereço (verifica se está no formato com objeto endereço ou direto)
        const endereco = empresa.endereco || empresa;
        $("#estado").text(endereco.estado || endereco.uf || "Não informado");
        $("#cidade").text(endereco.cidade || "Não informado");
        $("#rua").text(endereco.rua || endereco.logradouro || "Não informado");
        $("#cep").text(endereco.cep || "Não informado");

        // Responsável
        $("#responsavelNome").text(empresa.responsavelNome || empresa.nomeResponsavel || "Não informado");
        $("#responsavelCargo").text(empresa.responsavelCargo || empresa.cargoResponsavel || "Não informado");
        $("#responsavelEmail").text(empresa.responsavelEmail || empresa.emailResponsavel || "Não informado");
        $("#responsavelTelefone").text(empresa.responsavelTelefone || empresa.telefoneResponsavel || "Não informado");

        // Serviços de interesse
        if (empresa.servicosInteresse) {
            $('input[name="servicosInteresse"]').prop('checked', false);
            empresa.servicosInteresse.forEach(servico => {
                $(`input[name="servicosInteresse"][value="${servico}"]`).prop("checked", true);
            });
        }

        // Observações
        $("#observacoes").val(empresa.observacoes || "");
    }

    // Função para preencher modal de edição
    function preencherModalEdicao(empresa) {
        const endereco = empresa.endereco || empresa;
        
        $("#edit-razaoSocial").val(empresa.razaoSocial || "");
        $("#edit-nomeFantasia").val(empresa.nomeFantasia || "");
        $("#edit-cnpj").val(empresa.cnpj || "");
        $("#edit-segmento").val(empresa.segmento || "");
        $("#edit-estado").val(endereco.estado || endereco.uf || "");
        $("#edit-cidade").val(endereco.cidade || "");
        $("#edit-rua").val(endereco.rua || endereco.logradouro || "");
        $("#edit-cep").val(endereco.cep || "");
        $("#edit-responsavelNome").val(empresa.responsavelNome || empresa.nomeResponsavel || "");
        $("#edit-responsavelCargo").val(empresa.responsavelCargo || empresa.cargoResponsavel || "");
        $("#edit-responsavelEmail").val(empresa.responsavelEmail || empresa.emailResponsavel || "");
        $("#edit-responsavelTelefone").val(empresa.responsavelTelefone || empresa.telefoneResponsavel || "");
        $("#edit-observacoes").val(empresa.observacoes || "");
        
        // Serviços de interesse
        $('input[name="servicosInteresse"]').prop('checked', false);
        if (empresa.servicosInteresse) {
            empresa.servicosInteresse.forEach(servico => {
                $(`#edit-servico${servico.charAt(0).toUpperCase() + servico.slice(1)}`).prop("checked", true);
            });
        }
    }

    // Carrega os dados da empresa do usuário logado
    function carregarDadosEmpresa() {
        // Verificar se há usuário logado
        const userInfo = localStorage.getItem('userInfo');
        if (!userInfo) {
            alert("Você precisa estar logado para ver seus dados.");
            window.location.href = '/login.html';
            return;
        }

        const user = JSON.parse(userInfo);
        const userEmail = user.email;

        $.ajax({
            url: `/api/empresas/usuario/${userEmail}`,
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                console.log("Dados recebidos com sucesso:", data);
                if (data.length > 0) {
                    // Pegar a primeira empresa (ou a mais recente)
                    const empresa = data.sort((a, b) => new Date(b.dataCadastro) - new Date(a.dataCadastro))[0];
                    preencherFormulario(empresa);
                } else {
                    alert("Nenhuma empresa encontrada para este usuário.");
                }
            },
            error: function(xhr, status, error) {
                console.error("Erro ao carregar dados:", status, error);
                if (xhr.status === 404) {
                    alert("Nenhuma empresa cadastrada encontrada.");
                } else {
                    alert("Erro ao carregar dados da empresa.");
                }
            }
        });
    }

    // Função para excluir empresa
    function excluirEmpresa() {
        if (!empresaAtual) {
            alert("Nenhuma empresa para excluir.");
            return;
        }

        $.ajax({
            url: `/api/empresas/${empresaAtual.id}`,
            method: 'DELETE',
            success: function(response) {
                alert("Empresa excluída com sucesso!");
                window.location.href = 'index.html';
            },
            error: function(xhr, status, error) {
                console.error("Erro ao excluir empresa:", status, error);
                alert("Erro ao excluir empresa. Tente novamente.");
            }
        });
    }

    // Função para salvar edições
    function salvarEdicao() {
        if (!empresaAtual) {
            alert("Nenhuma empresa para editar.");
            return;
        }

        // Coletar dados do formulário
        const dadosAtualizados = {
            razaoSocial: $("#edit-razaoSocial").val(),
            nomeFantasia: $("#edit-nomeFantasia").val(),
            cnpj: $("#edit-cnpj").val(),
            segmento: $("#edit-segmento").val(),
            responsavelNome: $("#edit-responsavelNome").val(),
            responsavelCargo: $("#edit-responsavelCargo").val(),
            responsavelEmail: $("#edit-responsavelEmail").val(),
            responsavelTelefone: $("#edit-responsavelTelefone").val(),
            observacoes: $("#edit-observacoes").val(),
            endereco: {
                estado: $("#edit-estado").val(),
                cidade: $("#edit-cidade").val(),
                rua: $("#edit-rua").val(),
                cep: $("#edit-cep").val()
            },
            servicosInteresse: [],
            status: 'pendente' // Status volta para pendente após edição
        };

        // Coletar serviços selecionados
        $('input[name="servicosInteresse"]:checked').each(function() {
            dadosAtualizados.servicosInteresse.push($(this).val());
        });

        $.ajax({
            url: `/api/empresas/${empresaAtual.id}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(dadosAtualizados),
            success: function(response) {
                alert("Empresa atualizada com sucesso! O status foi alterado para 'pendente' e aguarda nova aprovação.");
                $('#modalEdicao').modal('hide');
                carregarDadosEmpresa(); // Recarregar dados
            },
            error: function(xhr, status, error) {
                console.error("Erro ao atualizar empresa:", status, error);
                alert("Erro ao atualizar empresa. Tente novamente.");
            }
        });
    }

    // Event Listeners
    $("#btn-editar").on("click", function() {
        if (empresaAtual) {
            preencherModalEdicao(empresaAtual);
            $('#modalEdicao').modal('show');
        } else {
            alert("Carregue os dados da empresa primeiro.");
        }
    });

    $("#btn-excluir").on("click", function() {
        if (empresaAtual) {
            $('#modalConfirmacao').modal('show');
        } else {
            alert("Carregue os dados da empresa primeiro.");
        }
    });

    $("#confirmar-exclusao").on("click", function() {
        $('#modalConfirmacao').modal('hide');
        excluirEmpresa();
    });

    $("#salvar-edicao").on("click", function() {
        // Validar formulário
        if ($("#form-edicao")[0].checkValidity()) {
            salvarEdicao();
        } else {
            $("#form-edicao")[0].reportValidity();
        }
    });

    // Inicia o carregamento dos dados
    carregarDadosEmpresa();
});
