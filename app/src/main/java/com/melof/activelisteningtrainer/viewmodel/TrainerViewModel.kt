package com.melof.activelisteningtrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.melof.activelisteningtrainer.data.ActiveSkill
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
import com.melof.activelisteningtrainer.data.UserDictionaryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrainerViewModel(app: Application) : AndroidViewModel(app) {

    private val userDict = UserDictionaryStore(app)

    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    val currentScenario: StateFlow<Scenario?> = _currentScenario.asStateFlow()

    private val _scoreResult = MutableStateFlow<ScoreResult?>(null)
    val scoreResult: StateFlow<ScoreResult?> = _scoreResult.asStateFlow()

    private val _userPhrases = MutableStateFlow(userDict.loadAll())
    val userPhrases: StateFlow<Map<ActiveSkill, List<String>>> = _userPhrases.asStateFlow()

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
        _involvementResult.value = DependencyScoring.evaluate(text, scenario)
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

    fun selectScenario(scenario: Scenario) {
        _currentScenario.value = scenario
        _scoreResult.value = null
    }

    fun submitResponse(text: String) {
        val scenario = _currentScenario.value ?: return
        _scoreResult.value = ScoringEngine.evaluate(text, scenario, userDict.loadAll())
    }

    fun retry() {
        _scoreResult.value = null
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
