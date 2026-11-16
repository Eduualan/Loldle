package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.local.ChampionEntity
import com.example.myapplication.data.local.GameEntity
import com.example.myapplication.ui.viewmodel.AdminViewModel
import com.example.myapplication.ui.viewmodel.GameViewModel
import com.example.myapplication.ui.viewmodel.GuessResult
import com.example.myapplication.ui.viewmodel.SessionViewModel

private val ink = Color(0xFF101820)
private val gold = Color(0xFFC8AA6E)
private val panel = Color(0xFF1E2833)

@Composable
fun AppNavigation(session: SessionViewModel, game: GameViewModel, admin: AdminViewModel) {
    val nav = rememberNavController()
    val role = session.role
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty()
    val mainRoutes = listOf("home", "game", "champions", "ranking")

    Scaffold(
        containerColor = ink,
        bottomBar = {
            if (route in mainRoutes) NavigationBar(containerColor = panel) {
                listOf("home" to "Início", "game" to "Jogar", "champions" to "Campeões", "ranking" to "Ranking").forEach { (destination, label) ->
                    NavigationBarItem(
                        selected = route == destination,
                        onClick = { nav.navigate(destination) { launchSingleTop = true } },
                        icon = { Text(label.take(1), color = gold) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "login", modifier = Modifier.padding(padding)) {
            composable("login") {
                LoginScreen(
                    onPlayer = { name -> session.loginPlayer(); game.setPlayer(name); nav.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onAdmin = { pin -> if (session.loginAdmin(pin)) { nav.navigate("home") { popUpTo("login") { inclusive = true } }; true } else false }
                )
            }
            composable("home") {
                HomeScreen(role, onGame = { nav.navigate("game") }, onHistory = { nav.navigate("history") }, onAdmin = { nav.navigate("admin") })
            }
            composable("game") { GameScreen(game) }
            composable("champions") { ChampionListScreen(admin) { nav.navigate("detail/${it.id}") } }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { back ->
                ChampionDetailScreen(admin.state.champions.firstOrNull { it.id == back.arguments?.getString("id") })
            }
            composable("ranking") { RankingScreen(admin) }
            composable("history") {
                HistoryScreen(
                    admin = admin,
                    canManage = role == "ADMIN",
                    onEdit = { nav.navigate("gameEdit/${it.id}") },
                    onDelete = admin::deleteGame
                )
            }
            composable("gameEdit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { back ->
                val gameId = back.arguments?.getLong("id")
                val record = admin.state.games.firstOrNull { it.id == gameId }
                if (role == "ADMIN" && record != null) {
                    GameRecordFormScreen(record, admin) { nav.popBackStack() }
                } else {
                    Page("Partida não encontrada", "Entre como administrador para editar o histórico.") { }
                }
            }
            composable("admin") {
                if (role == "ADMIN") AdminScreen(admin, onEdit = { nav.navigate("edit/${it ?: "new"}") }, onHistory = { nav.navigate("history") })
                else HomeScreen(role, onGame = { nav.navigate("game") }, onHistory = { nav.navigate("history") }, onAdmin = { nav.navigate("login") })
            }
            composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { back ->
                if (role == "ADMIN") {
                    val id = back.arguments?.getString("id")
                    ChampionFormScreen(admin.state.champions.firstOrNull { it.id == id }, admin) { nav.popBackStack() }
                } else {
                    LaunchedEffect(role) { nav.navigate("home") { popUpTo("edit/{id}") { inclusive = true } } }
                    Page("Acesso restrito", "Entre como administrador para continuar.") { }
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(onPlayer: (String) -> Unit, onAdmin: (String) -> Boolean) {
    var name by rememberSaveable { mutableStateOf("") }
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    Page("LOLDLE", "Entre para jogar") {
        Field("Seu nome", name) { name = it }
        Button(onClick = { onPlayer(name) }, modifier = Modifier.fillMaxWidth()) { Text("Entrar como jogador") }
        HorizontalDivider(color = gold)
        Field("PIN do administrador (demo: 1234)", pin, secret = true) { pin = it }
        Button(onClick = { if (!onAdmin(pin)) error = "PIN incorreto." }, modifier = Modifier.fillMaxWidth()) { Text("Entrar como administrador") }
        if (error.isNotEmpty()) Text(error, color = Color(0xFFFF8A80))
    }
}

@Composable
private fun HomeScreen(role: String, onGame: () -> Unit, onHistory: () -> Unit, onAdmin: () -> Unit) {
    Page("LOLDLE", if (role == "ADMIN") "Painel de administração" else "Descubra o campeão do dia") {
        Text("Compare classe, recurso e alcance dos seus palpites.", color = Color.LightGray)
        Button(onClick = onGame, modifier = Modifier.fillMaxWidth()) { Text("Começar partida") }
        Button(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("Ver histórico") }
        if (role == "ADMIN") Button(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) { Text("Gerenciar campeões") }
        Text("Os campeões são carregados da API Data Dragon e guardados no aparelho.", color = Color.LightGray)
    }
}

@Composable
private fun GameScreen(game: GameViewModel) {
    val state = game.state
    val suggestions = game.suggestions()
    Page("Partida", state.message) {
        Text("Tentativas: ${state.guesses.size}/6", color = gold, fontWeight = FontWeight.Bold)
        Field("Nome do campeão", state.guess, enabled = !state.finished, onChange = game::updateGuess)
        if (suggestions.isNotEmpty()) suggestions.forEach { champion ->
            Text(champion.name, color = Color.White, modifier = Modifier.fillMaxWidth().clickable { game.updateGuess(champion.name) }.padding(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = game::submitGuess, enabled = !state.finished) { Text("Tentar") }
            Button(onClick = game::restart) { Text("Nova partida") }
        }
        state.error?.let { Text(it, color = Color(0xFFFF8A80)) }
        state.guesses.reversed().forEach { GuessCard(it) }
    }
}

@Composable
private fun GuessCard(result: GuessResult) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = panel)) {
        Column(Modifier.padding(12.dp)) {
            Text(result.champion.name, color = gold, fontWeight = FontWeight.Bold)
            Hint("Classe", result.champion.role, result.sameRole)
            Hint("Recurso", result.champion.resource, result.sameResource)
            Hint("Alcance", result.champion.rangeType, result.sameRange)
        }
    }
}

@Composable
private fun Hint(label: String, value: String, matches: Boolean) {
    Text("$label: $value ${if (matches) "✓" else "✗"}", color = if (matches) Color(0xFF8BD3A8) else Color.LightGray)
}

@Composable
private fun ChampionListScreen(admin: AdminViewModel, onOpen: (ChampionEntity) -> Unit) {
    var filter by rememberSaveable { mutableStateOf("") }
    val shown = admin.filteredChampions(filter)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Campeões", color = gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Field("Buscar por nome ou classe", filter) { filter = it }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shown, key = { it.id }) { champion ->
                Card(Modifier.fillMaxWidth().clickable { onOpen(champion) }, colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(champion.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(champion.role, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChampionDetailScreen(champion: ChampionEntity?) {
    Page(champion?.name ?: "Campeão", champion?.title ?: "Não encontrado") {
        champion?.let {
            Text("Classe: ${it.role}", color = Color.White)
            Text("Recurso: ${it.resource}", color = Color.White)
            Text("Alcance: ${it.rangeType}", color = Color.White)
        }
    }
}

@Composable
private fun RankingScreen(admin: AdminViewModel) {
    val ranking = admin.ranking()
    Page("Ranking", "Vitórias registradas neste aparelho") {
        if (ranking.isEmpty()) Text("Ainda não há vitórias salvas.", color = Color.LightGray)
        ranking.forEachIndexed { index, (player, wins) ->
            Text("${index + 1}. $player — $wins vitória(s)", color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun HistoryScreen(
    admin: AdminViewModel,
    canManage: Boolean,
    onEdit: (GameEntity) -> Unit,
    onDelete: (GameEntity) -> Unit
) {
    val state = admin.state
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Histórico", color = gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        state.error?.let { Text(it, color = Color(0xFFFF8A80)) }
        state.notice?.let { Text(it, color = Color.LightGray) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.games, key = { it.id }) { game ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = panel)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${game.player}: ${game.champion}", color = Color.White)
                            Text("${if (game.won) "Vitória" else "Derrota"} • ${game.attempts} tentativa(s)", color = Color.LightGray)
                        }
                        if (canManage) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(onClick = { onEdit(game) }) { Text("Editar") }
                                Button(onClick = { onDelete(game) }) { Text("Excluir") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameRecordFormScreen(game: GameEntity, admin: AdminViewModel, onSaved: () -> Unit) {
    val state = admin.state
    var player by rememberSaveable(game.id) { mutableStateOf(game.player) }
    var champion by rememberSaveable(game.id) { mutableStateOf(game.champion) }
    var attempts by rememberSaveable(game.id) { mutableStateOf(game.attempts.toString()) }
    var won by rememberSaveable(game.id) { mutableStateOf(game.won) }
    var validationError by rememberSaveable(game.id) { mutableStateOf("") }

    LaunchedEffect(state.gameUpdateSucceeded) {
        if (state.gameUpdateSucceeded) {
            admin.clearGameUpdateSucceeded()
            onSaved()
        }
    }

    Page("Editar partida", "Corrija os dados do histórico") {
        state.error?.let { Text(it, color = Color(0xFFFF8A80)) }
        state.notice?.let { Text(it, color = Color.LightGray) }
        if (validationError.isNotBlank()) Text(validationError, color = Color(0xFFFF8A80))
        Field("Jogador", player) { player = it }
        Field("Campeão", champion) { champion = it }
        Field("Tentativas (1 a 6)", attempts) { attempts = it.filter(Char::isDigit).take(1) }
        Button(onClick = { won = !won }, modifier = Modifier.fillMaxWidth()) {
            Text(if (won) "Resultado: Vitória (toque para alterar)" else "Resultado: Derrota (toque para alterar)")
        }
        Button(onClick = {
            val attemptCount = attempts.toIntOrNull()
            if (player.isBlank() || champion.isBlank() || attemptCount == null || attemptCount !in 1..6) {
                validationError = "Informe jogador, campeão e de 1 a 6 tentativas."
            } else {
                validationError = ""
                admin.updateGame(game.copy(player = player.trim(), champion = champion.trim(), attempts = attemptCount, won = won))
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Salvar alterações") }
    }
}

@Composable
private fun AdminScreen(admin: AdminViewModel, onEdit: (String?) -> Unit, onHistory: () -> Unit) {
    val state = admin.state
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Administração", color = gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        state.error?.let { Text(it, color = Color(0xFFFF8A80)) }
        state.notice?.let { Text(it, color = Color.LightGray) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEdit(null) }) { Text("Novo") }
            Button(onClick = { admin.sync() }) { Text("Sincronizar API") }
            Button(onClick = onHistory) { Text("Histórico") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.champions, key = { it.id }) { champion ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = panel)) {
                    Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(champion.name, color = Color.White, modifier = Modifier.weight(1f).padding(top = 12.dp))
                        Button(onClick = { onEdit(champion.id) }) { Text("Editar") }
                        Button(onClick = { admin.delete(champion) }) { Text("Excluir") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChampionFormScreen(champion: ChampionEntity?, admin: AdminViewModel, onSaved: () -> Unit) {
    val state = admin.state
    var name by rememberSaveable(champion?.id) { mutableStateOf(champion?.name.orEmpty()) }
    var title by rememberSaveable(champion?.id) { mutableStateOf(champion?.title.orEmpty()) }
    var role by rememberSaveable(champion?.id) { mutableStateOf(champion?.role.orEmpty()) }
    var resource by rememberSaveable(champion?.id) { mutableStateOf(champion?.resource.orEmpty()) }
    var range by rememberSaveable(champion?.id) { mutableStateOf(champion?.rangeType ?: "À distância") }
    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            admin.clearSaveSucceeded()
            onSaved()
        }
    }
    Page(if (champion == null) "Novo campeão" else "Editar campeão") {
        state.error?.let { Text(it, color = Color(0xFFFF8A80)) }
        state.notice?.let { Text(it, color = if (state.saveSucceeded) Color(0xFF8BD3A8) else Color.LightGray) }
        Field("Nome", name) { name = it }
        Field("Título", title) { title = it }
        Field("Classe", role) { role = it }
        Field("Recurso", resource) { resource = it }
        Field("Alcance (À distância/Corpo a corpo)", range) { range = it }
        Button(onClick = { admin.save(champion?.id, name, title, role, resource, range) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar") }
    }
}

@Composable
private fun Page(title: String, subtitle: String = "", content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, color = gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) Text(subtitle, color = Color.LightGray)
        Spacer(Modifier.height(2.dp))
        content()
    }
}

@Composable
private fun Field(label: String, value: String, enabled: Boolean = true, secret: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}
