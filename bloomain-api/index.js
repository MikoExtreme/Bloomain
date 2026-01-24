const express = require('express');
const mongoose = require('mongoose');
const User = require('./models/User');
const Post = require('./models/Post');
const Comment = require('./models/Comment');
const app = express();
const PORT = 3000;
const bcrypt = require('bcrypt');

/**
 * Configuração de Limites para Base64 e JSON
 */
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

/**
 * Ligação ao MongoDB Atlas
 */
const mongoURI = "mongodb+srv://aluno25939_db_user:Dodot123!@damcluster.ty3jnxv.mongodb.net/bloomainDB?appName=DAMCluster";

mongoose.connect(mongoURI)
  .then(() => console.log("Ligado ao MongoDB com sucesso!"))
  .catch(err => console.error("Erro ao ligar ao MongoDB:", err));


/**
 * Rota de teste
 */
app.get('/', (req, res) => {
    res.send('Servidor Bloomain está a funcionar!');
});

/**
 * Registo
 */
app.post('/register', async (req, res) => {
    try {
        const { username, email, password, profileImage } = req.body;

        const existingUser = await User.findOne({ $or: [{ username }, { email }] });
        if (existingUser) {
            const field = existingUser.username === username ? "Username" : "Email";
            return res.status(400).json({ message: `${field} já está em uso.` });
        }

        const saltRounds = 10;
        const hashedPassword = await bcrypt.hash(password, saltRounds);

        const newUser = new User({
            username,
            email,
            password: hashedPassword,
            bio: "",
            profileImage: profileImage || "",
            stats: { posts: 0, followers: 0, following: 0 }
        });

        await newUser.save();
        res.status(201).json({ message: "Utilizador registado com sucesso!" });
    } catch (error) {
        res.status(500).json({ message: "Erro interno no servidor." });
    }
});

/**
 * Login
 */
