const express = require('express');
const mongoose = require('mongoose');
const User = require('./models/User'); // Importa o modelo que criaste
const Post = require('./models/Post');
const Comment = require('./models/Comment');
const app = express();
const PORT = 3000;

// 1. Configuração de Limites para Base64 e JSON
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// 2. Ligação ao MongoDB Atlas (Adicionei o nome da DB 'bloomainDB')
const mongoURI = "mongodb+srv://aluno25939_db_user:Dodot123!@damcluster.ty3jnxv.mongodb.net/bloomainDB?appName=DAMCluster";

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
        const { username, email, password, profileImage } = req.body;

        // 1. Validar formato de email no servidor (Segurança extra)
        const emailRegex = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
        if (!emailRegex.test(email)) {
            return res.status(400).json({ message: "O formato do email é inválido." });
        }

        // 2. Verificar se o username já existe
        const existingUser = await User.findOne({ username });
        if (existingUser) {
            return res.status(400).json({ message: "Este nome de utilizador já está em uso." });
        }

        // 3. Verificar se o email já existe
        const existingEmail = await User.findOne({ email });
        if (existingEmail) {
            return res.status(400).json({ message: "Este email já está registado." });
        }

        // 4. Validar força da password (opcional, mas recomendado)
        if (password.length < 8) {
            return res.status(400).json({ message: "A password deve ter pelo menos 8 caracteres." });
        }

        // 5. Criar o novo utilizador
        const newUser = new User({
            username,
            email,
            password, // Lembra-te que o ideal é usar bcrypt.hash(password, 10) aqui!
            bio: "",
            profileImage: profileImage || "",
            stats: { posts: 0, followers: 0, following: 0 }
        });

        await newUser.save();
        res.status(201).json({ message: "Utilizador registado com sucesso!" });
        console.log(`👤 Novo utilizador na DB: ${username}`);

    } catch (error) {
        console.error("Erro no registo:", error);
        res.status(500).json({ message: "Erro interno no servidor ao processar o registo." });
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
        if (!user) return res.status(404).json({ message: "Utilizador não encontrado" });

        res.json({
            username: user.username,
            bio: user.bio || "",
            profileImage: user.profileImage,
            stats: {
                // IMPORTANTE: Usar .length para enviar um número e não a lista toda
                posts: user.posts.length,
                followers: user.followers.length,
                following: user.following.length
            }
        });
    } catch (error) {
        res.status(500).json({ message: "Erro ao carregar perfil" });
    }
});


app.post('/posts', async (req, res) => {
    try {
        const { title, description, postImage, location, creatorId } = req.body;

        const newPost = new Post({
            title,
            description,
            postImage, // String Base64
            location,
            creator: creatorId
        });

        const savedPost = await newPost.save();

        // IMPORTANTE: Adicionar o ID do post à lista de posts do utilizador
        await User.findByIdAndUpdate(creatorId, {
            $push: { posts: savedPost._id }
        });

        res.status(201).json({ message: "Post publicado com sucesso!", postId: savedPost._id });
        console.log(`📸 Novo post criado por: ${creatorId}`);
    } catch (error) {
        console.error(error);
        res.status(400).json({ message: "Erro ao publicar post." });
    }
});




// 2. Buscar todos os posts (Lobby / Feed)
// Usamos .populate('creator') para trazer o nome e foto do dono do post
app.get('/posts', async (req, res) => {
    try {
        const posts = await Post.find()
            .populate('creator', 'username profileImage') 
            .sort({ createdAt: -1 }); // Mais recentes primeiro

        res.status(200).json(posts);
    } catch (error) {
        res.status(500).json({ message: "Erro ao carregar o feed." });
    }
});

// --- ROTA DE SETTINGS (Exemplo: Mudar Bio) ---
app.put('/settings/bio', async (req, res) => {
    try {
        const { userId, newBio } = req.body;
        await User.findByIdAndUpdate(userId, { bio: newBio });
        res.status(200).json({ message: "Bio atualizada!" });
    } catch (error) {
        res.status(500).json({ message: "Erro ao atualizar definições." });
    }
});


app.post('/comments', async (req, res) => {
    try {
        const { creatorId, postId, description } = req.body;

        const newComment = new Comment({
            creator: creatorId,
            postId: postId,
            description: description
        });

        const savedComment = await newComment.save();

        // IMPORTANTE: Adicionar o ID do comentário à lista de comments do Post
        await Post.findByIdAndUpdate(postId, {
            $push: { comments: savedComment._id }
        });

        res.status(201).json({ 
            message: "Comentário adicionado!", 
            commentId: savedComment._id 
        });
        console.log(`💬 Novo comentário no post ${postId} pelo user ${creatorId}`);
    } catch (error) {
        console.error(error);
        res.status(400).json({ message: "Erro ao adicionar comentário." });
    }
});


app.get('/posts/user/:userId', async (req, res) => {
    try {
        const posts = await Post.find({ creator: req.params.userId })
            .populate('creator', 'username profileImage')
            .sort({ createdAt: -1 });
        res.status(200).json(posts);
    } catch (error) {
        res.status(500).json({ message: "Erro ao buscar posts do utilizador" });
    }
});


// Rota para atualizar o utilizador
app.patch('/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const updates = req.body; // Aqui virá o { "profileImage": "base64..." }

        const updatedUser = await User.findByIdAndUpdate(id, updates, { new: true });
        
        if (!updatedUser) {
            return res.status(404).json({ message: "Utilizador não encontrado" });
        }

        res.json(updatedUser);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});



