package com.melof.activelisteningtrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.melof.activelisteningtrainer.data.ActiveSkill
import com.melof.activelisteningtrainer.data.Scenario
import com.melof.activelisteningtrainer.data.ScenarioRepository
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
