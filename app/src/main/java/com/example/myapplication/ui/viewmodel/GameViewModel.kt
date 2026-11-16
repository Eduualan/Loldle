package com.example.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.ChampionEntity
import com.example.myapplication.data.local.GameEntity
import com.example.myapplication.data.repository.ChampionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

data class GuessResult(
    val champion: ChampionEntity,
    val sameRole: Boolean,
    val sameResource: Boolean,
    val sameRange: Boolean
)

data class GameUiState(
    val champions: List<ChampionEntity> = emptyList(),
    val guesses: List<GuessResult> = emptyList(),
    val guess: String = "",
    val message: String = "Adivinhe o campeão em até 6 tentativas.",
    val finished: Boolean = false,
    val player: String = "Jogador",
    val error: String? = null
)

class GameViewModel(private val repository: ChampionRepository) : ViewModel() {
    var state by mutableStateOf(GameUiState())
        private set
    private var dailyAnswer: ChampionEntity? = null

    init {
        viewModelScope.launch {
            repository.observeChampions()
                .catch { state = state.copy(error = "Não foi possível carregar os campeões."); emit(emptyList()) }
                .collect { list ->
                state = state.copy(champions = list)
                if (dailyAnswer == null && list.isNotEmpty()) {
                    val sorted = list.sortedBy { it.name }
                    dailyAnswer = sorted[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % sorted.size]
                } else if (dailyAnswer != null && list.none { it.id == dailyAnswer?.id } && list.isNotEmpty()) {
                    dailyAnswer = list.random()
                }
            }
        }
        viewModelScope.launch { repository.gameError.collect { state = state.copy(error = it) } }
    }

    fun setPlayer(name: String) { state = state.copy(player = name.ifBlank { "Jogador" }) }
    fun updateGuess(value: String) { state = state.copy(guess = value.take(40), error = null) }

    fun suggestions(): List<ChampionEntity> = state.champions
        .filter { state.guess.isNotBlank() && it.name.contains(state.guess.trim(), ignoreCase = true) }
        .sortedBy { it.name }
        .take(6)

    fun submitGuess() {
        val list = state.champions.sortedBy { it.name }
        if (list.isEmpty()) { state = state.copy(message = "Carregando a lista de campeões…"); return }
        val guess = list.firstOrNull { it.name.equals(state.guess.trim(), true) }
        if (state.finished) return
        if (guess == null) { state = state.copy(message = "Escolha um campeão da lista."); return }
        if (state.guesses.any { it.champion.id == guess.id }) {
            state = state.copy(message = "Você já tentou esse campeão.")
            return
        }
        val answer = dailyAnswer ?: return
        val guesses = state.guesses + GuessResult(
            guess, guess.role == answer.role, guess.resource == answer.resource, guess.rangeType == answer.rangeType
        )
        val won = guess.id == answer.id
        val player = state.player
        val finished = won || guesses.size >= 6 || guesses.size == list.size
        if (won) {
            // Registra o resultado e já prepara uma rodada nova, preservando a mensagem de vitória.
            dailyAnswer = nextAnswer(list, answer.id)
            state = state.copy(
                guesses = emptyList(),
                guess = "",
                finished = false,
                message = "Acertou! O campeão era ${answer.name}. Nova rodada iniciada."
            )
            viewModelScope.launch {
                repository.saveGame(GameEntity(player = player, champion = answer.name, won = true, attempts = guesses.size))
            }
            return
        }
        state = state.copy(
            guesses = guesses, guess = "", finished = finished,
            message = if (finished) "Fim de jogo! O campeão era ${answer.name}." else "Veja as dicas e tente novamente."
        )
        if (finished) viewModelScope.launch {
            repository.saveGame(GameEntity(player = player, champion = answer.name, won = false, attempts = guesses.size))
        }
    }

    fun restart() {
        dailyAnswer = nextAnswer(state.champions.sortedBy { it.name }, dailyAnswer?.id)
        state = state.copy(guesses = emptyList(), guess = "", finished = false, message = "Adivinhe o campeão em até 6 tentativas.")
    }

    private fun nextAnswer(champions: List<ChampionEntity>, previousId: String?): ChampionEntity? {
        if (champions.isEmpty()) return null
        val alternatives = champions.filterNot { it.id == previousId }
        return (alternatives.ifEmpty { champions }).random()
    }
}
