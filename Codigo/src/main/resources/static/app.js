
document.getElementById('empresaForm').addEventListener('submit', function(event) {

      
        $("#cnpj").inputmask("99.999.999/9999-99");
        $("#responsavelTelefone").inputmask("(99) 99999-9999");  

      event.preventDefault();

      const form = event.target;
      if (!form.checkValidity()) {
        alert("Por favor, preencha todos os campos obrigatórios corretamente.");
        return;
      }

          // Captura os checkboxes marcados
    const selecionados = Array.from(document.querySelectorAll('input[name="servicosInteresse"]:checked'))
      .map(checkbox => checkbox.value);


      const dados = {
            id: Date.now(),
            razaoSocial: $("#razaoSocial").val(),
            nomeFantasia: $("#nomeFantasia").val(),
            cnpj: $("#cnpj").val(),
            segmento: $("#segmento").val(),
            endereco:{
              estado: $("#estado").val(),
              cidade: $("#cidade").val(),
              rua: $("#rua").val(),
              cep: $("#cep").val(),
            },
            responsavelNome: $("#responsavelNome").val(),
            responsavelCargo: $("#responsavelCargo").val(),
            responsavelEmail: $("#responsavelEmail").val(),
            responsavelTelefone: $("#responsavelTelefone").val(),
            servicosInteresse: selecionados,
            observacoes: $("#observacoes").val(),

      };
      // envia os dados para o localStorage
      const empresas = JSON.parse(localStorage.getItem("empresas")) || [];
      empresas.push(dados);
      localStorage.setItem("empresas", JSON.stringify(empresas));

      // envia os dados para o servidor via fetch
      fetch('/admin/cadastro', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dados)
      })
      .then(response => response.json())
      .then(data => {
        alert("Cadastro enviado com sucesso!");
        console.log("Resposta do servidor:", data);
        form.reset();
      })
      .catch(error => {
        console.error("Erro ao enviar:", error);
        alert("Erro ao enviar dados.");
      });
    });