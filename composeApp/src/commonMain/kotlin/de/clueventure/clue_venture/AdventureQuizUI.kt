package de.clueventure.clue_venture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quiz screen showing questions with ABCD answers between checkpoints
 */
@Composable
fun QuizScreen(
    questions: List<QuizQuestion>,
    onQuizCompleted: (correctAnswerCount: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (questions.isEmpty()) {
        return
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    var showResults by remember { mutableStateOf(false) }
    var correctAnswerCount by remember { mutableStateOf(0) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val isLastQuestion = currentQuestionIndex >= questions.size - 1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Progress indicator
            Text(
                text = "Frage ${currentQuestionIndex + 1} von ${questions.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((currentQuestionIndex + 1) / questions.size.toFloat())
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Card
            currentQuestion?.let { question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = question.questionText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Answer options
                        val answers = question.answers.sortedBy { it.answerOrder }
                        answers.forEach { answer ->
                            AnswerButton(
                                text = answer.answerText,
                                isSelected = selectedAnswers[question.id] == answer.id,
                                isCorrect = answer.isCorrect,
                                showResult = showResults,
                                onClick = {
                                    if (!showResults) {
                                        selectedAnswers = selectedAnswers.toMutableMap().apply {
                                            this[question.id] = answer.id
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result feedback (shown after submission)
            AnimatedVisibility(
                visible = showResults,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                currentQuestion?.let { question ->
                    val selectedAnswerId = selectedAnswers[question.id]
                    val selectedAnswer = question.answers.find { it.id == selectedAnswerId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (selectedAnswer?.isCorrect == true) {
                                Text(
                                    text = "✅ Richtig!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                )
                                Text(
                                    text = "+100 Punkte",
                                    fontSize = 14.sp,
                                    color = Color(0xFF4CAF50),
                                )
                            } else {
                                Text(
                                    text = "❌ Leider falsch",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5722),
                                )
                                Text(
                                    text = "Richtig: ${question.answers.find { it.isCorrect }?.answerText}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            if (!showResults) {
                Button(
                    onClick = {
                        val isCorrect = questions[currentQuestionIndex].answers
                            .find { it.id == selectedAnswers[questions[currentQuestionIndex].id] }
                            ?.isCorrect ?: false
                        if (isCorrect) correctAnswerCount++
                        showResults = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = selectedAnswers.containsKey(questions[currentQuestionIndex].id),
                ) {
                    Text("Antwort prüfen")
                }
            } else {
                Button(
                    onClick = {
                        if (isLastQuestion) {
                            onQuizCompleted(correctAnswerCount)
                        } else {
                            currentQuestionIndex++
                            showResults = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(if (isLastQuestion) "Quiz abgeschlossen!" else "Nächste Frage")
                }
            }

            if (showResults && !isLastQuestion) {
                Button(
                    onClick = {
                        if (isLastQuestion) {
                            onQuizCompleted(correctAnswerCount)
                        } else {
                            currentQuestionIndex++
                            showResults = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text("Oder weiter")
                }
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showResult: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = when {
        showResult && isCorrect -> Color(0xFF4CAF50)
        showResult && isSelected && !isCorrect -> Color(0xFFFF5722)
        isSelected && !showResult -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        showResult && isCorrect -> Color.White
        showResult && isSelected && !isCorrect -> Color.White
        isSelected && !showResult -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(enabled = !showResult) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected && !showResult) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        showResult && isCorrect -> "✓"
                        showResult && isSelected && !isCorrect -> "✗"
                        else -> ""
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
