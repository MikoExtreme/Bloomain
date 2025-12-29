const mongoose = require('mongoose');

const PostSchema = new mongoose.Schema({
    title: { type: String, required: true },
    description: { type: String, default: "" },
    postImage: { type: String, required: true }, // String Base64 do post
    location: { type: String, default: "" },    // Não obrigatório
    createdAt: { type: Date, default: Date.now },
    
    // FK para o criador do post
    creator: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    
    // Otimização: Array de IDs de quem deu Like
    likes: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
    
    // Array de IDs dos comentários (FK para a tabela de comentários)
    comments: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Comment' }]
});

module.exports = mongoose.model('Post', PostSchema);