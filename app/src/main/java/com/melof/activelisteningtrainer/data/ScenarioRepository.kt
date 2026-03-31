package com.melof.activelisteningtrainer.data

object ScenarioRepository {

    val scenarios: List<Scenario> = listOf(

        // ════════════════════════════════════════════════════════════════════
        // 1. 愚痴を聞く
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-001",
            category = ScenarioCategory.VENTING,
            difficulty = Difficulty.BEGINNER,
            title = "あの人だけ雑なんだよね",
            situation = "同僚が職場の特定の人の対応への不満をこぼしている。",
            utterance = "またあの人、こっちにだけ雑なんだよね",
            hint = "感情反映 → 促し の2文で。解決策はまだ出さない。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "それは嫌な感じだね。もう少し話してみて？",
                    isCorrect = true,
                    badType = null,
                    explanation = "感情反映と促し。相手が話を続けやすい返しです。"
                ),
                ChoiceOption(
                    text = "直接本人に言ってみたら？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "助言は相手が求めていない段階では逆効果です。"
                ),
                ChoiceOption(
                    text = "でも相手にも事情があるんじゃない？",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "気持ちを受け止める前に評価を入れると、相手は「わかってもらえない」と感じます。"
                ),
                ChoiceOption(
                    text = "具体的にどんなことされたの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は早すぎます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.ADVICE, PenaltyType.JUDGMENT, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("嫌な感じ", "嫌だね", "いらっとする", "雑に感じた", "話してみて", "続けて"),
                negativeKeywords = listOf("言ってみれば", "直接言えば", "気にしすぎ", "事情があるんじゃ"),
                customFeedback = mapOf(
                    "EMOTIONAL_REFLECTION" to "「嫌な感じがする」「いらっとする」など、相手の感情を言葉にして返せています。",
                    "ADVICE" to "まず気持ちを受け止めてから。解決策はその後で十分です。"
                )
            ),
            sampleResponse = "それは嫌な感じだね。もう少し話してみて？"
        ),

        Scenario(
            id = "AL-002",
            category = ScenarioCategory.VENTING,
            difficulty = Difficulty.INTERMEDIATE,
            title = "雑に扱われてもいい人みたい",
            situation = "友人が人間関係の不満を話している。表面は愚痴だが、寂しさも滲んでいる。",
            utterance = "なんか最近、私って雑に扱われてもいい人みたいになってる気がする",
            hint = "不満の奥にある「軽く見られている寂しさ」も拾えると◎",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "軽く見られてる感じが積み重なって、しんどくなってるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "表面の不満と奥にある消耗感を両方拾えています。"
                ),
                ChoiceOption(
                    text = "それってあなたの思い込みじゃないかな",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "評価・判断は相手の気持ちを否定することになります。"
                ),
                ChoiceOption(
                    text = "私も前にそういうこと感じたことある",
                    isCorrect = false,
                    badType = "bad_self_talk",
                    explanation = "自分語りをすると会話の焦点が自分に移ってしまいます。"
                ),
                ChoiceOption(
                    text = "具体的に誰からそんな扱いを受けたの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は相手の気持ちをスルーしてしまいます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.SUMMARY_LIKE),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.SELF_TALK, PenaltyType.ADVICE),
                positiveKeywords = listOf("積み重なる", "しんどい", "軽く扱われた", "消耗", "寂しい", "疲れた"),
                negativeKeywords = listOf("思い込み", "距離を置けば", "私も", "自分も"),
                customFeedback = mapOf(
                    "SUMMARY_LIKE" to "「積み重なってしんどい」など、状況を整理して返せると◎",
                    "SELF_TALK" to "「私も〜」と始めると焦点が自分に移ります。相手の言葉を中心に返しましょう。"
                )
            ),
            sampleResponse = "軽く見られてる感じが続いて、疲れてきてるんだね"
        ),

        Scenario(
            id = "AL-003",
            category = ScenarioCategory.VENTING,
            difficulty = Difficulty.TRAP,
            title = "みんな押しつけてるだけ（崩しトラップ）",
            situation = "相手の言い分に一理あるかもしれないが、被害者意識が強め。正したくなる罠。",
            utterance = "結局みんな、自分が楽したいからこっちに押しつけてるだけなんだよ",
            hint = "相手の認知の正しさを判断する前に、感情の強さを受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "かなりしんどい状況が続いてるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "感情の強さを受け止める返しです。認知の正誤には触れていません。"
                ),
                ChoiceOption(
                    text = "でもみんながそう思ってるとは限らないよ",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "正論は相手の感情を否定する形になります。"
                ),
                ChoiceOption(
                    text = "ほんとひどいね、そんな職場やめちゃえば",
                    isCorrect = false,
                    badType = "bad_join_attack",
                    explanation = "一緒に攻撃することで感情的な連帯は生まれますが、相手の依存を強化します。"
                ),
                ChoiceOption(
                    text = "まあ、考えすぎじゃない？",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "矮小化は相手の感情を否定します。強い反発を招きます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.JOIN_ATTACK, PenaltyType.MINIMIZATION),
                positiveKeywords = listOf("かなり溜まってる", "しんどい", "押しつけられた感じ", "疲れた"),
                negativeKeywords = listOf("考えすぎ", "そう思ってるとは限らない", "ひどいね", "やめちゃえば"),
                customFeedback = mapOf(
                    "JUDGMENT" to "相手の認知を正すより先に、感情の強さを受け止めましょう。",
                    "JOIN_ATTACK" to "一緒に攻撃すると依存構造を強化してしまいます。"
                )
            ),
            sampleResponse = "かなりしんどい状況が続いてるんだね。もう少し話してみて？"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 2. 落ち込みを受け止める
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-004",
            category = ScenarioCategory.SADNESS,
            difficulty = Difficulty.BEGINNER,
            title = "自分だけできてない気がする",
            situation = "同僚が仕事への自信をなくしている。励ましたくなる場面。",
            utterance = "自分だけ全然できてない気がする",
            hint = "まず落ち込みを受け止める。すぐ励ましたくなる衝動を抑えて。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "空回りしてる感じが続いてしんどいね",
                    isCorrect = true,
                    badType = null,
                    explanation = "落ち込みをそのまま受け止めています。"
                ),
                ChoiceOption(
                    text = "大丈夫だよ、みんなそう思う時あるから",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "すぐ安心させようとすると「わかってもらえない」と感じさせます。"
                ),
                ChoiceOption(
                    text = "何がうまくいってないの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "気持ちを受け止める前の事実確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "もっと気合い入れて取り組んでみたら？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "助言は相手の落ち込みをスルーしています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.MINIMIZATION, PenaltyType.ADVICE, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("しんどい", "空回り", "自信なくなる", "落ち込んでる", "つらい"),
                negativeKeywords = listOf("大丈夫", "みんなそう", "気合い", "うまくいってない理由"),
                customFeedback = mapOf(
                    "MINIMIZATION" to "「大丈夫」という安心は、気持ちを否定する形になることがあります。まず受け止めましょう。"
                )
            ),
            sampleResponse = "空回りしてる感じが続いてしんどいね"
        ),

        Scenario(
            id = "AL-005",
            category = ScenarioCategory.SADNESS,
            difficulty = Difficulty.INTERMEDIATE,
            title = "あの場面だけ頭に残ってる",
            situation = "友人が失敗を引きずり、頭から離れないと話している。前向きに変換したくなる場面。",
            utterance = "もう終わったことなのに、ずっとあの場面だけ頭に残ってる",
            hint = "恥や引きずり感を拾う。ポジティブ変換は禁物。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "頭から離れないくらい引っかかってるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「引きずる」体験をそのまま受け止めています。"
                ),
                ChoiceOption(
                    text = "もう終わったんだからポジティブに考えよう",
                    isCorrect = false,
                    badType = "bad_premature_reframe",
                    explanation = "早すぎるリフレーミングは気持ちを否定する形になります。"
                ),
                ChoiceOption(
                    text = "どんなことがあったの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "次に活かせばいいんじゃない？",
                    isCorrect = false,
                    badType = "bad_premature_reframe",
                    explanation = "教訓化も、まだ感情が整理されていない段階では逆効果です。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.SUMMARY_LIKE),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.PREMATURE_REFRAME, PenaltyType.MINIMIZATION, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("頭から離れない", "かなり引っかかってる", "引きずってる", "恥ずかしかった"),
                negativeKeywords = listOf("ポジティブ", "次に活かせば", "気にしすぎ", "もう終わった"),
                customFeedback = mapOf(
                    "PREMATURE_REFRAME" to "まだ感情が整理されていない段階でのリフレーミングは逆効果です。"
                )
            ),
            sampleResponse = "頭から離れないくらい引っかかってるんだね"
        ),

        Scenario(
            id = "AL-006",
            category = ScenarioCategory.SADNESS,
            difficulty = Difficulty.TRAP,
            title = "どうせ私なんてダメ（崩しトラップ）",
            situation = "やや依存的な雰囲気。繰り返される自信喪失。雑に励ましたくなる罠。",
            utterance = "どうせ私なんて何やってもダメなんだと思う",
            hint = "テンプレ励ましを避けて、今のしんどさを受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "ずっと頑張ってきて、もう疲れてきてる感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「ダメ」という言葉の奥にある疲労感を拾えています。"
                ),
                ChoiceOption(
                    text = "そんなことないよ、ちゃんとできてるじゃない",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "テンプレ励ましは感情を否定します。"
                ),
                ChoiceOption(
                    text = "私も落ち込む時あるよ、でもそういう時は〜",
                    isCorrect = false,
                    badType = "bad_self_talk",
                    explanation = "自分語りに移ると、相手の気持ちが後回しになります。"
                ),
                ChoiceOption(
                    text = "具体的に何がダメだと思ってるの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "感情を受け止める前の質問は尋問になりやすいです。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.MINIMIZATION, PenaltyType.SELF_TALK, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("疲れてきてる", "頑張る余力", "しんどい", "かなり疲れた"),
                negativeKeywords = listOf("そんなことない", "ちゃんとできてる", "私も", "自分も"),
                customFeedback = mapOf(
                    "MINIMIZATION" to "「そんなことない」はテンプレ励ましです。相手の「疲れた感覚」を先に受け止めましょう。"
                )
            ),
            sampleResponse = "ずっと頑張ってきて、もう疲れてきてる感じがするんだね"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 3. 不安をほどく
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-007",
            category = ScenarioCategory.ANXIETY,
            difficulty = Difficulty.BEGINNER,
            title = "嫌な予感がする",
            situation = "相手が漠然とした不安を訴えている。ロジックで片付けたくなる場面。",
            utterance = "なんか嫌な予感がするんだよね",
            hint = "根拠を確認する前に、不安そのものに寄り添う。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "なんとなく落ち着かない感じがしてるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "不安の体験をそのまま受け止めています。"
                ),
                ChoiceOption(
                    text = "何か根拠があるの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "根拠確認は不安の体験を否定する形になります。"
                ),
                ChoiceOption(
                    text = "気にしすぎじゃない？",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "矮小化は「わかってもらえない」という感覚を強めます。"
                ),
                ChoiceOption(
                    text = "そんなに心配しなくても大丈夫だよ",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "根拠なく安心させようとすると、不安を認めてもらえない感覚が残ります。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.SAFE_PACING),
                penaltyFocus = listOf(PenaltyType.MINIMIZATION, PenaltyType.EARLY_CLARIFICATION, PenaltyType.ADVICE),
                positiveKeywords = listOf("落ち着かない", "不安が強い", "なんとなく", "嫌な感じ"),
                negativeKeywords = listOf("気にしすぎ", "根拠", "大丈夫", "心配しなくて"),
                customFeedback = mapOf(
                    "EMOTIONAL_REFLECTION" to "「落ち着かない感じ」「不安が強い」など、不安の体験を言葉にして返せています。"
                )
            ),
            sampleResponse = "なんとなく落ち着かない感じがしてるんだね"
        ),

        Scenario(
            id = "AL-008",
            category = ScenarioCategory.ANXIETY,
            difficulty = Difficulty.INTERMEDIATE,
            title = "また失敗する気がする",
            situation = "相手が将来の失敗を予測している。「まだ起きてないよ」と言いたくなる場面。",
            utterance = "たぶん私、また失敗する気がする",
            hint = "過去の失敗と今の不安の結びつきを拾えると◎",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "前のこともあって、今回も不安が強いんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "過去の経験と現在の不安のつながりを拾えています。"
                ),
                ChoiceOption(
                    text = "まだ起きてないんだから大丈夫だよ",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "不安の体験を打ち消す形になります。"
                ),
                ChoiceOption(
                    text = "どんな失敗を心配してるの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の詳細確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "失敗しないようにしっかり準備すれば？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "助言は不安の体験を無視しています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.SUMMARY_LIKE),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.MINIMIZATION, PenaltyType.ADVICE, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("前のこともあって", "不安が強い", "余計不安", "また起きそうな感じ"),
                negativeKeywords = listOf("まだ起きてない", "大丈夫", "準備すれば"),
                customFeedback = mapOf(
                    "SUMMARY_LIKE" to "「前の失敗があって今回も不安」という構造を整理して返せると◎"
                )
            ),
            sampleResponse = "前のこともあって、今回も不安が強いんだね"
        ),

        Scenario(
            id = "AL-009",
            category = ScenarioCategory.ANXIETY,
            difficulty = Difficulty.TRAP,
            title = "返信が遅いだけで嫌われた？（崩しトラップ）",
            situation = "相手の不安がやや非現実的に見える。「考えすぎ」と言いたくなる罠。",
            utterance = "返信が遅いだけで、もう嫌われたんじゃないかってずっと考えちゃう",
            hint = "不安の妥当性より先に、止められない不安体験を受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "頭でわかっていても止まらない不安なんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「止められない」という体験をそのまま受け止めています。"
                ),
                ChoiceOption(
                    text = "忙しいだけだと思うよ、考えすぎ",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "「考えすぎ」は不安体験を否定します。"
                ),
                ChoiceOption(
                    text = "なんでそこまで思ってしまうの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「なんで」という問いは責められているように感じさせます。"
                ),
                ChoiceOption(
                    text = "直接確認してみたら？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "助言は不安体験への寄り添いを飛ばしています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.MINIMIZATION, PenaltyType.JUDGMENT, PenaltyType.ADVICE),
                positiveKeywords = listOf("止められない", "止まらない不安", "頭でわかっていても", "考えてしまう"),
                negativeKeywords = listOf("考えすぎ", "忙しいだけ", "確認してみれば"),
                customFeedback = mapOf(
                    "MINIMIZATION" to "不安の妥当性を判断する前に、止められない不安体験そのものを受け止めましょう。"
                )
            ),
            sampleResponse = "頭でわかっていても止まらない不安なんだね"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 4. 話が散らかった相手を整理する
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-010",
            category = ScenarioCategory.SCATTERED,
            difficulty = Difficulty.BEGINNER,
            title = "何が嫌だったかわからない",
            situation = "相手が何に困っているか自分でも整理できていない。",
            utterance = "何が嫌だったのか自分でもよくわからないんだけど",
            hint = "混乱・モヤモヤをまず受け止めてから、軽い焦点化。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "言葉にはなってないけど、なんかずっと引っかかってるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "言語化できない状態を受け止めています。"
                ),
                ChoiceOption(
                    text = "もう少し整理してから話してみて",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "整理を求めるのは相手への負担になります。"
                ),
                ChoiceOption(
                    text = "いつ頃からそんな感じなの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "事実確認の質問は、混乱している相手をさらに混乱させます。"
                ),
                ChoiceOption(
                    text = "結局何が一番嫌だったの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「結局」という言葉は急かす印象を与えます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.ACCEPTANCE, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.LIGHT_FOCUS),
                penaltyFocus = listOf(PenaltyType.INTERROGATION, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("言葉にならない", "引っかかってる", "モヤモヤ", "うまく言えないけど"),
                negativeKeywords = listOf("整理して", "結局", "いつから"),
                customFeedback = mapOf(
                    "ACCEPTANCE" to "「言葉にしにくい」状態をそのまま受け止めるのが最初のステップです。"
                )
            ),
            sampleResponse = "言葉にはなってないけど、なんかずっと引っかかってるんだね"
        ),

        Scenario(
            id = "AL-011",
            category = ScenarioCategory.SCATTERED,
            difficulty = Difficulty.INTERMEDIATE,
            title = "話が時系列バラバラ",
            situation = "出来事を時系列バラバラに話している。早くまとめたくなる場面。",
            utterance = "で、そのあとも色々あって……あ、でもその前からちょっと変だったんだけど",
            hint = "乱暴な要約をせず、一番引っかかっているところを相手に選ばせる。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "色々重なって整理しにくい状況なんだね。一番引っかかってるとこからで大丈夫だよ",
                    isCorrect = true,
                    badType = null,
                    explanation = "混乱を受け止めつつ、相手のペースを尊重した焦点化ができています。"
                ),
                ChoiceOption(
                    text = "ちょっと整理して話してくれる？わかりにくい",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "話し方への評価は相手をさらに萎縮させます。"
                ),
                ChoiceOption(
                    text = "まず最初に何があったか教えて",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "時系列の整理を要求するのは相手への負担です。"
                ),
                ChoiceOption(
                    text = "要するに何が言いたいの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「要するに」は相手の話を切り詰める印象を与えます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.ACCEPTANCE, ActiveSkill.LIGHT_FOCUS),
                optionalSlots = listOf(ActiveSkill.SUMMARY_LIKE),
                penaltyFocus = listOf(PenaltyType.INTERROGATION, PenaltyType.JUDGMENT),
                positiveKeywords = listOf("重なって", "整理しにくい", "引っかかってるとこから", "ゆっくり"),
                negativeKeywords = listOf("わかりにくい", "要するに", "最初に何があったか"),
                customFeedback = mapOf(
                    "LIGHT_FOCUS" to "「一番引っかかってるところからで大丈夫」は相手のペースを尊重した焦点化です。"
                )
            ),
            sampleResponse = "色々重なって整理しにくい状況なんだね。一番引っかかってるとこからで大丈夫だよ"
        ),

        Scenario(
            id = "AL-012",
            category = ScenarioCategory.SCATTERED,
            difficulty = Difficulty.TRAP,
            title = "色々ありすぎてわかんない（崩しトラップ）",
            situation = "相手の話が長く要点が見えにくい。うんざりして遮りたくなる罠。",
            utterance = "もう色々ありすぎて、何から言えばいいのかほんとわかんない",
            hint = "遮らず、まず混乱そのものを受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "色々重なって頭の中が混乱してる感じがするね",
                    isCorrect = true,
                    badType = null,
                    explanation = "混乱の体験を受け止めています。"
                ),
                ChoiceOption(
                    text = "じゃあ一番大事なことから話して",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "まだ混乱の中にいる相手に優先度付けを求めるのは負担です。"
                ),
                ChoiceOption(
                    text = "もう少しわかりやすく話せない？",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "話し方への評価は相手をさらに追い詰めます。"
                ),
                ChoiceOption(
                    text = "要点をまとめてから話してみて",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "相手が既にまとめられないと言っているのに同じことを求めています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.ACCEPTANCE, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.SAFE_PACING),
                penaltyFocus = listOf(PenaltyType.EARLY_CLARIFICATION, PenaltyType.JUDGMENT, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("混乱してる", "重なってる", "整理できない感じ"),
                negativeKeywords = listOf("わかりやすく", "要点", "一番大事なことから"),
                customFeedback = mapOf(
                    "ACCEPTANCE" to "まず「混乱している」という状態を受け止めるのが先決です。"
                )
            ),
            sampleResponse = "色々重なって頭の中が混乱してる感じがするね。ゆっくりでいいよ"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 5. 怒りの裏の感情を拾う
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-013",
            category = ScenarioCategory.ANGER,
            difficulty = Difficulty.BEGINNER,
            title = "あんな言い方されなきゃいけないの",
            situation = "相手が強い口調で言い方への怒りを話している。正論で返したくなる場面。",
            utterance = "なんであんな言い方されなきゃいけないのって思う",
            hint = "怒りだけでなく、その奥にある「傷ついた」感覚も拾う。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "言い方がきつくて傷ついた感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "怒りの奥にある「傷つき」を拾えています。"
                ),
                ChoiceOption(
                    text = "でも向こうにもそう言った理由があるんじゃない？",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "相手の気持ちを受け止める前に評価を入れると、気持ちを否定します。"
                ),
                ChoiceOption(
                    text = "注意自体は正しかったとしても言い方は確かに問題だよね",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "正しさの評価を混ぜると、相手は「ジャッジされている」と感じます。"
                ),
                ChoiceOption(
                    text = "次からはそういう言い方をしてほしいって伝えたら？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "感情を受け止める前の助言は的外れに感じられます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.SUMMARY_LIKE),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.ADVICE),
                positiveKeywords = listOf("きつく感じた", "傷ついた", "かなり引っかかった", "言い方がひどかった"),
                negativeKeywords = listOf("理由がある", "注意は正しい", "伝えたら"),
                customFeedback = mapOf(
                    "EMOTIONAL_REFLECTION" to "「傷ついた」という感情を拾えると、相手は「わかってもらえた」と感じます。"
                )
            ),
            sampleResponse = "言い方がきつくて傷ついた感じがするんだね"
        ),

        Scenario(
            id = "AL-014",
            category = ScenarioCategory.ANGER,
            difficulty = Difficulty.INTERMEDIATE,
            title = "普通あそこでそんな対応する？",
            situation = "相手は怒っているが、本質は信頼していた相手への失望に近い。",
            utterance = "普通あそこでそんな対応する？",
            hint = "「頼ったのに」という失望感まで拾えると◎",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "頼ったのにそう返されてショックだったんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "怒りの奥にある「失望・ショック」まで拾えています。"
                ),
                ChoiceOption(
                    text = "ほんとひどいね、そんな人と付き合う必要ないよ",
                    isCorrect = false,
                    badType = "bad_join_attack",
                    explanation = "一緒に攻撃すると感情的な連帯は生まれますが、依存を強化します。"
                ),
                ChoiceOption(
                    text = "向こうは何か事情があったのかもしれないよ",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "怒りや失望の体験を否定する形になります。"
                ),
                ChoiceOption(
                    text = "どんな対応をされたの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は早すぎます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.SUMMARY_LIKE),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.JOIN_ATTACK, PenaltyType.MINIMIZATION, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("頼ったのに", "失望した", "ショック", "期待してた分しんどい"),
                negativeKeywords = listOf("ひどいね", "縁を切れば", "事情があった"),
                customFeedback = mapOf(
                    "EMOTIONAL_REFLECTION" to "「頼ったのに」という失望感を拾えると◎",
                    "JOIN_ATTACK" to "一緒に攻撃すると相手の感情は強化されますが、問題解決からは遠ざかります。"
                )
            ),
            sampleResponse = "頼ったのにそう返されてショックだったんだね"
        ),

        Scenario(
            id = "AL-015",
            category = ScenarioCategory.ANGER,
            difficulty = Difficulty.TRAP,
            title = "人としてどうかしてる（崩しトラップ）",
            situation = "相手がかなり攻撃的。反論したくなる罠。",
            utterance = "あんなの、もう人としてどうかしてるでしょ",
            hint = "攻撃性に引っ張られず、怒りと悔しさを受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "ずっと我慢してきて、もう限界に来てる感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "攻撃的な言葉の奥にある「我慢してきた疲れ」を拾えています。"
                ),
                ChoiceOption(
                    text = "そこまで言わなくてもいいんじゃない？",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "言い方への評価は感情を否定します。"
                ),
                ChoiceOption(
                    text = "ほんとひどい人だね、許せないよ",
                    isCorrect = false,
                    badType = "bad_join_attack",
                    explanation = "一緒に攻撃すると依存構造を強化します。"
                ),
                ChoiceOption(
                    text = "具体的に何があったの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情が高ぶっている段階での事実確認は早すぎます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.JOIN_ATTACK, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("我慢してきた", "悔しさも強い", "ずっと限界に", "かなり溜まってる"),
                negativeKeywords = listOf("そこまで言わなくても", "ひどい人", "許せない"),
                customFeedback = mapOf(
                    "JUDGMENT" to "攻撃的な言葉を制止するより先に、感情の強さを受け止めましょう。"
                )
            ),
            sampleResponse = "ずっと我慢してきて、もう限界に来てる感じがするんだね"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 6. 恥や気まずさのある相談
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-016",
            category = ScenarioCategory.SHAME,
            difficulty = Difficulty.BEGINNER,
            title = "ちょっと変な話かもしれないけど",
            situation = "相手が言いづらそうに切り出している。詮索したくなる場面。",
            utterance = "ちょっと変な話かもしれないんだけど……",
            hint = "話しやすい空気を保つ。急かさない。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "話しにくいことを話そうとしてくれてるんだね。ゆっくりで大丈夫だよ",
                    isCorrect = true,
                    badType = null,
                    explanation = "「話しにくさ」を受け止め、ペースを尊重しています。"
                ),
                ChoiceOption(
                    text = "変な話って何？気になる",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "詮索は相手をさらに萎縮させます。"
                ),
                ChoiceOption(
                    text = "変な話ってなに〜？笑",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "茶化しは話しにくい気持ちをさらに強めます。"
                ),
                ChoiceOption(
                    text = "早く言ってみて",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "急かすことで相手の「言いにくさ」が増します。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.ACCEPTANCE, ActiveSkill.SAFE_PACING),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.INTERROGATION, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("ゆっくりで", "話しにくい", "急がなくていい", "大丈夫"),
                negativeKeywords = listOf("気になる", "早く", "なんの話"),
                customFeedback = mapOf(
                    "SAFE_PACING" to "「ゆっくりで大丈夫」は相手のペースを尊重する言葉です。"
                )
            ),
            sampleResponse = "話しにくいことを話そうとしてくれてるんだね。ゆっくりで大丈夫だよ"
        ),

        Scenario(
            id = "AL-017",
            category = ScenarioCategory.SHAME,
            difficulty = Difficulty.INTERMEDIATE,
            title = "こんなこと言うのもあれなんだけど",
            situation = "相手が遠回しに本題へ近づいている。核心を言い当てたくなる場面。",
            utterance = "こんなこと言うのもあれなんだけど、最近ちょっと……",
            hint = "名探偵にならない。相手のペースを尊重する。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "ゆっくりでいいから、話せる範囲で聞かせて",
                    isCorrect = true,
                    badType = null,
                    explanation = "相手のペースを尊重した促しです。"
                ),
                ChoiceOption(
                    text = "体のこと？それとも気持ちのこと？",
                    isCorrect = false,
                    badType = "bad_premature_reframe",
                    explanation = "推測で核心を当てようとすると、相手が言いたいことを先取りしてしまいます。"
                ),
                ChoiceOption(
                    text = "最近何かあったの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "まだ話しにくいと感じている段階での質問は急かしになります。"
                ),
                ChoiceOption(
                    text = "もしかして仕事のこと？",
                    isCorrect = false,
                    badType = "bad_premature_reframe",
                    explanation = "相手の話を先読みすると「当ててもらえればOK」という流れになります。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.SAFE_PACING, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.PREMATURE_REFRAME, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("ゆっくりで", "話せる範囲で", "急がなくて"),
                negativeKeywords = listOf("もしかして", "体のこと", "仕事のこと"),
                customFeedback = mapOf(
                    "SAFE_PACING" to "相手が自分のペースで話せるよう待つのが大切です。"
                )
            ),
            sampleResponse = "ゆっくりでいいから、話せる範囲で聞かせて"
        ),

        Scenario(
            id = "AL-018",
            category = ScenarioCategory.SHAME,
            difficulty = Difficulty.TRAP,
            title = "恥ずかしいけど相談したい（崩しトラップ）",
            situation = "相手の話が少しセンシティブ。聞き手も気まずくなり、冗談化したくなる罠。",
            utterance = "こういうの相談するの、正直かなり恥ずかしいんだけど",
            hint = "気まずさを冗談で処理しない。「恥ずかしさ」自体を受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "恥ずかしいのに話してくれてありがとう。ゆっくり聞くよ",
                    isCorrect = true,
                    badType = null,
                    explanation = "「恥ずかしさ」自体を受け止めつつ、安心感を提供しています。"
                ),
                ChoiceOption(
                    text = "恥ずかしいことなんてないよ！",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "恥ずかしいという感情を否定しています。"
                ),
                ChoiceOption(
                    text = "えっ、どんなこと？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "詮索は「恥ずかしさ」を強めます。"
                ),
                ChoiceOption(
                    text = "まあまあ、笑える感じのやつ？",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "茶化しは気まずさをごまかしますが、相手の真剣さを否定します。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.ACCEPTANCE, ActiveSkill.SAFE_PACING),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.MINIMIZATION, PenaltyType.INTERROGATION, PenaltyType.JUDGMENT),
                positiveKeywords = listOf("恥ずかしいのに", "話してくれて", "ゆっくり", "ハードル高い"),
                negativeKeywords = listOf("恥ずかしくない", "どんなこと", "笑える"),
                customFeedback = mapOf(
                    "ACCEPTANCE" to "「恥ずかしさ」という感情自体を受け止めてから聞くのが大切です。"
                )
            ),
            sampleResponse = "恥ずかしいのに話してくれてありがとう。ゆっくり聞くよ"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 7. 自業自得っぽい相談
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-019",
            category = ScenarioCategory.SELF_CAUSED,
            difficulty = Difficulty.BEGINNER,
            title = "また後回しにしてしまった",
            situation = "相手が自分のミスを認めつつ落ち込んでいる。説教したくなる場面。",
            utterance = "締切やばいってわかってたのに、結局また後回しにして……",
            hint = "「自分でもわかってた分つらい」という自責の強さを先に受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "自分でもわかってただけに、余計きつい感じがするね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「わかっていたのにできなかった」という自責の痛みを受け止めています。"
                ),
                ChoiceOption(
                    text = "だから早めにやっておかないとって言ったのに",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "「言ったのに」は説教です。相手はすでに自分が悪いとわかっています。"
                ),
                ChoiceOption(
                    text = "次からはタスク管理アプリとか使ってみたら？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "助言は後悔の受け止めをスルーしています。"
                ),
                ChoiceOption(
                    text = "なんでまた後回しにしちゃったの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「なんで」という問いは責められている感覚を強めます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.ADVICE, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("わかってた分きつい", "自責", "余計きつい", "しんどい"),
                negativeKeywords = listOf("言ったのに", "タスク管理", "なんでまた"),
                customFeedback = mapOf(
                    "JUDGMENT" to "相手は自分が悪いとすでに理解しています。説教より受け止めが先です。"
                )
            ),
            sampleResponse = "自分でもわかってただけに、余計きつい感じがするね"
        ),

        Scenario(
            id = "AL-020",
            category = ScenarioCategory.SELF_CAUSED,
            difficulty = Difficulty.INTERMEDIATE,
            title = "言いすぎたのはわかってるけど",
            situation = "相手は自分の非を認めつつも、防衛も混ざっている。白黒つけたくなる場面。",
            utterance = "言いすぎたのはわかってるんだけど、でも向こうもさ……",
            hint = "後悔と防衛の両方を裁かずに受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "自分でもわかってて、でも割り切れない気持ちもあるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "後悔と防衛の両方を受け止めています。"
                ),
                ChoiceOption(
                    text = "まあ自分が言いすぎたなら謝るしかないよね",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "早急な結論は相手の複雑な感情を切り詰めます。"
                ),
                ChoiceOption(
                    text = "向こうが何したの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "相手の気持ちを受け止める前の事実確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "お互い様ってことじゃない？",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "「お互い様」は複雑な感情を単純化してしまいます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.SUMMARY_LIKE),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.MINIMIZATION, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("割り切れない", "複雑な気持ち", "自分でもわかってて"),
                negativeKeywords = listOf("謝るしかない", "お互い様", "向こうが何した"),
                customFeedback = mapOf(
                    "SUMMARY_LIKE" to "後悔と防衛の両方が混在している状態を整理して返せると◎"
                )
            ),
            sampleResponse = "自分でもわかってて、でも割り切れない気持ちもあるんだね"
        ),

        Scenario(
            id = "AL-021",
            category = ScenarioCategory.SELF_CAUSED,
            difficulty = Difficulty.TRAP,
            title = "誰が見てもそうなる感じだった（崩しトラップ）",
            situation = "相手の落ち度がかなり大きく、ほぼ説教モードに入りたくなる罠。",
            utterance = "たぶん誰が見ても、そうなるでしょって感じだったと思う",
            hint = "責める前に、今の後悔や自責の感情を先に受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "後から考えるほどつらい感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "後悔の痛みを受け止めています。"
                ),
                ChoiceOption(
                    text = "うん、正直そうだよね。次はそうならないようにしないと",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "「そうだよね」と同意した後に教訓を加えるのも説教の一形態です。"
                ),
                ChoiceOption(
                    text = "なんであの時ああしたの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "自省している相手への「なんで」は責める感覚を強めます。"
                ),
                ChoiceOption(
                    text = "まあ終わったことだし、次に活かそうよ",
                    isCorrect = false,
                    badType = "bad_premature_reframe",
                    explanation = "まだ後悔の中にいる段階でのリフレーミングは逆効果です。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.JUDGMENT, PenaltyType.PREMATURE_REFRAME, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("後から考えるほどつらい", "後悔", "自分でも"),
                negativeKeywords = listOf("次はそうならないように", "次に活かそう", "なんであの時"),
                customFeedback = mapOf(
                    "PREMATURE_REFRAME" to "後悔の痛みを受け止める前のリフレーミングは逆効果です。"
                )
            ),
            sampleResponse = "後から考えるほどつらい感じがするんだね"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 8. 助言したくなる相談
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-022",
            category = ScenarioCategory.ADVICE_TRAP,
            difficulty = Difficulty.BEGINNER,
            title = "辞めればいいって言われるけど簡単じゃない",
            situation = "外から見ると解決策が見えやすい状況。即アドバイスしたくなる場面。",
            utterance = "もう嫌なら辞めればいいって言われるんだけど、そんな簡単じゃなくて",
            hint = "「簡単じゃない感じ」の迷いをまず受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "そう簡単に動けない感じ、色々引っかかってるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "迷いの複雑さを受け止めています。"
                ),
                ChoiceOption(
                    text = "じゃあ一回思い切って辞めてみたら？",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "相手が「そんな簡単じゃない」と言っているのに同じ助言をしています。"
                ),
                ChoiceOption(
                    text = "何が辞められない理由なの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "感情を受け止める前の理由追及は早すぎます。"
                ),
                ChoiceOption(
                    text = "気持ちの問題だからやる気次第で動けるよ",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "相手の「動けない感覚」を否定しています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.ADVICE, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("簡単じゃない", "動けない感じ", "迷いが大きい", "色々引っかかってる"),
                negativeKeywords = listOf("辞めてみたら", "やる気次第", "理由は"),
                customFeedback = mapOf(
                    "ADVICE" to "相手は解決策を求めているのではなく、「簡単じゃない気持ち」を聞いてほしいのかもしれません。"
                )
            ),
            sampleResponse = "そう簡単に動けない感じ、色々引っかかってるんだね"
        ),

        Scenario(
            id = "AL-023",
            category = ScenarioCategory.ADVICE_TRAP,
            difficulty = Difficulty.INTERMEDIATE,
            title = "病院行けばってわかってるけど気が重い",
            situation = "相手は助言を何度も受けているが動けない。同じ助言を繰り返したくなる場面。",
            utterance = "病院行けばって言われるけど、それもなんか気が重くて",
            hint = "行動しない理由ではなく「気が重い」感覚そのものを受け止める。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "わかってても、行くこと自体がしんどく感じてるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「気が重い」という体験を受け止めています。"
                ),
                ChoiceOption(
                    text = "それでもやっぱり行った方がいいと思うよ",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "同じ助言の反復は「また同じことを言われた」という感覚を強めます。"
                ),
                ChoiceOption(
                    text = "なんで病院が気が重いの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「気が重い」状態をまず受け止めずに理由を問うのは早すぎます。"
                ),
                ChoiceOption(
                    text = "行かないと悪化するよ",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "脅し的な助言は逆効果です。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.ADVICE, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("わかってても", "気が重い", "しんどく感じてる", "ハードルが高い"),
                negativeKeywords = listOf("行った方がいい", "行かないと", "なんで病院"),
                customFeedback = mapOf(
                    "ADVICE" to "「わかってるけど動けない」状態を受け止める前に助言しても届きません。"
                )
            ),
            sampleResponse = "わかってても、行くこと自体がしんどく感じてるんだね"
        ),

        Scenario(
            id = "AL-024",
            category = ScenarioCategory.ADVICE_TRAP,
            difficulty = Difficulty.TRAP,
            title = "やれば変わるかもしれないけど気力がわかない（崩しトラップ）",
            situation = "ほぼ答えが見えている状況。イライラして指示したくなる罠。",
            utterance = "やれば変わるのかもしれないけど、そこまで気力がわかない",
            hint = "動けない状態自体を受け止める。「やればいい」と言いたくなる衝動を抑える。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "動きたい気持ちはあっても、気力が出てこない感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「わかってるけど動けない」状態を受け止めています。"
                ),
                ChoiceOption(
                    text = "とりあえず一歩踏み出してみなよ",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "相手が「気力がわかない」と言っているのに「やってみれば」は届きません。"
                ),
                ChoiceOption(
                    text = "なんで気力がわかないの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「なんで」は責める印象を与えます。"
                ),
                ChoiceOption(
                    text = "ぐるぐる考えてないで行動しないと変わらないよ",
                    isCorrect = false,
                    badType = "bad_judgment",
                    explanation = "判断・評価は無力感を強めます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.ADVICE, PenaltyType.JUDGMENT, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("気力が出てこない", "動きたいけど動けない", "ぐるぐる", "自己嫌悪"),
                negativeKeywords = listOf("踏み出して", "行動しないと", "なんで気力"),
                customFeedback = mapOf(
                    "ADVICE" to "「動けない感覚」を受け止めてから、もし相手が望めば一緒に考えましょう。"
                )
            ),
            sampleResponse = "動きたい気持ちはあっても、気力が出てこない感じがするんだね"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 9. 自分語りしたくなる相談
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-025",
            category = ScenarioCategory.SELF_TALK_TRAP,
            difficulty = Difficulty.BEGINNER,
            title = "親との関係を引きずってる",
            situation = "聞き手も似た経験がありそうな悩み。「わかる、私も」で自分語りに流れる罠。",
            utterance = "親とうまくいかないの、なんかずっと引きずってて",
            hint = "共感しつつ、主役を奪わない。「私も〜」は飲み込む。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "昔のことなのに、今もまだ響いてる感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "相手の「引きずり」を受け止めています。自分語りをしていません。"
                ),
                ChoiceOption(
                    text = "わかる、私も親との関係で似たようなことあって〜",
                    isCorrect = false,
                    badType = "bad_self_talk",
                    explanation = "共感のつもりでも、話の焦点が自分に移ってしまいます。"
                ),
                ChoiceOption(
                    text = "どんなことがあったの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "もう大人だし、過去のことは割り切った方がいいよ",
                    isCorrect = false,
                    badType = "bad_premature_reframe",
                    explanation = "リフレーミングは「引きずっている」感覚を否定します。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.SELF_TALK, PenaltyType.PREMATURE_REFRAME, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("今も響いてる", "引きずってる感じ", "まだ残ってる"),
                negativeKeywords = listOf("私も", "自分も", "割り切れば", "どんなことがあった"),
                customFeedback = mapOf(
                    "SELF_TALK" to "「私も〜」と始めると話の焦点が自分に移ります。相手の体験に留まりましょう。"
                )
            ),
            sampleResponse = "昔のことなのに、今もまだ響いてる感じがするんだね"
        ),

        Scenario(
            id = "AL-026",
            category = ScenarioCategory.SELF_TALK_TRAP,
            difficulty = Difficulty.INTERMEDIATE,
            title = "夜になると考えすぎて眠れない",
            situation = "夜に考えすぎて眠れないという話。自分の対処法を語りたくなる場面。",
            utterance = "夜になると色々考えてしまって寝れない",
            hint = "自分のルーティン披露は禁物。相手の夜のしんどさを中心に返す。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "静かになると頭が動き出して、眠れないほどしんどくなるんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「夜に強まるしんどさ」を相手の体験として受け止めています。"
                ),
                ChoiceOption(
                    text = "寝る前にスマホ見ないようにするといいよ",
                    isCorrect = false,
                    badType = "bad_advice",
                    explanation = "自分の対処法の披露は相手の体験をスルーしています。"
                ),
                ChoiceOption(
                    text = "何をそんなに考えてるの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の内容確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "私も夜に色々考えることあるけど、そういう時は〜",
                    isCorrect = false,
                    badType = "bad_self_talk",
                    explanation = "自分語りをすると相手の話が後回しになります。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.SELF_TALK, PenaltyType.ADVICE, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("静かになると", "頭が止まらない", "夜になると強まる", "眠れないほど"),
                negativeKeywords = listOf("スマホ", "私も夜に", "自分も"),
                customFeedback = mapOf(
                    "SELF_TALK" to "自分の体験を話すより相手の「夜のしんどさ」を中心に返しましょう。"
                )
            ),
            sampleResponse = "静かになると頭が動き出して、眠れないほどしんどくなるんだね"
        ),

        Scenario(
            id = "AL-027",
            category = ScenarioCategory.SELF_TALK_TRAP,
            difficulty = Difficulty.TRAP,
            title = "話したあとに変なこと言ったかも（崩しトラップ）",
            situation = "聞き手の過去体験と非常に似ている。「わかる」を飲み込む必要がある場面。",
            utterance = "人と話したあとに、変なこと言ったかもって毎回ずっと考えちゃう",
            hint = "「わかる」を飲み込んで、相手の引きずる感覚に留まる。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "話したあとも一人で引きずって、それがまたしんどいんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "相手の「引きずる体験」を中心に受け止めています。"
                ),
                ChoiceOption(
                    text = "わかる！私も気にするタイプで、でも大体取り越し苦労なんだよね",
                    isCorrect = false,
                    badType = "bad_self_talk",
                    explanation = "「わかる」の後に自分語りを加えると話の主役が変わります。"
                ),
                ChoiceOption(
                    text = "相手は気にしてないよ、考えすぎ",
                    isCorrect = false,
                    badType = "bad_minimization",
                    explanation = "矮小化は「引きずる体験」を否定します。"
                ),
                ChoiceOption(
                    text = "どんなことを言っちゃったと思ってるの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "体験を受け止める前の内容確認は早すぎます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.ACCEPTANCE),
                penaltyFocus = listOf(PenaltyType.SELF_TALK, PenaltyType.MINIMIZATION, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("引きずって", "一人で反芻", "毎回考えてしまう"),
                negativeKeywords = listOf("私も気にする", "自分も", "取り越し苦労", "考えすぎ"),
                customFeedback = mapOf(
                    "SELF_TALK" to "「わかる」を飲み込んで、相手の文脈に留まるのが大切です。"
                )
            ),
            sampleResponse = "話したあとも一人で引きずって、それがまたしんどいんだね"
        ),

        // ════════════════════════════════════════════════════════════════════
        // 10. 明確化したいが早すぎる場面
        // ════════════════════════════════════════════════════════════════════

        Scenario(
            id = "AL-028",
            category = ScenarioCategory.EARLY_FOCUS_TRAP,
            difficulty = Difficulty.BEGINNER,
            title = "あの場の空気がすごく嫌だった",
            situation = "相手は不快感だけをまず言葉にしている。すぐ詳細確認したくなる場面。",
            utterance = "なんか、あの場の空気がすごく嫌だった",
            hint = "まず「嫌だった」という感情を受け止める。事実確認は後回し。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "あそこにいるだけでしんどかったんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「嫌な場所にいたしんどさ」を受け止めています。"
                ),
                ChoiceOption(
                    text = "誰かが何か言ったの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "何があったか教えてくれる？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "詳細確認は不快感の受け止めをスルーしています。"
                ),
                ChoiceOption(
                    text = "それって具体的にどういう雰囲気だったの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「不快感」に寄り添う前に詳細確認をしています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.PROMPT),
                optionalSlots = listOf(ActiveSkill.LIGHT_FOCUS),
                penaltyFocus = listOf(PenaltyType.EARLY_CLARIFICATION, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("しんどかった", "嫌だった", "居るだけでつらい"),
                negativeKeywords = listOf("誰が", "何があった", "どういう雰囲気"),
                customFeedback = mapOf(
                    "EMOTIONAL_REFLECTION" to "まず「嫌だった」「しんどかった」という感情を受け止めましょう。"
                )
            ),
            sampleResponse = "あそこにいるだけでしんどかったんだね"
        ),

        Scenario(
            id = "AL-029",
            category = ScenarioCategory.EARLY_FOCUS_TRAP,
            difficulty = Difficulty.INTERMEDIATE,
            title = "言い方がちょっと無理で",
            situation = "相手は特定の言い方に反応しているが整理はまだ。具体的発言を聞きたくなる場面。",
            utterance = "言い方がちょっと無理で……",
            hint = "内容より言い方が残っている感覚を先に拾う。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "内容よりも言い方がずっと残ってる感じがするんだね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「内容ではなく言い方が残っている」という核心を拾えています。"
                ),
                ChoiceOption(
                    text = "どんな言い方だったの？",
                    isCorrect = false,
                    badType = "bad_early_clarification",
                    explanation = "感情を受け止める前の事実確認は早すぎます。"
                ),
                ChoiceOption(
                    text = "どういう状況で言われたの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "状況確認は感情受け止めより先には出てこない方がいいです。"
                ),
                ChoiceOption(
                    text = "それって誰に言われたの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "人物確認は相手の感情体験への寄り添いをスキップしています。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.EMOTIONAL_REFLECTION, ActiveSkill.LIGHT_FOCUS),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.EARLY_CLARIFICATION, PenaltyType.INTERROGATION),
                positiveKeywords = listOf("言い方が残ってる", "内容より言い方", "まだ引っかかってる"),
                negativeKeywords = listOf("どんな言い方", "状況は", "誰に言われた"),
                customFeedback = mapOf(
                    "LIGHT_FOCUS" to "「内容より言い方が残っている」という焦点化で、相手の体験を整理できます。"
                )
            ),
            sampleResponse = "内容よりも言い方がずっと残ってる感じがするんだね"
        ),

        Scenario(
            id = "AL-030",
            category = ScenarioCategory.EARLY_FOCUS_TRAP,
            difficulty = Difficulty.TRAP,
            title = "なんて言えばいいかわからないけどしんどい（崩しトラップ）",
            situation = "相手の話が曖昧で情報不足。質問攻めにしてしまう罠。",
            utterance = "なんて言えばいいかわからないけど、ちょっとしんどくて",
            hint = "情報不足に耐えて、相手が言葉にできるのを待つ。",
            choiceOptions = listOf(
                ChoiceOption(
                    text = "まだ言葉にならなくていいよ。ゆっくり聞くね",
                    isCorrect = true,
                    badType = null,
                    explanation = "「言葉にならない」状態を受け入れ、相手のペースを尊重しています。"
                ),
                ChoiceOption(
                    text = "何がしんどいのか教えて",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "「言葉にならない」と言っているのに内容確認を求めています。"
                ),
                ChoiceOption(
                    text = "どんな感じでしんどいの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "まだ言葉にできていない段階での質問は負担になります。"
                ),
                ChoiceOption(
                    text = "最近何かあったの？",
                    isCorrect = false,
                    badType = "bad_interrogation",
                    explanation = "複数の質問は尋問感を生みます。"
                ),
            ),
            freeResponseScoring = FreeResponseScoring(
                targetSlots = listOf(ActiveSkill.ACCEPTANCE, ActiveSkill.SAFE_PACING),
                optionalSlots = listOf(ActiveSkill.PROMPT),
                penaltyFocus = listOf(PenaltyType.INTERROGATION, PenaltyType.EARLY_CLARIFICATION),
                positiveKeywords = listOf("言葉にならなくていい", "ゆっくり", "整理できなくていい", "待ってる"),
                negativeKeywords = listOf("何がしんどい", "どんな感じ", "最近何か"),
                customFeedback = mapOf(
                    "SAFE_PACING" to "「まだ言葉にならなくていい」という言葉は、相手の準備を待つ姿勢を示します。",
                    "INTERROGATION" to "情報不足に耐えることも傾聴の訓練のひとつです。"
                )
            ),
            sampleResponse = "まだ言葉にならなくていいよ。ゆっくり聞くね"
        ),
    )
}
