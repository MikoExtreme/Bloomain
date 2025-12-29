const mongoose = require('mongoose');

const CommentSchema = new mongoose.Schema({
    // FK para saber quem comentou
    creator: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    
    // FK para saber em que post o comentário está
    postId: { type: mongoose.Schema.Types.ObjectId, ref: 'Post', required: true },
    
    description: { type: String, required: true },
    createdAt: { type: Date, default: Date.now },
    
    // Likes no próprio comentário (também como array de IDs de users)
    likes: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }]
});

module.exports = mongoose.model('Comment', CommentSchema);