app.post('/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        const user = await User.findOne({ username });

        if (user && await bcrypt.compare(password, user.password)) {
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

/**
 * Perfil 
 */
app.get('/profile/:userId', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId);
        if (!user) return res.status(404).json({ message: "Utilizador não encontrado" });

        res.json({
            username: user.username,
            bio: user.bio || "",
            profileImage: user.profileImage,
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


/**
 * Criar Post
 */
app.post('/posts', async (req, res) => {
    try {
        const { title, description, postImage, location, creatorId } = req.body;

        const newPost = new Post({
            title,
            description,
            postImage, 
            location,
            creator: creatorId
        });

        const savedPost = await newPost.save();

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




/**
 * Feed
 */
app.get('/posts', async (req, res) => {
    try {
        const posts = await Post.find()
            .populate('creator', 'username profileImage') 
            .sort({ createdAt: -1 }); 

        res.status(200).json(posts);
    } catch (error) {
        res.status(500).json({ message: "Erro ao carregar o feed." });
    }
});




/**
 * Criar Comentário
 */
app.post('/comments', async (req, res) => {
    try {
        const { creatorId, postId, description } = req.body;

        const newComment = new Comment({
            creator: creatorId,
            postId: postId,
            description: description
        });

        const savedComment = await newComment.save();

        await Post.findByIdAndUpdate(postId, {
            $push: { comments: savedComment._id }
        });

        res.status(201).json({ 
            message: "Comentário adicionado!", 
            commentId: savedComment._id 
        });
        console.log(`Novo comentário no post ${postId} pelo user ${creatorId}`);
    } catch (error) {
        console.error(error);
        res.status(400).json({ message: "Erro ao adicionar comentário." });
    }
});


/**
 * Get dos Posts do Utilizador
 */
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


/**
 * Atualizar dados do Utilizador
 */
app.patch('/users/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { loggedInUserId, password, ...updates } = req.body;

        if (id !== loggedInUserId) {
            return res.status(403).json({ message: "Ação não autorizada. Não podes editar este perfil." });
        }

        if (password) {
            const saltRounds = 10;
            updates.password = await bcrypt.hash(password, saltRounds);
        }

        const updatedUser = await User.findByIdAndUpdate(id, updates, { new: true });
        
        if (!updatedUser) {
            return res.status(404).json({ message: "Utilizador não encontrado" });
        }

        res.json(updatedUser);
    } catch (error) {
        res.status(500).json({ error: "Erro ao atualizar perfil: " + error.message });
    }
});



/**
 * Likes
 */
app.post('/posts/:postId/like', async (req, res) => {
    try {
        const { postId } = req.params;
        const { userId } = req.body;

        const post = await Post.findById(postId);
        if (!post) return res.status(404).json({ message: "Post não encontrado" });

        const index = post.likes.indexOf(userId);

        if (index === -1) {
            post.likes.push(userId);
            await post.save();
            res.status(200).json({ message: "Like adicionado!", likes: post.likes });
        } else {
            post.likes.splice(index, 1);
            await post.save();
            res.status(200).json({ message: "Like removido!", likes: post.likes });
        }
    } catch (error) {
        res.status(500).json({ message: "Erro ao processar like" });
    }
});



/**
 * Buscar Comentários
 */
app.get('/posts/:postId/comments', async (req, res) => {
    try {
        const { postId } = req.params;

        
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

        res.status(200).json(post.comments);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Erro ao carregar comentários" });
    }
});




/**
 * Seguir
 */
app.post('/users/:id/follow', async (req, res) => {
    try {
        const { id } = req.params;
        const { followerId } = req.body;

        if (id === followerId) return res.status(400).json({ message: "Não podes seguir-te a ti mesmo" });

        const userToFollow = await User.findById(id);
        const me = await User.findById(followerId);

        const index = userToFollow.followers.indexOf(followerId);

        if (index === -1) {
            userToFollow.followers.push(followerId);
            me.following.push(id);
            await userToFollow.save();
            await me.save();
            res.json({ message: "Seguindo!", isFollowing: true });
        } else {
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


/**
 * Apagar Comentário
 */
app.delete('/comments/:id', async (req, res) => {
    try {
        await Comment.findByIdAndDelete(req.params.id);
        res.json({ message: "Comentário removido" });
    } catch (error) {
        res.status(500).json({ error: "Erro ao apagar comentário" });
    }
});

/**
 * Apagar Post
 */
app.delete('/posts/:id', async (req, res) => {
    try {
        await Post.findByIdAndDelete(req.params.id);
        await Comment.deleteMany({ postId: req.params.id });
        res.json({ message: "Post removido" });
    } catch (error) {
        res.status(500).json({ error: "Erro ao apagar post" });
    }
});

/**
 * Apagar Utilizador
 */
app.delete('/users/:id', async (req, res) => {
    try {
        const userIdToDelete = req.params.id;
        const { loggedInUserId } = req.body; 

        if (userIdToDelete !== loggedInUserId) {
            return res.status(403).json({ message: "Ação não autorizada. Não podes apagar esta conta." });
        }

        await User.updateMany({ following: userIdToDelete}, { $pull: { following: userIdToDelete } });
        await User.updateMany({ followers: userIdToDelete }, { $pull: { followers: userIdToDelete } });

        await Post.deleteMany({ creator: userIdToDelete });
        await Comment.deleteMany({ creatorId: userIdToDelete });

        await User.findByIdAndDelete(userIdToDelete);

        res.json({ message: "Utilizador e conexões removidas com sucesso." });
    } catch (error) {
        res.status(500).json({ error: "Erro ao apagar conta" });
    }
});


/**
 * Detalhes do Post
 */
app.get('/posts/single/:postId', async (req, res) => {
    try {
        const post = await Post.findById(req.params.postId).populate('creator');
        res.json(post);
    } catch (e) {
        res.status(500).send();
    }
});
/**
 * Pesquisar Utilizadores
 */
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


/**
 * Ligar o Servidor
 */
app.listen(PORT, () => {
    console.log(`🚀 Servidor rodando em http://localhost:${PORT}`);
});