// Rota para Alternar Like (Toggle Like)
app.post('/posts/:postId/like', async (req, res) => {
    try {
        const { postId } = req.params;
        const { userId } = req.body;

        const post = await Post.findById(postId);
        if (!post) return res.status(404).json({ message: "Post não encontrado" });

        // Verifica se o utilizador já deu like
        const index = post.likes.indexOf(userId);

        if (index === -1) {
            // Se não deu like, adiciona o ID do utilizador ao array
            post.likes.push(userId);
            await post.save();
            res.status(200).json({ message: "Like adicionado!", likes: post.likes });
        } else {
            // Se já deu like, remove o ID (Desfazer Like)
            post.likes.splice(index, 1);
            await post.save();
            res.status(200).json({ message: "Like removido!", likes: post.likes });
        }
    } catch (error) {
        res.status(500).json({ message: "Erro ao processar like" });
    }
});




app.get('/posts/:postId/comments', async (req, res) => {
    try {
        const { postId } = req.params;

        // 1. Procuramos o post pelo ID
        // 2. Usamos o .populate para buscar os dados dos comentários
        // 3. Dentro do comentário, fazemos outro .populate para saber quem escreveu (username e foto)
        const post = await Post.findById(postId).populate({
            path: 'comments',
            populate: {
                path: 'creator',
                select: 'username profileImage'
            }
        });

        if (!post) {
            return res.status(404).json({ message: "Post não encontrado" });
        }

        // Retornamos apenas a lista de comentários do post
        res.status(200).json(post.comments);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Erro ao carregar comentários" });
    }
});



app.patch('/users/:id/password', async (req, res) => {
    const { newPassword } = req.body;
    // Aqui deves atualizar o campo password no teu modelo User do MongoDB
    await User.findByIdAndUpdate(req.params.id, { password: newPassword });
    res.json({ message: "Password atualizada com sucesso!" });
});


app.patch('/users/:id', async (req, res) => {
    try {
        const updates = req.body;
        // Se houver password, podes querer encriptá-la aqui antes de guardar
        const user = await User.findByIdAndUpdate(req.params.id, updates, { new: true });
        res.json(user);
    } catch (err) {
        res.status(500).send("Erro ao atualizar perfil");
    }
});


app.post('/users/:id/follow', async (req, res) => {
    try {
        const { id } = req.params; // ID de quem vai ser seguido
        const { followerId } = req.body; // O teu ID (quem está a clicar no botão)

        if (id === followerId) return res.status(400).json({ message: "Não podes seguir-te a ti mesmo" });

        const userToFollow = await User.findById(id);
        const me = await User.findById(followerId);

        const index = userToFollow.followers.indexOf(followerId);

        if (index === -1) {
            // Seguir
            userToFollow.followers.push(followerId);
            me.following.push(id);
            await userToFollow.save();
            await me.save();
            res.json({ message: "Seguindo!", isFollowing: true });
        } else {
            // Deixar de seguir (Unfollow)
            userToFollow.followers.splice(index, 1);
            me.following.splice(me.following.indexOf(id), 1);
            await userToFollow.save();
            await me.save();
            res.json({ message: "Deixaste de seguir", isFollowing: false });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});


// 1. Apagar um Comentário
app.delete('/comments/:id', async (req, res) => {
    try {
        await Comment.findByIdAndDelete(req.params.id);
        res.json({ message: "Comentário removido" });
    } catch (error) {
        res.status(500).json({ error: "Erro ao apagar comentário" });
    }
});

// 2. Apagar uma Publicação (Post)
app.delete('/posts/:id', async (req, res) => {
    try {
        await Post.findByIdAndDelete(req.params.id);
        // Opcional: Apagar também os comentários deste post
        await Comment.deleteMany({ postId: req.params.id });
        res.json({ message: "Post removido" });
    } catch (error) {
        res.status(500).json({ error: "Erro ao apagar post" });
    }
});

// 3. Apagar a Conta
app.delete('/users/:id', async (req, res) => {
    try {
        const userId = req.params.id;

        // 1. Remover o ID deste utilizador das listas de 'following' de toda a gente
        // (Quem o seguia, deixa de o seguir)
        await User.updateMany(
            { following: userId }, 
            { $pull: { following: userId } }
        );

        // 2. Remover o ID deste utilizador das listas de 'followers' de toda a gente
        // (Quem ele seguia, perde um seguidor)
        await User.updateMany(
            { followers: userId }, 
            { $pull: { followers: userId } }
        );

        // 3. Apagar os dados criados por ele (Posts e Comentários)
        await Post.deleteMany({ creator: userId });
        await Comment.deleteMany({ creatorId: userId });

        // 4. Finalmente, apagar o utilizador em si
        await User.findByIdAndDelete(userId);

        res.json({ message: "Utilizador e conexões removidas com sucesso." });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Erro ao limpar rasto do utilizador" });
    }
});


app.get('/posts/single/:postId', async (req, res) => {
    try {
        const post = await Post.findById(req.params.postId).populate('creator');
        res.json(post);
    } catch (e) {
        res.status(500).send();
    }
});


app.get('/users/search/:query', async (req, res) => {
    try {
        const query = req.params.query;
        // Procura utilizadores onde o username contém a 'query'
        const users = await User.find({
            username: { $regex: query, $options: 'i' }
        }).select('username profileImage _id bio'); // Apenas dados necessários

        res.json(users);
    } catch (error) {
        res.status(500).json({ error: "Erro na pesquisa" });
    }
});


app.listen(PORT, () => {
    console.log(`🚀 Servidor rodando em http://localhost:${PORT}`);
});