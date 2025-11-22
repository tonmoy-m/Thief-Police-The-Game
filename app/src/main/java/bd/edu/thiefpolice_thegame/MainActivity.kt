package bd.edu.thiefpolice_thegame

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bd.edu.thiefpolice_thegame.ui.theme.ThiefPoliceTheGameTheme
import kotlinx.coroutines.delay

// --- Enums and Data Classes at Top Level ---
enum class Role { Babu, Police, Thief, Dacoit }
enum class RoundPhase { TokenSelection, RoleReveal, BabuInstruction, PoliceGuess, RevealAndScore, RoundSummary, GameOver }
data class Player(val name: String, val role: Role? = null, val isHuman: Boolean = false)
data class RoundResult(
    val round: Int,
    val playerRoles: List<Role>,
    val playerNames: List<String>,
    val roundScores: List<Int>,
    val guessIdx: Int?,
    val instruction: Role?
)

// --- Main Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThiefPoliceTheGameTheme {
                AppNavigation()
            }
        }
    }
}

// --- Navigation ---
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen {
                navController.navigate("mainMenu") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("mainMenu") {
            MainMenuScreen(
                onStartGame = { navController.navigate("game") },
                onAbout = { navController.navigate("about") },
                onHowToPlay = { navController.navigate("howToPlay") }
            )
        }
        composable("game") {
            MultiRoundGameScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable("howToPlay") {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }
    }
}

