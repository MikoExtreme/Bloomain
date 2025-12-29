const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    profileImage: { type: String, default: "" }, // String Base64
    bio: { type: String, default: "Olá! Estou a usar o Bloomain." },
    
    // Arrays de Foreign Keys (IDs de outros utilizadores)
    followers: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }], 
    following: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
    
    // Array para saber quais posts pertencem a este user
    posts: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Post' }]
});

module.exports = mongoose.model('User', UserSchema);