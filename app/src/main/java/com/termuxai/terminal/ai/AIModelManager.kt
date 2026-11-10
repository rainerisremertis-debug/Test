package com.termuxai.terminal.ai

import android.content.Context
import android.util.Log
import com.termuxai.terminal.TerminalApplication
import kotlinx.coroutines.*
import java.io.File

object AIModelManager {
    private const val TAG = "AIModelManager"
    private lateinit var context: Context
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val models = mutableMapOf<String, AIModel>()

    fun initialize(ctx: Context) {
        context = ctx
        // Pre-configure Gemma models
        registerModel(AIModel(
            id = "gemma-2b",
            name = "Gemma 2B",
            description = "Google's Gemma 2B instruction-tuned model",
            size = 1_500_000_000L, // ~1.5GB
            url = "https://huggingface.co/google/gemma-2b-it-gguf/resolve/main/gemma-2b-it-q4_k_m.gguf",
            format = ModelFormat.GGUF,
            backend = Backend.EXECUTORCH,
            hasRAG = true,
            hasSkillLearning = true
        ))
        registerModel(AIModel(
            id = "gemma-7b",
            name = "Gemma 7B",
            description = "Google's Gemma 7B instruction-tuned model",
            size = 4_500_000_000L, // ~4.5GB
            url = "https://huggingface.co/google/gemma-7b-it-gguf/resolve/main/gemma-7b-it-q4_k_m.gguf",
            format = ModelFormat.GGUF,
            backend = Backend.EXECUTORCH,
            hasRAG = true,
            hasSkillLearning = true
        ))
    }

    private fun registerModel(model: AIModel) {
        models[model.id] = model
    }

    fun getAvailableModels(): List<AIModel> = models.values.toList()

    fun getModel(id: String): AIModel? = models[id]

    suspend fun downloadModel(modelId: String, onProgress: (Float) -> Unit): Boolean {
        val model = models[modelId] ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val modelsDir = File(context.filesDir, "models")
                modelsDir.mkdirs()
                val modelFile = File(modelsDir, "${model.id}.gguf")

                // Download with progress
                // In production, use OkHttp with progress callback
                Log.d(TAG, "Downloading model: ${model.name}")
                // Simulated download progress
                for (i in 0..100) {
                    onProgress(i / 100f)
                    delay(50)
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download model: ${model.name}", e)
                false
            }
        }
    }

    fun isModelDownloaded(modelId: String): Boolean {
        val modelFile = File(context.filesDir, "models/${modelId}.gguf")
        return modelFile.exists()
    }

    fun getModelPath(modelId: String): String? {
        val modelFile = File(context.filesDir, "models/${modelId}.gguf")
        return if (modelFile.exists()) modelFile.absolutePath else null
    }
}

data class AIModel(
    val id: String,
    val name: String,
    val description: String,
    val size: Long,
    val url: String,
    val format: ModelFormat,
    val backend: Backend,
    val hasRAG: Boolean = false,
    val hasSkillLearning: Boolean = false
)

enum class ModelFormat { GGUF, ONNX, TFLITE, EXECUTORCH }
enum class Backend { EXECUTORCH, TENSORFLOW_LITE, LLAMA_CPP }