package com.example.riskcraft

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riskcraft.adapter.ChatAdapter
import com.example.riskcraft.model.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class GeminiChatActivity : AppCompatActivity() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatInput: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var typingIndicator: TextView
    private lateinit var suggestionsContainer: LinearLayout

    // TODO: Replace with your actual Gemini API key
    private val GEMINI_API_KEY = "AIzaSyBJ2Ta5F_kmYhyTkdR_HgHmXdaI6nEmBhA"

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            },
            systemInstruction = content {
                text(
                    """You are RiskCraft AI Assistant — a friendly, knowledgeable trading mentor built into the RiskCraft stock trading simulation app.

Your role:
- Help users learn about stock market concepts (P&L, stop-loss, market orders, limit orders, etc.)
- Explain trading strategies in simple terms
- Provide guidance on portfolio diversification
- Answer questions about how the RiskCraft app works (virtual trading, wallet, challenges, leaderboard)
- Give beginner-friendly tips for stock and crypto trading
- Explain Indian market indices (NIFTY 50, SENSEX) and popular stocks

Rules:
- Always remind users this is a simulation/educational platform — not real financial advice
- Keep responses concise (2-4 paragraphs max)
- Use simple language, avoid jargon unless explaining it
- Be encouraging and supportive to new traders
- Use bullet points for lists
- If asked about specific stock predictions, clarify you can't predict markets but can explain analysis methods"""
                )
            }
        )
    }

    private val chat by lazy {
        generativeModel.startChat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gemini_chat)

        recyclerView = findViewById(R.id.chatRecyclerView)
        chatInput = findViewById(R.id.chatInput)
        sendBtn = findViewById(R.id.sendBtn)
        typingIndicator = findViewById(R.id.typingIndicator)
        suggestionsContainer = findViewById(R.id.suggestionsContainer)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        // Welcome message
        addAiMessage("Hey! 👋 I'm your RiskCraft AI Assistant.\n\nI can help you with:\n• Trading concepts & strategies\n• Portfolio tips\n• Understanding market terms\n• App features & guidance\n\nAsk me anything!")

        // Back button
        findViewById<ImageView>(R.id.backBtn).setOnClickListener { finish() }

        // Send button
        sendBtn.setOnClickListener { sendMessage() }

        // Enter key sends
        chatInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        // Suggestion chips
        setupChips()

        // Check if opened with a specific question (from TradeActivity, etc.)
        val prefilledQuestion = intent.getStringExtra("question")
        if (!prefilledQuestion.isNullOrEmpty()) {
            chatInput.setText(prefilledQuestion)
            sendMessage()
        }
    }

    private fun setupChips() {
        val chipActions = mapOf(
            R.id.chip1 to "What is P&L in trading?",
            R.id.chip2 to "How should I diversify my portfolio?",
            R.id.chip3 to "Explain stop-loss and how to use it",
            R.id.chip4 to "Give me some tips for beginner traders"
        )

        for ((chipId, question) in chipActions) {
            findViewById<Chip>(chipId)?.setOnClickListener {
                chatInput.setText(question)
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        val text = chatInput.text.toString().trim()
        if (text.isEmpty()) return

        // Hide suggestions after first message
        suggestionsContainer.visibility = View.GONE

        // Add user message
        addUserMessage(text)
        chatInput.setText("")

        // Show typing indicator
        typingIndicator.visibility = View.VISIBLE

        // Send to Gemini
        lifecycleScope.launch {
            try {
                val response = chat.sendMessage(text)
                typingIndicator.visibility = View.GONE
                val reply = response.text ?: "Sorry, I couldn't generate a response. Please try again."
                addAiMessage(reply)
            } catch (e: Exception) {
                typingIndicator.visibility = View.GONE
                addAiMessage("⚠️ Something went wrong: ${e.localizedMessage ?: "Unknown error"}\n\nPlease check your internet connection and try again.")
            }
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(text, isUser = true))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }

    private fun addAiMessage(text: String) {
        messages.add(ChatMessage(text, isUser = false))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }
}
