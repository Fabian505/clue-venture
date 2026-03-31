package com.clueventure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clueventure.data.model.Question
import com.clueventure.data.repository.AdventureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuizUiState {
    data object Loading : QuizUiState()
    data class Question(val question: com.clueventure.data.model.Question) : QuizUiState()
    data class Answered(val isCorrect: Boolean, val correctAnswer: String, val pointsEarned: Int) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

class QuizViewModel(
    private val repository: AdventureRepository = AdventureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var currentQuestion: com.clueventure.data.model.Question? = null

    fun loadQuestion(waypointId: String) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            runCatching { repository.getQuestionsForWaypoint(waypointId).firstOrNull() }
                .onSuccess { question ->
                    if (question != null) {
                        currentQuestion = question
                        _uiState.value = QuizUiState.Question(question)
                    } else {
                        _uiState.value = QuizUiState.Error("No question found for this waypoint")
                    }
                }
                .onFailure { _uiState.value = QuizUiState.Error(it.message ?: "Failed to load question") }
        }
    }

    fun submitAnswer(selectedAnswer: String) {
        val question = currentQuestion ?: return
        val isCorrect = selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
        val pointsEarned = if (isCorrect) question.pointsValue else 0
        _uiState.value = QuizUiState.Answered(
            isCorrect = isCorrect,
            correctAnswer = question.correctAnswer,
            pointsEarned = pointsEarned
        )
    }
}