// --- Splash Screen ---
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF42A5F5), // blue
                        Color(0xFF66BB6A), // green
                        Color(0xFFFFCA28)  // yellow
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Thief Police The Game",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// --- Main Menu with Quit Logic ---
@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onAbout: () -> Unit,
    onHowToPlay: () -> Unit
) {
    val activity = LocalActivity.current
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept Back press
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFE57373), // red
                        Color(0xFFBA68C8), // purple
                        Color(0xFF64B5F6)  // blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Thief Police The Game",
                fontSize = 24.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onStartGame,
                modifier = Modifier.width(200.dp)
            ) {
                Text("Start Game", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onHowToPlay,
                modifier = Modifier.width(200.dp)
            ) {
                Text("How to Play", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAbout,
                modifier = Modifier.width(200.dp)
            ) {
                Text("About", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showExitDialog = true },
                modifier = Modifier.width(200.dp)
            ) {
                Text("Exit", fontSize = 16.sp)
            }
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Quit Game?") },
            text = { Text("Are you sure you want to quit the game?") },
            confirmButton = {
                TextButton(
                    onClick = { activity?.finishAffinity() }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

// --- Multi Round Game Wrapper ---
@Composable
fun MultiRoundGameScreen(onBack: () -> Unit) {
    val totalRounds = 10
    val playerNames = listOf("You", "CPU 1", "CPU 2", "CPU 3")
    var currentRound by remember { mutableStateOf(1) }
    var scores by remember { mutableStateOf(IntArray(4) { 0 }) }
    var roundHistory by remember { mutableStateOf(mutableListOf<RoundResult>()) }
    var phase by remember { mutableStateOf("play") }
    var showRoundDialog by remember { mutableStateOf(true) }
    var showBackDialog by remember { mutableStateOf(false) }

    // Intercept Back press during gameplay
    BackHandler(enabled = true) {
        showBackDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Box(modifier = Modifier.fillMaxSize()) {
            if (showRoundDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Round $currentRound / $totalRounds") },
                    text = { Text("Get ready!") },
                    confirmButton = {
                        Button(onClick = { showRoundDialog = false }) { Text("Start") }
                    }
                )
            } else if (phase == "play") {
                GameRoundScreenMulti(
                    playerNames = playerNames,
                    onBack = { showBackDialog = true },
                    onRoundEnd = { result ->
                        // Update scores
                        for (i in 0..3) scores[i] += result.roundScores[i]
                        roundHistory.add(result)
                        phase = "summary"
                    },
                    roundNo = currentRound
                )
            } else if (phase == "summary") {
                val last = roundHistory.lastOrNull()
                val isGameOver = currentRound == totalRounds
                if (last != null) {
                    RoundSummaryScreen(
                        roundHistory = roundHistory,
                        playerNames = playerNames,
                        isGameEnd = isGameOver,
                        onContinue = {
                            if (isGameOver) {
                                phase = "gameover"
                            } else {
                                currentRound++
                                phase = "play"
                                showRoundDialog = true
                            }
                        },
                        onBack = { showBackDialog = true }
                    )
                }
            } else if (phase == "gameover") {
                val maxScore = scores.maxOrNull() ?: 0
                val winners = playerNames.filterIndexed { i, _ -> scores[i] == maxScore }

                // Auto-close after 5 seconds
                LaunchedEffect(Unit) {
                    delay(5000)
                    onBack()
                }

// Final Game Over screen centered
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF9C4), // light yellow
                                    Color(0xFFC8E6C9), // light green
                                    Color(0xFFB2DFDB)  // light teal
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Game Over! Final Scores", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(20.dp))
                        for (i in 0..3) {
                            Text("${playerNames[i]}: ${scores[i]}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Winner: ${winners.joinToString(", ")}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                    }
                }
            }
        }

        // Home icon - positioned where the content text starts vertically (only for non-summary screens)
        if (phase != "summary") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { showBackDialog = true },
                    modifier = Modifier
                        .padding(top = 200.dp, end = 16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Back to Menu",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    // Back to menu confirmation dialog
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Back to Menu?") },
            text = { Text("Are you sure you want to return to the main menu? Current game progress will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackDialog = false
                        onBack()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBackDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

// --- Game Screen Entry Point (old, now routed to MultiRoundGameScreen) ---
@Composable
fun GameScreen(onBack: () -> Unit) {
    MultiRoundGameScreen(onBack = onBack)
}

// --- GameRoundScreen with multi-round data reporting ---
@Composable
fun GameRoundScreenMulti(
    playerNames: List<String>,
    onBack: () -> Unit,
    onRoundEnd: (RoundResult) -> Unit,
    roundNo: Int,
) {
    var playerToToken by remember { mutableStateOf<IntArray?>(null) }
    var phase by remember { mutableStateOf(RoundPhase.TokenSelection) }
    var selectedTokenIndex by remember { mutableStateOf<Int?>(null) }
    var assignedRoles by remember { mutableStateOf<List<Role>?>(null) }
    var roleHidden by remember { mutableStateOf(false) }
    var babuInstruction by remember { mutableStateOf<Role?>(null) }
    var policeGuessIndex by remember { mutableStateOf<Int?>(null) }

    // Assign roles randomly, ensuring each role is used exactly once
    fun assignRoles(humanIndex: Int) {
        val roles = Role.values().toMutableList().shuffled()
        val assigned = MutableList<Role?>(4) { null }
        assigned[humanIndex] = roles[0]
        val cpuIndexes = (0..3).filter { it != humanIndex }
        for (i in cpuIndexes.indices) {
            assigned[cpuIndexes[i]] = roles[i + 1]
        }
        assignedRoles = assigned.map { it!! }
        roleHidden = false
    }

    // After selecting a token, transition to RoleReveal
    LaunchedEffect(selectedTokenIndex, assignedRoles, playerToToken) {
        if (phase == RoundPhase.TokenSelection &&
            selectedTokenIndex != null && assignedRoles != null && playerToToken != null) {
            phase = RoundPhase.RoleReveal
        }
    }

    // Role reveal logic (Thief/Dacoit hidden after 2s)
    LaunchedEffect(phase, assignedRoles, selectedTokenIndex) {
        if (phase == RoundPhase.RoleReveal &&
            assignedRoles != null && selectedTokenIndex != null) {
            val yourRole = assignedRoles!![selectedTokenIndex!!]
            if (yourRole == Role.Thief || yourRole == Role.Dacoit) {
                delay(2000)
                roleHidden = true
            }
        }
    }

    // Handle CPU Babu instruction
    LaunchedEffect(phase, assignedRoles, selectedTokenIndex) {
        if (phase == RoundPhase.BabuInstruction && assignedRoles != null && selectedTokenIndex != null) {
            val pt = playerToToken
            if (pt != null) {
                val babuToken = assignedRoles!!.indexOf(Role.Babu)
                val babuPlayerIdx = pt.indexOf(babuToken)
                val isHumanBabu = babuPlayerIdx == 0
                if (!isHumanBabu && babuInstruction == null) {
                    babuInstruction = if ((0..1).random() == 0) Role.Thief else Role.Dacoit
                }
            }
        }
    }

    // Handle CPU Police guess
    LaunchedEffect(phase, assignedRoles, selectedTokenIndex, babuInstruction) {
        if (
            phase == RoundPhase.PoliceGuess &&
            assignedRoles != null && selectedTokenIndex != null && babuInstruction != null
        ) {
            val pt = playerToToken
            if (pt != null) {
                val policeToken = assignedRoles!!.indexOf(Role.Police)
                val policePlayerIdx = pt.indexOf(policeToken)
                val isHumanPolice = policePlayerIdx == 0
                if (!isHumanPolice && policeGuessIndex == null) {
                    val candidates = (0..3).filter {
                        val tokenIdx = pt[it]
                        assignedRoles!![tokenIdx] != Role.Babu && assignedRoles!![tokenIdx] != Role.Police
                    }
                    policeGuessIndex = candidates.random()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD), // light blue
                        Color(0xFFF1F8E9), // light green
                        Color(0xFFFFF9C4)  // light yellow
                    )
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (phase) {
            RoundPhase.TokenSelection -> {
                Text("Round $roundNo: Select your token", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(4) { index ->
                        Button(
                            onClick = {
                                selectedTokenIndex = index
                                assignRoles(index)
                                // Assign tokens to CPUs randomly for this round, persistently
                                val cpuTokens = (0..3).filter { it != index }.shuffled()
                                playerToToken = IntArray(4).apply {
                                    this[0] = index
                                    for (cpuIdx in 1..3) this[cpuIdx] = cpuTokens[cpuIdx - 1]
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .padding(8.dp),
                            enabled = selectedTokenIndex == null
                        ) {
                            Text(if (selectedTokenIndex == index) "✓" else "?", fontSize = 28.sp)
                        }
                    }
                }
            }
            RoundPhase.RoleReveal -> {
                val pt = playerToToken!!
                val yourRole = assignedRoles!![selectedTokenIndex!!]
                val policeToken = assignedRoles!!.indexOf(Role.Police)
                val babuToken = assignedRoles!!.indexOf(Role.Babu)

                var localRoleHidden by remember { mutableStateOf(false) }
                var allowContinue by remember { mutableStateOf(false) }

                LaunchedEffect(phase, assignedRoles, selectedTokenIndex) {
                    allowContinue = false
                    localRoleHidden = false
                    if (yourRole == Role.Thief || yourRole == Role.Dacoit) {
                        delay(2000)
                        localRoleHidden = true
                        allowContinue = true
                    } else {
                        allowContinue = true
                    }
                }

                fun getRoleDisplay(playerIdx: Int): Triple<String, Color, FontWeight> {
                    val tokenIdx = pt[playerIdx]
                    val isYou = playerIdx == 0
                    val role = assignedRoles!![tokenIdx]
                    return when {
                        tokenIdx == policeToken -> Triple("Police", Color(0xFF1976D2), FontWeight.Bold)
                        tokenIdx == babuToken -> Triple("Babu", Color(0xFF388E3C), FontWeight.Bold)
                        isYou && (yourRole == Role.Thief || yourRole == Role.Dacoit) && !localRoleHidden ->
                            Triple(yourRole.name, Color.Black, FontWeight.Bold)
                        isYou && (yourRole == Role.Thief || yourRole == Role.Dacoit) && localRoleHidden ->
                            Triple("[HIDDEN]", Color.Black, FontWeight.Bold)
                        isYou && (yourRole == Role.Babu || yourRole == Role.Police) ->
                            Triple(yourRole.name, Color.Black, FontWeight.Bold)
                        else -> Triple("?", Color.Gray, FontWeight.Normal)
                    }
                }

                Text("Token & Role Reveal", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    for (playerIdx in 0..3) {
                        val tokenIdx = pt[playerIdx]
                        val isYou = playerIdx == 0
                        val name = if (isYou) "You" else "CPU $playerIdx"
                        val (roleString, color, weight) = getRoleDisplay(playerIdx)
                        Text(
                            "$name picked token ${tokenIdx + 1} - $roleString",
                            fontSize = 16.sp,
                            color = color,
                            fontWeight = weight
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))
                if (allowContinue) {
                    Button(onClick = { phase = RoundPhase.BabuInstruction }) {
                        Text("Continue")
                    }
                } else {
                    Text("Please wait...", color = Color.Gray)
                }
            }
            RoundPhase.BabuInstruction -> {
                val pt = playerToToken!!
                // --- Police/Babu always visible ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (playerIdx in 0..3) {
                        val tokenIdx = pt[playerIdx]
                        val isPolice = assignedRoles!![tokenIdx] == Role.Police
                        val isBabu = assignedRoles!![tokenIdx] == Role.Babu
                        val name = if (playerIdx == 0) "You" else "CPU $playerIdx"
                        if (isPolice || isBabu) {
                            val color = if (isPolice) Color(0xFF1976D2) else Color(0xFF388E3C)
                            val label = if (isPolice) "Police" else "Babu"
                            Box(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "$name: $label",
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
                val babuToken = assignedRoles!!.indexOf(Role.Babu)
                val babuPlayerIdx = pt.indexOf(babuToken)
                val isHumanBabu = babuPlayerIdx == 0
                if (isHumanBabu) {
                    Text("You are Babu!\nWhich role should Police try to catch?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    Row {
                        Button(
                            onClick = { babuInstruction = Role.Thief },
                            modifier = Modifier.padding(8.dp),
                            enabled = babuInstruction == null
                        ) { Text("Thief") }
                        Button(
                            onClick = { babuInstruction = Role.Dacoit },
                            modifier = Modifier.padding(8.dp),
                            enabled = babuInstruction == null
                        ) { Text("Dacoit") }
                    }
                    if (babuInstruction != null) {
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { phase = RoundPhase.PoliceGuess }) {
                            Text("Continue")
                        }
                    }
                } else {
                    Text("Babu (${playerNames[babuPlayerIdx]}) has instructed Police to catch:", fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("${babuInstruction?.name ?: ""}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { phase = RoundPhase.PoliceGuess }) {
                        Text("Continue")
                    }
                }
            }
            RoundPhase.PoliceGuess -> {
                val pt = playerToToken!!
                val policeToken = assignedRoles!!.indexOf(Role.Police)
                val policePlayerIdx = pt.indexOf(policeToken)
                val isHumanPolice = policePlayerIdx == 0
                val instruction = babuInstruction ?: Role.Thief // fallback

                // --- Police/Babu always visible ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (playerIdx in 0..3) {
                        val tokenIdx = pt[playerIdx]
                        val isPolice = assignedRoles!![tokenIdx] == Role.Police
                        val isBabu = assignedRoles!![tokenIdx] == Role.Babu
                        val name = if (playerIdx == 0) "You" else "CPU $playerIdx"
                        if (isPolice || isBabu) {
                            val color = if (isPolice) Color(0xFF1976D2) else Color(0xFF388E3C)
                            val label = if (isPolice) "Police" else "Babu"
                            Box(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "$name: $label",
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Valid candidates: not Police, not Babu
                val candidates = (0..3).filter {
                    val tokenIdx = pt[it]
                    assignedRoles!![tokenIdx] != Role.Babu && assignedRoles!![tokenIdx] != Role.Police
                }

                if (isHumanPolice) {
                    Text("You are Police!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Babu instructed you to catch: ${instruction.name}", fontSize = 18.sp)
                    Spacer(Modifier.height(24.dp))
                    Text("Who do you think is the ${instruction.name}?", fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    candidates.forEach { idx ->
                        Button(
                            onClick = { policeGuessIndex = idx },
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(0.6f),
                            enabled = policeGuessIndex == null
                        ) {
                            Text(playerNames[idx])
                        }
                    }
                    if (policeGuessIndex != null) {
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { phase = RoundPhase.RevealAndScore }) {
                            Text("Continue")
                        }
                    }
                } else {
                    LaunchedEffect(policeGuessIndex) {
                        if (policeGuessIndex == null && candidates.isNotEmpty()) {
                            policeGuessIndex = candidates.random()
                        }
                    }
                    val guessName = policeGuessIndex?.let { playerNames[it] } ?: ""
                    Text("Police (${playerNames[policePlayerIdx]}) is deciding...", fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    if (policeGuessIndex != null) {
                        Text("Police chooses: $guessName", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { phase = RoundPhase.RevealAndScore }) {
                            Text("Continue")
                        }
                    } else {
                        Text("...", fontSize = 28.sp)
                    }
                }
            }
            RoundPhase.RevealAndScore -> {
                val pt = playerToToken!!
                val roles = List(4) { playerIdx -> assignedRoles!![pt[playerIdx]] }
                val policeIdx = roles.indexOf(Role.Police)
                val babuIdx = roles.indexOf(Role.Babu)
                val thiefIdx = roles.indexOf(Role.Thief)
                val dacoitIdx = roles.indexOf(Role.Dacoit)
                val instruction = babuInstruction
                val guessIdx = policeGuessIndex

// Master (Babu) always +100
                val roundScores = IntArray(4) { 0 }
                roundScores[babuIdx] = 100

                if (guessIdx != null && instruction != null) {
                    if (roles[guessIdx] == instruction) {
                        // Police correct
                        roundScores[policeIdx] = 80
                        // Caught player gets 0
                        if (instruction == Role.Thief) {
                            roundScores[thiefIdx] = 0
                            roundScores[dacoitIdx] = 60
                        } else {
                            roundScores[dacoitIdx] = 0
                            roundScores[thiefIdx] = 40
                        }
                    } else {
                        // Police wrong
                        roundScores[policeIdx] = 0
                        roundScores[thiefIdx] = 40
                        roundScores[dacoitIdx] = 60
                    }
                }

                Column(Modifier.padding(24.dp)) {
                    Text("All Roles Revealed", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(18.dp))
                    for (playerIdx in 0..3) {
                        val role = roles[playerIdx]
                        val name = playerNames[playerIdx]
                        val color = when (role) {
                            Role.Police -> Color(0xFF1976D2)
                            Role.Babu -> Color(0xFF388E3C)
                            Role.Thief -> Color(0xFFD32F2F)
                            Role.Dacoit -> Color(0xFF6D4C41)
                        }
                        Text("$name: ${role.name}", color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    if (guessIdx != null && instruction != null) {
                        val guessRole = roles[guessIdx]
                        val outcome = if (guessRole == instruction) {
                            "Police caught the correct ${instruction.name}! Police & Babu win."
                        } else {
                            "Police failed. Thief & Dacoit win."
                        }
                        Text(outcome, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val result = RoundResult(
                                round = roundNo,
                                playerRoles = roles,
                                playerNames = playerNames,
                                roundScores = roundScores.toList(),
                                guessIdx = guessIdx,
                                instruction = instruction
                            )
                            onRoundEnd(result)
                        },
                        modifier = Modifier.padding(24.dp)
                    ) { Text("Continue") }
                }
            }
            else -> {}
        }
    }
}

// --- Round Summary Screen ---
@Composable
fun RoundSummaryScreen(
    roundHistory: List<RoundResult>,
    playerNames: List<String>,
    isGameEnd: Boolean,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val numberOfRounds = roundHistory.size
    val roundsToShow = 10 // Always show 10 rows + 1 total row
    // Build per-player scores for each round (fill with 0 if fewer than 10 rounds yet)
    val allScores = List(roundsToShow) { roundIdx ->
        if (roundIdx < roundHistory.size)
            roundHistory[roundIdx].roundScores
        else
            listOf(0, 0, 0, 0)
    }
    // Compute totals
    val totals = IntArray(4) { idx -> allScores.sumOf { it[idx] } }
    val maxScore = totals.maxOrNull() ?: 0
    val winners = playerNames.filterIndexed { i, _ -> totals[i] == maxScore }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1BEE7), // light purple
                        Color(0xFFBBDEFB), // light blue
                        Color(0xFFC5E1A5)  // light green
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Home icon positioned at top-right, aligned with "Game Progress" title
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 130.dp, end = 0.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Back to Menu",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(32.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isGameEnd) "Game Over! Final Scores" else "Game Progress",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))
            // Table
            Column(
                modifier = Modifier.background(Color.White, shape = MaterialTheme.shapes.medium)
            ) {
                // Header
                Row(
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Round", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    playerNames.forEach {
                        Text(it, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
                // 10 round rows
                for (roundIdx in 0 until roundsToShow) {
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("${roundIdx + 1}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        allScores[roundIdx].forEach { score ->
                            Text("$score", modifier = Modifier.weight(1f), color = Color.DarkGray, textAlign = TextAlign.Center)
                        }
                    }
                }
                // Total row
                Row(
                    modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    totals.forEach { t ->
                        Text("$t", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "Winner: ${winners.joinToString(", ")}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onContinue) {
                Text(if (isGameEnd) "Continue" else "Next Round")
            }
        }
    }
}


// --- How to Play Screen ---
@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE0B2), // light orange
                        Color(0xFFFFF9C4), // light yellow
                        Color(0xFFC8E6C9)  // light green
                    )
                )
            )
    ) {
        // Home icon at top-right
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Back to Menu",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(32.dp)
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(top = 40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "How to Play",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. The Objective
            Text(
                "1. The Objective",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Play through 10 rounds. The player with the highest total score at the end wins.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. The Roles
            Text(
                "2. The Roles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Babu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text("👑", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Babu (Leader)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                    Text(
                        "Orders the Police to catch a criminal.\nScore: Always +100.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Police
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text("👮", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Police (Enforcer)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        "Must catch the specific criminal (Thief or Dacoit) ordered by the Babu.\nScore: +80 if correct, 0 if wrong.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dacoit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text("💰", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Dacoit (Criminal)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D4C41)
                    )
                    Text(
                        "Hides from the Police.\nScore: +60 if not caught.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thief
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text("👺", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Thief (Criminal)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        "Hides from the Police.\nScore: +40 if not caught.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. How a Round Works
            Text(
                "3. How a Round Works",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "• Pick a Token: Select a card to get your role.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• Memorize Quickly: If you are the Thief or Dacoit, your role will disappear after 2 seconds! Remember it.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• The Order: The Babu will tell the Police who to catch (e.g., \"Catch the Thief\").",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• The Guess: The Police must guess which player is holding that role.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "• Scoring: Points are awarded based on whether the Police was right or wrong.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// --- About Screen ---
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8BBD0), // light pink
                        Color(0xFFE1BEE7), // light purple
                        Color(0xFFB2DFDB)  // light teal
                    )
                )
            )
    ) {
        // Home icon at top-right, aligned with content
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 200.dp, end = 16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Back to Menu",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(32.dp)
            )
        }

        // Content centered
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("About Thief Police The Game", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Relive the nostalgia of 'Chor-Police'",
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "without the paper chits! Jump into 10 rounds",
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "of strategic deduction against smart CPU opponents.",
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Made with ❤️ by Miskat",
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Telegram Icon
            IconButton(
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://t.me/tonmoy_m")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.stat_notify_chat),
                    contentDescription = "Telegram",
                    tint = Color(0xFF0088cc),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}