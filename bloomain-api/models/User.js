const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
    username: {
        type: String,
        required: [true, 'O nome de utilizador é obrigatório'],
        unique: true, // Garante que não existem dois nomes iguais
        trim: true,
        minlength: [3, 'O nome deve ter pelo menos 3 caracteres']
    },
    email: {
        type: String,
        required: [true, 'O email é obrigatório'],
        unique: true,
        lowercase: true,
        trim: true,
        // Regex para validar formato de email
        match: [/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/, 'Por favor insira um email válido']
    },
    password: {
        type: String,
        required: [true, 'A password é obrigatória'],
        minlength: [8, 'A password deve ter pelo menos 8 caracteres']
    },
    profileImage: { type: String, default: "" }, // String Base64
    bio: { type: String, default: "Olá! Estou a usar o Bloomain." },
    
    // Arrays de Foreign Keys (IDs de outros utilizadores)
    followers: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }], 
    following: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
    
    // Array para saber quais posts pertencem a este user
    posts: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Post' }]
});

module.exports = mongoose.model('User', UserSchema);