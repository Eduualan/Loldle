package com.example.myapplication.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import com.example.myapplication.data.local.ChampionEntity
import com.example.myapplication.data.local.GameEntity
import com.example.myapplication.data.repository.ChampionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class AdminUiState(
    val champions: List<ChampionEntity> = emptyList(),
    val games: List<GameEntity> = emptyList(),
    val championVersion: String? = null,
    val error: String? = null,
    val notice: String? = null,
    val saveSucceeded: Boolean = false,
    val gameUpdateSucceeded: Boolean = false
)

class AdminViewModel(private val repository: ChampionRepository) : ViewModel() {
    var state by mutableStateOf(AdminUiState())
        private set
    init {
        viewModelScope.launch {
            repository.observeChampions()
                .catch { state = state.copy(error = "Não foi possível ler os campeões salvos."); emit(emptyList()) }
                .collect { state = state.copy(champions = it) }
        }
        viewModelScope.launch {
            repository.observeGames()
                .catch { state = state.copy(error = "Não foi possível ler o histórico salvo."); emit(emptyList()) }
                .collect { state = state.copy(games = it) }
        }
        viewModelScope.launch { repository.adminError.collect { state = state.copy(error = it) } }
        viewModelScope.launch { repository.championVersion.collect { state = state.copy(championVersion = it) } }
    }

    fun sync() = viewModelScope.launch {
        state = state.copy(notice = "Atualizando lista da Data Dragon…")
        val synced = repository.sync()
        state = state.copy(notice = if (synced) "Lista sincronizada." else "A sincronização falhou.")
    }

    fun delete(champion: ChampionEntity) = viewModelScope.launch {
        state = state.copy(notice = if (repository.deleteChampion(champion)) "Campeão excluído." else "Não foi possível excluir o campeão.")
    }
    fun deleteGame(game: GameEntity) = viewModelScope.launch {
        state = state.copy(notice = if (repository.deleteGame(game)) "Registro excluído." else "Não foi possível excluir o registro.")
    }

    fun updateGame(game: GameEntity) = viewModelScope.launch {
        state = state.copy(gameUpdateSucceeded = false, notice = null)
        if (game.player.isBlank() || game.champion.isBlank() || game.attempts !in 1..6) {
            state = state.copy(notice = "Informe jogador, campeão e de 1 a 6 tentativas.")
            return@launch
        }
        val saved = repository.updateGame(game)
        state = state.copy(
            notice = if (saved) "Partida atualizada." else "Não foi possível atualizar a partida.",
            gameUpdateSucceeded = saved
        )
    }

    fun save(
        id: String?,
        name: String,
        title: String,
        role: String,
        resource: String,
        range: String,
        gender: String,
        species: String,
        region: String
    ) = viewModelScope.launch {
        state = state.copy(saveSucceeded = false, notice = null)
        if (name.isBlank() || title.isBlank() || role.isBlank()) {
            state = state.copy(notice = "Preencha nome, título e classe.")
            return@launch
        }
        if (state.champions.any { it.id != id && it.name.equals(name.trim(), true) }) {
            state = state.copy(notice = "Já existe um campeão com esse nome.")
            return@launch
        }
        val key = id ?: name.trim().replace(Regex("[^A-Za-z0-9]"), "").ifBlank { "champion${System.currentTimeMillis()}" }
        val champion = ChampionEntity(
            id = key,
            name = name.trim(),
            title = title.trim(),
            role = role.trim(),
            resource = resource.trim().ifBlank { "Desconhecido" },
            rangeType = range.trim().ifBlank { "Desconhecido" },
            gender = gender.trim().ifBlank { ChampionEntity.UNKNOWN_GENDER },
            species = species.trim().ifBlank { ChampionEntity.UNKNOWN_SPECIES },
            region = region.trim().ifBlank { ChampionEntity.UNKNOWN_REGION }
        )
        val saved = if (id == null) repository.saveChampion(champion) else repository.updateChampion(champion)
        state = state.copy(
            notice = if (saved) "Campeão salvo." else "Não foi possível salvar o campeão.",
            saveSucceeded = saved
        )
    }

    fun clearSaveSucceeded() { state = state.copy(saveSucceeded = false) }
    fun clearGameUpdateSucceeded() { state = state.copy(gameUpdateSucceeded = false) }

    fun filteredChampions(query: String): List<ChampionEntity> {
        if (query.isBlank()) return state.champions.sortedBy { it.name }

        return state.champions.filter { champion ->
            listOf(champion.name, champion.title, champion.role, champion.gender, champion.species, champion.region)
                .any { value -> value.contains(query.trim(), ignoreCase = true) }
        }.sortedBy { it.name }
    }

    fun ranking(): List<Pair<String, Int>> = state.games
        .filter { it.won }
        .groupBy { it.player }
        .map { (player, wins) -> player to wins.size }
        .sortedByDescending { it.second }
}
