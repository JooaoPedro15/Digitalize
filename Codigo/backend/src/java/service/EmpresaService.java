package service;

// Importações do Spark e da biblioteca Gson
import static spark.Spark.*;

// Importações internas do projeto
import dao.EmpresaDAO;
import model.Empresa;

/**
 * Classe responsável por definir os endpoints da API relacionados à entidade Empresa.
 * 
 * Ela utiliza o framework Spark Java para criar rotas REST (GET, POST, PUT, DELETE),
 * e o DAO (Data Access Object) para realizar as operações no banco de dados PostgreSQL.
 */
public class EmpresaService {

    // Objeto de acesso ao banco de dados
    private EmpresaDAO dao = new EmpresaDAO();

    // Gson é usado para converter objetos Java em JSON (e vice-versa)
    private Gson gson = new Gson();

    /**
     * Método que registra todas as rotas relacionadas à Empresa.
     * 
     * Ele é chamado no Main.java, dentro do método main().
     * 
     * Exemplo:
     *    new EmpresaService().routes();
     */
    public void routes() {

        // Cria um grupo de rotas que começam com /empresa
        path("/empresa", () -> {

            /**
             * Rota GET /empresa
             * Retorna a lista de todas as empresas cadastradas no banco.
             */
            get("", (req, res) -> {
                res.type("application/json"); // Define o tipo de resposta
                return gson.toJson(dao.getAll()); // Converte a lista de empresas em JSON
            });

            /**
             * Rota GET /empresa/:cnpj
             * Retorna uma empresa específica com base no CNPJ informado na URL.
             * 
             * Exemplo: GET /empresa/12345678000199
             */
            get("/:cnpj", (req, res) -> {
                res.type("application/json");
                String cnpj = req.params(":cnpj"); // Captura o parâmetro da URL
                Empresa e = dao.getByCnpj(cnpj);   // Busca no banco via DAO

                if (e == null) { // Caso não encontre
                    res.status(404);
                    return gson.toJson("Empresa não encontrada");
                }

                return gson.toJson(e); // Retorna a empresa em JSON
            });

            /**
             * Rota POST /empresa
             * Cria uma nova empresa no banco de dados.
             * 
             * Exemplo de corpo da requisição (JSON):
             * {
             *   "cnpj": "12345678000199",
             *   "nomeFantasia": "TechNova",
             *   "razaoSocial": "TechNova LTDA",
             *   "segmento": "Tecnologia",
             *   "endereco": "Rua Alfa, 123",
             *   "status": "ATIVA"
             * }
             */
            post("", (req, res) -> {
                res.type("application/json");

                // Converte o corpo da requisição (JSON) em um objeto Empresa
                Empresa e = gson.fromJson(req.body(), Empresa.class);

                boolean ok = dao.insert(e); // Insere no banco via DAO

                res.status(ok ? 201 : 400); // 201 Created ou 400 Bad Request
                return gson.toJson(e);
            });

            /**
             * Rota PUT /empresa/:cnpj
             * Atualiza os dados de uma empresa existente.
             * 
             * Exemplo: PUT /empresa/12345678000199
             * Corpo (JSON):
             * {
             *   "nomeFantasia": "TechNova 2.0",
             *   "razaoSocial": "TechNova LTDA",
             *   "segmento": "TI",
             *   "endereco": "Rua Beta, 456",
             *   "status": "ATIVA"
             * }
             */
            put("/:cnpj", (req, res) -> {
                res.type("application/json");

                // Converte o corpo da requisição para objeto
                Empresa e = gson.fromJson(req.body(), Empresa.class);

                // Define o CNPJ da URL como o identificador
                e.setCnpj(req.params(":cnpj"));

                boolean ok = dao.update(e); // Atualiza no banco

                res.status(ok ? 200 : 400); // 200 OK ou 400 Bad Request
                return gson.toJson(e);
            });

            /**
             * Rota DELETE /empresa/:cnpj
             * Remove uma empresa com base no CNPJ.
             * 
             * Exemplo: DELETE /empresa/12345678000199
             */
            delete("/:cnpj", (req, res) -> {
                res.type("application/json");

                boolean ok = dao.delete(req.params(":cnpj"));

                // Retorna 204 (sem corpo) se sucesso, 404 se não encontrada
                res.status(ok ? 204 : 404);
                return ok ? "" : gson.toJson("Empresa não encontrada");
            });
        });
    }
}
