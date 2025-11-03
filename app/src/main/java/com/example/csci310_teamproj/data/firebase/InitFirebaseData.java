package com.example.csci310_teamproj.data.firebase;

import com.google.firebase.database.DatabaseReference;
import java.util.HashMap;
import java.util.Map;

public class InitFirebaseData {

    public static void seedTestData() {
        DatabaseReference postsRef = FirebaseHelper.getPostsRef();
        DatabaseReference promptsRef = FirebaseHelper.getPromptsRef();

        // 📝 Sample post
        String postId = postsRef.push().getKey();
        if (postId != null) {
            Map<String, Object> post = new HashMap<>();
            post.put("title", "How GPT-4 helped my research");
            post.put("authorId", "test_user_id");
            post.put("authorName", "Test User");
            post.put("llmTag", "GPT-4");
            post.put("body", "I used GPT-4 for text summarization...");
            post.put("timestamp", System.currentTimeMillis());
            post.put("upvotes", 3);
            post.put("downvotes", 0);
            postsRef.child(postId).setValue(post);
        }

        // 💬 Sample prompt
        String promptId = promptsRef.push().getKey();
        if (promptId != null) {
            Map<String, Object> prompt = new HashMap<>();
            prompt.put("title", "Summarize any academic paper");
            prompt.put("authorId", "test_user_id");
            prompt.put("authorName", "Test User");
            prompt.put("description", "A prompt for academic paper summaries");
            prompt.put("text", "Summarize the following text in 3 bullet points:");
            prompt.put("llmTag", "Claude 3");
            prompt.put("timestamp", System.currentTimeMillis());
            promptsRef.child(promptId).setValue(prompt);
        }
    }
}
