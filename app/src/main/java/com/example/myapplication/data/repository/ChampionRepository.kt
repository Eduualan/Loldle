package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ChampionDao
import com.example.myapplication.data.local.ChampionEntity
import com.example.myapplication.data.local.GameDao
import com.example.myapplication.data.local.GameEntity
import com.example.myapplication.data.remote.ChampionApi
import com.example.myapplication.data.remote.ChampionDto
import com.example.myapplication.data.remote.ChampionTraitsApi
import com.example.myapplication.data.remote.ChampionTraitsDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException

class ChampionRepository(
    private val champions: ChampionDao,
    private val games: GameDao,
    private val api: ChampionApi,
    private val traitsApi: ChampionTraitsApi
) {
    private val _adminError = MutableStateFlow<String?>(null)
    val adminError = _adminError.asStateFlow()
    private val _gameError = MutableStateFlow<String?>(null)
    val gameError = _gameError.asStateFlow()
    private val _championVersion = MutableStateFlow<String?>(null)
    val championVersion = _championVersion.asStateFlow()
    private var cachedTraits: Map<String, ChampionTraitsDto> = emptyMap()
    private val sampleIds by lazy { sampleChampions.mapTo(mutableSetOf()) { it.id } }

    fun observeChampions(): Flow<List<ChampionEntity>> = champions.observeAll()
    fun observeGames(): Flow<List<GameEntity>> = games.observeAll()

    suspend fun initialise() {
        val savedChampions = safeAdmin { champions.getAll() }.orEmpty()
        val hasOnlySamples = savedChampions.isNotEmpty() && savedChampions.all { it.id in sampleIds }

        if (savedChampions.isEmpty() || hasOnlySamples) {
            val synced = sync()
            if (!synced && savedChampions.isEmpty()) {
                safeAdmin { champions.insertRemote(sampleChampions) }
            }
            return
        }

        refreshVersion()
        refreshSavedTraits(savedChampions)
    }

    suspend fun sync(): Boolean {
        return try {
            val version = api.versions().firstOrNull() ?: error("Versão indisponível")
            _championVersion.value = version

            val traits = loadTraits()
            val remoteChampions = api.champions(version).data.values.map { it.toEntity(traits[it.id]) }
            champions.insertRemote(remoteChampions)
            remoteChampions.forEach { champion ->
                traits[champion.id]?.let { trait -> updateTraits(champion.id, trait) }
            }

            _adminError.value = null
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _adminError.value = "Falha ao atualizar campeões pela API. Confira sua conexão."
            false
        }
    }

    private suspend fun refreshVersion() {
        try {
            _championVersion.value = api.versions().firstOrNull()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Os dados já salvos continuam disponíveis sem conexão.
        }
    }

    private suspend fun refreshSavedTraits(savedChampions: List<ChampionEntity>) {
        val traits = loadTraits()
        savedChampions.forEach { champion ->
            traits[champion.id]?.let { updateTraits(champion.id, it) }
        }
    }

    private suspend fun updateTraits(id: String, traits: ChampionTraitsDto) {
        safeAdmin {
            champions.updateTraits(
                id,
                traits.gender.toPortugueseGender(),
                traits.species.toPortugueseSpecies()
            )
        }
    }

    private fun ChampionDto.toEntity(traits: ChampionTraitsDto?) = ChampionEntity(
        id = id,
        name = name,
        title = title,
        role = tags.joinToString(" / ") { it.toRole() }.ifBlank { "Outro" },
        resource = partype.ifBlank { "Sem recurso" },
        rangeType = if (stats.attackrange > 300) "À distância" else "Corpo a corpo",
        gender = traits?.gender.toPortugueseGender(),
        species = traits?.species.toPortugueseSpecies()
    )
    suspend fun saveChampion(champion: ChampionEntity) = safeAdmin { champions.insert(champion) } != null
    suspend fun updateChampion(champion: ChampionEntity) = safeAdmin { champions.update(champion) > 0 } == true
    suspend fun deleteChampion(champion: ChampionEntity) = safeAdmin { champions.delete(champion) > 0 } == true
    suspend fun saveGame(game: GameEntity) = safeGame { games.insert(game) } != null
    suspend fun updateGame(game: GameEntity) = safeAdmin { games.update(game) > 0 } == true
    suspend fun deleteGame(game: GameEntity) = safeAdmin { games.delete(game) > 0 } == true
    private suspend fun <T> safeAdmin(action: suspend () -> T): T? = try {
        action().also { _adminError.value = null }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        _adminError.value = "Falha ao salvar ou consultar os dados locais."
        null
    }
    private suspend fun <T> safeGame(action: suspend () -> T): T? = try {
        action().also { _gameError.value = null }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        _gameError.value = "Falha ao salvar o resultado da partida."
        null
    }

    private fun String.toRole() = when (this) {
        "Fighter" -> "Lutador"
        "Mage" -> "Mago"
        "Marksman" -> "Atirador"
        "Support" -> "Suporte"
        "Tank" -> "Tanque"
        "Assassin" -> "Assassino"
        else -> this
    }
    private suspend fun loadTraits(): Map<String, ChampionTraitsDto> {
        if (cachedTraits.isNotEmpty()) return cachedTraits
        return try {
            traitsApi.championTraits().associateBy { it.id }.also { cachedTraits = it }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            cachedTraits
        }
    }

    private fun String?.toPortugueseGender() = when (this?.uppercase()) {
        "MALE" -> "Masculino"
        "FEMALE" -> "Feminino"
        "OTHER" -> "Outro"
        else -> ChampionEntity.UNKNOWN_GENDER
    }

    private fun List<String>?.toPortugueseSpecies(): String = this
        ?.map { species ->
            when (species.lowercase()) {
                "human" -> "Humano"
                "vastayan" -> "Vastaya"
                "spirit" -> "Espírito"
                "demon" -> "Demônio"
                "celestial" -> "Celestial"
                "undead" -> "Morto-vivo"
                "void-born", "voidborn" -> "Nascido do Vazio"
                "dragon" -> "Dragão"
                "minotaur" -> "Minotauro"
                "god" -> "Divindade"
                "yordle", "darkin", "brackern", "golem", "troll" -> species
                else -> species.replaceFirstChar { it.uppercase() }
            }
        }
        ?.joinToString(" / ")
        ?.ifBlank { ChampionEntity.UNKNOWN_SPECIES }
        ?: ChampionEntity.UNKNOWN_SPECIES

    private val sampleChampions = listOf(
        ChampionEntity("Ahri", "Ahri", "A Raposa de Nove Caudas", "Mago", "Mana", "À distância"),
        ChampionEntity("Ashe", "Ashe", "A Arqueira do Gelo", "Atirador", "Mana", "À distância"),
        ChampionEntity("Garen", "Garen", "O Poder de Demacia", "Lutador / Tanque", "Sem mana", "Corpo a corpo"),
        ChampionEntity("Lux", "Lux", "A Dama da Luz", "Mago", "Mana", "À distância"),
        ChampionEntity("Yasuo", "Yasuo", "O Imperdoável", "Lutador / Assassino", "Fluxo", "Corpo a corpo")
    )
}
