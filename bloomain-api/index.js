const express = require('express');
const mongoose = require('mongoose');
const User = require('./models/User'); // Importa o modelo que criaste
const app = express();
const PORT = 3000;

// 1. Configuração de Limites para Base64 e JSON
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ limit: '10mb', extended: true }));

// 2. Ligação ao MongoDB Atlas (Adicionei o nome da DB 'bloomainDB')
const mongoURI = "mongodb+srv://aluno25939_db_user:Mitomeioses2!@damcluster.ty3jnxv.mongodb.net/bloomainDB?appName=DAMCluster";

mongoose.connect(mongoURI)
  .then(() => console.log("✅ Ligado ao MongoDB com sucesso!"))
  .catch(err => console.error("❌ Erro ao ligar ao MongoDB:", err));

// --- ROTAS ---

// Rota de teste
app.get('/', (req, res) => {
    res.send('Servidor Bloomain está a funcionar!');
});

// Rota de Registo REAL (Grava no MongoDB)
app.post('/register', async (req, res) => {
    try {
        // 1. Extrair também o profileImage enviado pelo Android
        const { username, email, password, profileImage } = req.body;

        // 2. Criar o novo utilizador incluindo a imagem
        const newUser = new User({
            username,
            email,
            password,
            profileImage: profileImage || "", // Guarda a string Base64 ou vazio se não houver
            stats: { posts: 0, followers: 0, following: 0 }
        });

        await newUser.save();
        res.status(201).json({ message: "Utilizador registado com sucesso!" });
        console.log(`👤 Novo utilizador na DB: ${username} (com foto)`);
    } catch (error) {
        console.error("Erro no registo:", error);
        res.status(400).json({ message: "Erro ao registar. Talvez o user já exista?" });
    }
});

// Rota de Login REAL (Verifica na DB)
app.post('/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        const user = await User.findOne({ username, password });

        if (user) {
            res.status(200).json({
                message: "Login com sucesso!",
                userId: user._id,
                username: user.username
            });
        } else {
            res.status(401).json({ message: "Utilizador ou senha incorretos." });
        }
    } catch (error) {
        res.status(500).json({ message: "Erro no servidor." });
    }
});

// Rota de Perfil (Agora podes buscar por utilizador real no futuro)
// Rota para buscar os dados reais do perfil
app.get('/profile/:userId', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId);
        if (!user) {
            return res.status(404).json({ message: "Utilizador não encontrado" });
        }

        res.json({
            username: user.username,
            bio: user.bio,
            profileImage: user.profileImage, // A String Base64
            stats: {
                posts: user.posts.length,
                followers: user.followers.length,
                following: user.following.length
            }
        });
    } catch (error) {
        res.status(500).json({ message: "Erro ao carregar perfil" });
    }
});

app.listen(PORT, () => {
    console.log(`🚀 Servidor rodando em http://localhost:${PORT}`);
});