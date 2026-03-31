package com.clueventure.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import com.clueventure.ui.viewmodel.QuizUiState
import com.clueventure.ui.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionScreen(
    waypointId: String,
    waypointTitle: String,
    onNavigateBack: () -> Unit,
    onAnsweredCorrectly: (pointsEarned: Int) -> Unit,
    viewModel: QuizViewModel = remember { QuizViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(waypointId) {
        viewModel.loadQuestion(waypointId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz: $waypointTitle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is QuizUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadQuestion(waypointId) }) {
                        Text("Retry")
                    }
                }

                is QuizUiState.Question -> {
                    Text(
                        text = state.question.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.question.options) { option ->
                            OutlinedButton(
                                onClick = { viewModel.submitAnswer(option) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(option)
                            }
                        }
                    }

                    state.question.hintText?.let { hint ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "💡 Hint: $hint",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                is QuizUiState.Answered -> {
                    val isCorrect = state.isCorrect
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isCorrect) "✅ Correct!" else "❌ Incorrect",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (!isCorrect) {
                                Text(
                                    text = "Correct answer: ${state.correctAnswer}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (state.pointsEarned > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "+${state.pointsEarned} points",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onAnsweredCorrectly(state.pointsEarned) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue to next waypoint")
                    }
                }
            }
        }
    }
}
