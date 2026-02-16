Requisitos:
	1-Instalar o Node
	2-Instalar o MongoDB Server
	3-Instalar o MongoDB Compass (Apoio Visual)

Processo:
	1-Após clonar o GitHub do projeto e ter feita as instalações, deve ir para a pasta da API (../bloomain_api) através da linha de comandos (CMD)
	2-Executar "node index.js"
	3-O servidor vai correr na sua máquina e vai tentar aceder á base de dados da MongoDB Atlas (Cloud)
	4-Caso falhe, irá aceder ao MongoDB Local

Configuração do MongoDB Compass:
	1-Clicar em "Add new connection"
	2-Onde diz "URI", escrever isto: "mongodb://localhost:27017"
	3-Clicar "Save & Connect"
	4-Após executar o "node index.js", a base de dados correta irá aparecer "BloomainDB"

Cuidados a ter no código do projeto:
	1-No ficheiro de código "RetrofitClient.kt", terá de alterar o "BASE_URL" para o IP da máquina onde o servidor está a correr
	2-Se a rede em que se encontra permitir aceder ao MongoDB Atlas, todas as pessoas que tiverem o código e quiserem simular, terão de colocar o BASE_URL igual ao IP da máquina onde o servidor está a correr
	3-Caso queira correr várias servidores ao mesmo tempo, terá de colocar uma porta diferente

