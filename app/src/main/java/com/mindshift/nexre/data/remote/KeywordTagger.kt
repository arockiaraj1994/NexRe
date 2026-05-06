package com.mindshift.nexre.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordTagger @Inject constructor() {

    private val rules = listOf(
        listOf("android", "kotlin", "compose", "jetpack") to "Android",
        listOf("python", "django", "fastapi", "flask") to "Python",
        listOf("ai", "llm", "ml", "gpt", "gemini", "machine learning", "neural") to "AI",
        listOf("github", "open source", "opensource") to "GitHub",
        listOf("security", "cve", "vulnerability", "exploit") to "Security",
        listOf("finance", "stock", "investing", "crypto") to "Finance",
        listOf("react", "vue", "frontend", "css", "html", "javascript", "typescript") to "Frontend",
        listOf("docker", "kubernetes", "devops", "ci/cd", "helm") to "DevOps",
        listOf("go", "golang") to "Go",
        listOf("rust") to "Rust",
        listOf("database", "sql", "postgres", "mysql", "mongodb") to "Database",
        listOf("backend", "api", "rest", "graphql", "microservice") to "Backend",
        listOf("research", "paper", "arxiv", "study") to "Research",
    )

    fun tag(title: String, description: String): List<String> {
        val text = "$title $description".lowercase()
        return rules
            .filter { (keywords, _) -> keywords.any { it in text } }
            .map { (_, tag) -> tag }
            .distinct()
    }
}
