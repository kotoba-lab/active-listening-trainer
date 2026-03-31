package com.melof.activelisteningtrainer.data

object DependencyScoring {

    /**
     * 依存・不安型シナリオを採点して InvolvementResult を返す。
     * - ngKeywords に含まれるキーワードが input に含まれていたら NGパターンをトリガー
     * - okKeywords に含まれるキーワードが input に含まれていたら OKパターンを達成
     */
    fun evaluate(input: String, scenario: DependencyScenario): InvolvementResult {
        val lower = input

        val ngResults = scenario.ngPatterns.map { pattern ->
            val keywords = scenario.ngKeywords[pattern] ?: emptyList()
            val triggered = keywords.filter { kw -> lower.contains(kw) }
            NGPatternResult(
                pattern      = pattern,
                triggered    = triggered.isNotEmpty(),
                triggeredWords = triggered,
            )
        }

        val okResults = scenario.okPatterns.map { pattern ->
            val keywords = scenario.okKeywords[pattern] ?: emptyList()
            val matched = keywords.filter { kw -> lower.contains(kw) }
            OKPatternResult(
                pattern      = pattern,
                achieved     = matched.isNotEmpty(),
                matchedWords = matched,
            )
        }

        return InvolvementResult(
            input     = input,
            scenario  = scenario,
            ngResults = ngResults,
            okResults = okResults,
        )
    }
}
