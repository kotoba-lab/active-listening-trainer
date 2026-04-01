package com.melof.activelisteningtrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.melof.activelisteningtrainer.data.ActiveSkill
import com.melof.activelisteningtrainer.data.AppDatabase
import com.melof.activelisteningtrainer.data.AttemptRecord
import com.melof.activelisteningtrainer.data.Difficulty
import com.melof.activelisteningtrainer.data.DependencyRepository
import com.melof.activelisteningtrainer.data.DependencyScenario
import com.melof.activelisteningtrainer.data.DependencyScoring
import com.melof.activelisteningtrainer.data.InvolvementResult
import com.melof.activelisteningtrainer.data.LlmScoringPromptBuilder
import com.melof.activelisteningtrainer.data.Scenario
import com.melof.activelisteningtrainer.data.ScenarioGenerator
import com.melof.activelisteningtrainer.data.ScenarioRepository
import com.melof.activelisteningtrainer.data.ScenarioType
import com.melof.activelisteningtrainer.data.ScoreResult
import com.melof.activelisteningtrainer.data.ScoringEngine
import androidx.lifecycle.viewModelScope
import com.melof.activelisteningtrainer.data.ApiKeyStore
import com.melof.activelisteningtrainer.data.ClaudeApiClient
import com.melof.activelisteningtrainer.data.LlmScoreResult
import com.melof.activelisteningtrainer.data.PracticeLogEntry
import com.melof.activelisteningtrainer.data.PracticeLogStore
import com.melof.activelisteningtrainer.data.UserDictionaryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TrainerViewModel(app: Application) : AndroidViewModel(app) {

    private val userDict    = UserDictionaryStore(app)
    private val logStore    = PracticeLogStore(app)
    private val apiKeyStore = ApiKeyStore(app)
    private val dao         = AppDatabase.getInstance(app).attemptDao()

    // ── 今日の開始タイムスタンプ ──────────────────────────────────────────────────
    private fun todayStart(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // ── 習得判定（3回連続クリア） ─────────────────────────────────────────────────
    val masteredScenarioIds: StateFlow<Set<String>> = dao.allAttemptedIds()
        .map { ids ->
            ids.filter { id ->
                val last3 = dao.lastThree(id)
                last3.size >= 3 && last3.all { it.passed }
            }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    // ── 今日のノルマ（1問以上回答） ───────────────────────────────────────────────
    val todayDone: StateFlow<Boolean> = dao.todayAttempts(todayStart())
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // ── 最終プレーからの経過日数 ──────────────────────────────────────────────────
    private val _daysSinceLastPlay = MutableStateFlow(0)
    val daysSinceLastPlay: StateFlow<Int> = _daysSinceLastPlay.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val last = dao.lastAttempt()
            _daysSinceLastPlay.value = if (last == null) 0
            else ((System.currentTimeMillis() - last.timestamp) / 86_400_000L).toInt()
        }
    }

    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    val currentScenario: StateFlow<Scenario?> = _currentScenario.asStateFlow()

    private val _scoreResult = MutableStateFlow<ScoreResult?>(null)
    val scoreResult: StateFlow<ScoreResult?> = _scoreResult.asStateFlow()

    private val _userPhrases = MutableStateFlow(userDict.loadAll())
    val userPhrases: StateFlow<Map<ActiveSkill, List<String>>> = _userPhrases.asStateFlow()

    private val _practiceLog = MutableStateFlow(logStore.loadAll())
    val practiceLog: StateFlow<List<PracticeLogEntry>> = _practiceLog.asStateFlow()

    private val _llmScore   = MutableStateFlow<LlmScoreResult?>(null)
    val llmScore: StateFlow<LlmScoreResult?> = _llmScore.asStateFlow()

    private val _llmLoading = MutableStateFlow(false)
    val llmLoading: StateFlow<Boolean> = _llmLoading.asStateFlow()

    private val _llmError   = MutableStateFlow<String?>(null)
    val llmError: StateFlow<String?> = _llmError.asStateFlow()

    /** selectScenario 時に確定したモード名（"CHOICE"|"GUIDED"|"FREE"） */
    private var _currentPlayMode: String = "FREE"

    val scenarios = ScenarioRepository.scenarios
    val dependencyScenarios = DependencyRepository.scenarios

    // ── 依存・不安型モード ────────────────────────────────────────────────────────
    private val _currentDepScenario = MutableStateFlow<DependencyScenario?>(null)
    val currentDepScenario: StateFlow<DependencyScenario?> = _currentDepScenario.asStateFlow()

    private val _involvementResult = MutableStateFlow<InvolvementResult?>(null)
    val involvementResult: StateFlow<InvolvementResult?> = _involvementResult.asStateFlow()

    fun selectDepScenario(scenario: DependencyScenario) {
        _currentDepScenario.value = scenario
        _involvementResult.value = null
    }

    fun submitDepResponse(text: String) {
        val scenario = _currentDepScenario.value ?: return
        val result = DependencyScoring.evaluate(text, scenario)
        _involvementResult.value = result

        val cleared = result.involvementLevel.name == "SAFE"
        logStore.saveEntry(
            PracticeLogEntry(
                timestampMs    = System.currentTimeMillis(),
                scenarioTitle  = scenario.title,
                playMode       = "DEP",
                totalScore     = result.rawScore,
                targetAchieved = 0,
                targetTotal    = 0,
                cleared        = cleared,
            )
        )
        _practiceLog.value = logStore.loadAll()

        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(AttemptRecord(scenarioId = "dep_${scenario.id}", passed = cleared))
            _daysSinceLastPlay.value = 0
        }
    }

    fun retryDep() {
        _involvementResult.value = null
    }

    /** 難易度フィルター付きでランダムにDEPシナリオを1問選んで返す（選択は呼び出し元が selectDepScenario で行う） */
    fun randomDepScenario(difficulty: Difficulty? = null): DependencyScenario? {
        val pool = if (difficulty != null) {
            dependencyScenarios.filter { it.difficulty == difficulty }
        } else {
            dependencyScenarios
        }
        return pool.randomOrNull()
    }

    /**
     * 構造化パラメータで既存ALシナリオからマッチするものを選んで返す。
     * ScenarioGenerator.randomParams() と組み合わせて使う。
     */
    fun pickScenarioByParams(
        difficulty: Difficulty,
        type: ScenarioType? = null,
    ): Scenario? {
        val params = ScenarioGenerator.randomParams(difficulty, type)
        return ScenarioGenerator.pickScenario(params, scenarios)
    }

    /**
     * 統合LLM採点プロンプトを生成する（Scenario / DependencyScenario 両対応）。
     */
    fun buildLlmPrompt(input: String): String? {
        val sc = _currentScenario.value
        val dep = _currentDepScenario.value
        return when {
            dep != null -> LlmScoringPromptBuilder.buildForDependencyScenario(input, dep)
            sc  != null -> LlmScoringPromptBuilder.buildForScenario(input, sc)
            else        -> null
        }
    }

    fun selectScenario(scenario: Scenario, playMode: String = "FREE") {
        _currentScenario.value = scenario
        _currentPlayMode = playMode
        _scoreResult.value = null
        _llmScore.value = null
        _llmError.value = null
    }

    fun submitResponse(text: String) {
        val scenario = _currentScenario.value ?: return
        val result = ScoringEngine.evaluate(text, scenario, userDict.loadAll())
        _scoreResult.value = result

        val targetSlots = scenario.freeResponseScoring.targetSlots
        val achieved = result.slotResults.count { it.skill in targetSlots && it.achieved }
        val cleared  = achieved == targetSlots.size && result.penaltyResults.none { it.triggered }

        logStore.saveEntry(
            PracticeLogEntry(
                timestampMs    = System.currentTimeMillis(),
                scenarioTitle  = scenario.title,
                playMode       = _currentPlayMode,
                totalScore     = result.totalScore,
                targetAchieved = achieved,
                targetTotal    = targetSlots.size,
                cleared        = cleared,
            )
        )
        _practiceLog.value = logStore.loadAll()

        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(AttemptRecord(scenarioId = scenario.id, passed = cleared))
            _daysSinceLastPlay.value = 0
        }
    }

    fun retry() {
        _scoreResult.value = null
        _llmScore.value = null
        _llmError.value = null
    }

    // ── API キー管理 ─────────────────────────────────────────────────────────────

    fun saveApiKey(key: String) = apiKeyStore.save(key)
    fun loadApiKey(): String = apiKeyStore.load()
    fun hasApiKey(): Boolean = apiKeyStore.hasKey()

    // ── LLM採点 ──────────────────────────────────────────────────────────────────

    fun requestLlmScore() {
        val apiKey = apiKeyStore.load()
        if (apiKey.isBlank()) {
            _llmError.value = "APIキーが設定されていません"
            return
        }
        val input = _scoreResult.value?.input
            ?: _involvementResult.value?.input
            ?: return
        val prompt = buildLlmPrompt(input) ?: return

        _llmScore.value = null
        _llmError.value = null
        _llmLoading.value = true

        viewModelScope.launch {
            ClaudeApiClient.score(prompt, apiKey)
                .onSuccess { result ->
                    _llmScore.value = result
                    _llmLoading.value = false
                }
                .onFailure { e ->
                    _llmError.value = e.message ?: "エラーが発生しました"
                    _llmLoading.value = false
                }
        }
    }

    fun registerPhrase(skill: ActiveSkill, phrase: String) {
        userDict.addPhrase(skill, phrase)
        _userPhrases.value = userDict.loadAll()
    }

    fun removePhrase(skill: ActiveSkill, phrase: String) {
        userDict.removePhrase(skill, phrase)
        _userPhrases.value = userDict.loadAll()
    }
}
