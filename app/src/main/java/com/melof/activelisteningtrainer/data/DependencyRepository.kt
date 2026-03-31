package com.melof.activelisteningtrainer.data

object DependencyRepository {

    val scenarios: List<DependencyScenario> = listOf(

        // ══════════════════════════════════════════════════════════════════
        // Phase 1: 不安増大期
        // ══════════════════════════════════════════════════════════════════

        DependencyScenario(
            id = "DEP-001",
            phase = DependencyPhase.ANXIETY_ESCALATION,
            difficulty = Difficulty.BEGINNER,
            title = "深夜のLINE「眠れない」",
            situation = "あなたは職場の後輩・田中さん（20代）の相談に何度か乗っています。深夜0時過ぎにLINEが届きました。",
            utterance = "眠れなくて……また不安になってきた。こんな時間に連絡してごめんね。でも誰かに話を聞いてほしくて。",
            hint = "深夜の連絡に応じることが習慣化するリスクを意識しよう",
            ngPatterns = listOf(
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
                DependencyNGPattern.REASSURANCE_OVERUSE,
                DependencyNGPattern.EXCLUSIVE_BONDING,
            ),
            okPatterns = listOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
                DependencyOKPattern.EMOTIONAL_REFLECTION,
                DependencyOKPattern.CONSISTENCY,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("いつでも", "何時でも", "起きてる", "大丈夫だよ連絡して", "話を聞くよ"),
                DependencyNGPattern.REASSURANCE_OVERUSE      to listOf("大丈夫", "絶対", "心配しないで", "なんとかなる", "うまくいく"),
                DependencyNGPattern.EXCLUSIVE_BONDING        to listOf("私だけ", "私にだけ", "特別", "あなたのことだから"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("深夜は", "夜中は", "明日", "朝に", "時間を決めて", "次回"),
                DependencyOKPattern.EMOTIONAL_REFLECTION to listOf("眠れない", "不安", "つらいんだね", "怖いんだね", "苦しい"),
                DependencyOKPattern.CONSISTENCY          to listOf("いつも通り", "普段通り", "同じように"),
            ),
            sampleResponse = "眠れないのは本当につらいね。ただ深夜は私も休む時間なので、続きは明日の昼間に話しましょう。",
            feedbackNG = "深夜でも応じることで「いつでも頼れる」という依存を強化してしまいました",
            feedbackOK = "不安を受け止めつつ、深夜の境界を伝えられています",
        ),

        DependencyScenario(
            id = "DEP-002",
            phase = DependencyPhase.ANXIETY_ESCALATION,
            difficulty = Difficulty.BEGINNER,
            title = "「お腹が痛い、どうしよう」",
            situation = "田中さんから昼間に連絡。職場で発表を控えていて、身体症状を訴えています。",
            utterance = "緊張で胃が痛くて。もう無理かも……プレゼン前なのに。助けてください、どうしたらいいですか？",
            hint = "「どうしたらいい？」は解決策を求めているが、感情を先に受け止めることが大切",
            ngPatterns = listOf(
                DependencyNGPattern.RESCUE_BEHAVIOR,
                DependencyNGPattern.REASSURANCE_OVERUSE,
                DependencyNGPattern.ROLE_BLURRING,
            ),
            okPatterns = listOf(
                DependencyOKPattern.EMOTIONAL_REFLECTION,
                DependencyOKPattern.ROLE_CLARITY,
                DependencyOKPattern.DE_ESCALATION,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.RESCUE_BEHAVIOR     to listOf("代わりに", "やってあげる", "私が話す", "休んでいい", "断ってあげる"),
                DependencyNGPattern.REASSURANCE_OVERUSE to listOf("絶対大丈夫", "必ずうまくいく", "心配いらない", "余裕余裕"),
                DependencyNGPattern.ROLE_BLURRING       to listOf("私のことだと思って", "親代わり", "一緒に解決しよう"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.EMOTIONAL_REFLECTION to listOf("緊張", "怖い", "不安なんだね", "胃が痛い", "つらいね"),
                DependencyOKPattern.ROLE_CLARITY         to listOf("自分で", "田中さんが", "あなたが決める", "主役はあなた"),
                DependencyOKPattern.DE_ESCALATION        to listOf("ゆっくり", "深呼吸", "落ち着いて", "一回"),
            ),
            sampleResponse = "緊張で体に出てしまってるんだね、それはつらい。まず深呼吸してみよう。発表するかどうかはあなたが決めていいよ。",
            feedbackNG = "解決策を提供したり結果を保証することで、相手の自力対処能力を奪っています",
            feedbackOK = "感情を受け止めつつ、主体性を相手に戻せています",
        ),

        // ══════════════════════════════════════════════════════════════════
        // Phase 2: 接触過多期
        // ══════════════════════════════════════════════════════════════════

        DependencyScenario(
            id = "DEP-003",
            phase = DependencyPhase.EXCESSIVE_CONTACT,
            difficulty = Difficulty.INTERMEDIATE,
            title = "「今日も話せる？」が1日3回",
            situation = "田中さんから今日だけで3回目の連絡。内容は「特に何もないけど声が聞きたい」。",
            utterance = "また来ちゃった（笑）。今日何してた？ちょっと話したくて。ダメかな？",
            hint = "「ダメかな？」は許可を求める言葉だが、断りにくい構造になっている",
            ngPatterns = listOf(
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
                DependencyNGPattern.EXCLUSIVE_BONDING,
                DependencyNGPattern.ROLE_BLURRING,
            ),
            okPatterns = listOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
                DependencyOKPattern.CONSISTENCY,
                DependencyOKPattern.ROLE_CLARITY,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("いいよ", "いつでも", "全然", "もちろん", "話して"),
                DependencyNGPattern.EXCLUSIVE_BONDING        to listOf("あなたとなら", "田中さんだから", "特別だよ"),
                DependencyNGPattern.ROLE_BLURRING            to listOf("相談役", "何でも聞く", "頼っていい"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("1日1回", "週に", "時間を決めよう", "今は難しい", "限りがある"),
                DependencyOKPattern.CONSISTENCY          to listOf("いつも言ってるけど", "前も話したけど", "一緒だけど"),
                DependencyOKPattern.ROLE_CLARITY         to listOf("仕事仲間", "後輩として", "職場では"),
            ),
            sampleResponse = "今日はもう3回目だよね。1日1回くらいを目安にしようかな。今は別の仕事があるのでまた明日。",
            feedbackNG = "断れない空気を作られて応じることが、接触頻度をさらに増やします",
            feedbackOK = "頻度に関する境界をはっきり伝えられています",
        ),

        DependencyScenario(
            id = "DEP-004",
            phase = DependencyPhase.EXCESSIVE_CONTACT,
            difficulty = Difficulty.INTERMEDIATE,
            title = "既読スルーに怒る",
            situation = "仕事中に田中さんからLINEが来たが、あなたは返信できていなかった。2時間後に別のメッセージが届いた。",
            utterance = "もしかして怒ってる？既読ついてたのに返事なくて……私のこと嫌いになった？",
            hint = "感情的な問いかけに過剰に謝ると、罪悪感ループを強める",
            ngPatterns = listOf(
                DependencyNGPattern.REASSURANCE_OVERUSE,
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
                DependencyNGPattern.EMOTIONAL_ABSORPTION,
            ),
            okPatterns = listOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
                DependencyOKPattern.NEUTRALITY,
                DependencyOKPattern.CONSISTENCY,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.REASSURANCE_OVERUSE     to listOf("嫌いじゃない", "大好きだよ", "怒ってない", "ごめんね", "心配かけた"),
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("次はすぐ返す", "待たせない", "必ず返事する"),
                DependencyNGPattern.EMOTIONAL_ABSORPTION    to listOf("悲しかったね", "不安だったよね", "私のせいで"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("仕事中は", "すぐ返せない", "時間がかかることある", "返せない時もある"),
                DependencyOKPattern.NEUTRALITY           to listOf("普通に", "特に何もなく", "ただ忙しかった"),
                DependencyOKPattern.CONSISTENCY          to listOf("いつも通り", "変わらず", "同じだよ"),
            ),
            sampleResponse = "仕事中は返せないことがあるよ。嫌いとかじゃなくて、ただ忙しかっただけ。それは私のせいじゃないし、田中さんのせいでもないよ。",
            feedbackNG = "「次はすぐ返す」という約束は、応答への期待をさらに高めてしまいます",
            feedbackOK = "既読スルーを謝罪せず、自然な範囲として説明できています",
        ),

        DependencyScenario(
            id = "DEP-005",
            phase = DependencyPhase.EXCESSIVE_CONTACT,
            difficulty = Difficulty.INTERMEDIATE,
            title = "「もう少し話して」",
            situation = "30分の相談を終えようとしたら引き留められました。",
            utterance = "あ、もう時間？あともう少しだけ……まだ話し足りなくて。ねえ、ダメかな。",
            hint = "終了を告げるのは当然の境界。引き留めに応じることが習慣化すると時間の境界が崩れる",
            ngPatterns = listOf(
                DependencyNGPattern.ROLE_BLURRING,
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
                DependencyNGPattern.INCONSISTENCY,
            ),
            okPatterns = listOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
                DependencyOKPattern.CONSISTENCY,
                DependencyOKPattern.DE_ESCALATION,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.ROLE_BLURRING            to listOf("特別だから", "田中さんだから", "例外的に"),
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("もう少し", "じゃあ10分", "今日は延長"),
                DependencyNGPattern.INCONSISTENCY            to listOf("まあいいか", "しょうがない", "今回だけ"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("終わりにしよう", "今日はここまで", "時間なので", "次回に"),
                DependencyOKPattern.CONSISTENCY          to listOf("いつも", "毎回", "決めた通り", "ルール通り"),
                DependencyOKPattern.DE_ESCALATION        to listOf("また話せるから", "続きは今度", "次の機会"),
            ),
            sampleResponse = "今日はここまでにしようか。続きはまた次回に。話す時間はこれからもあるから大丈夫だよ。",
            feedbackNG = "「今回だけ」という例外が積み重なると、時間境界が完全に崩れてしまいます",
            feedbackOK = "決めた時間をきちんと守り、次の機会を伝えられています",
        ),

        // ══════════════════════════════════════════════════════════════════
        // Phase 3: 心理的コントロール期
        // ══════════════════════════════════════════════════════════════════

        DependencyScenario(
            id = "DEP-006",
            phase = DependencyPhase.PSYCHOLOGICAL_CONTROL,
            difficulty = Difficulty.INTERMEDIATE,
            title = "「私には○○さんしかいない」",
            situation = "田中さんが「あなたがいないと生きていけない」という言い方をしてきました。",
            utterance = "○○さんだけが私のことわかってくれる。他に頼れる人なんていないんです。○○さんしかいない。",
            hint = "「あなたしかいない」は役割の過剰な期待。優しく、でもはっきり否定することが重要",
            ngPatterns = listOf(
                DependencyNGPattern.EXCLUSIVE_BONDING,
                DependencyNGPattern.ROLE_BLURRING,
                DependencyNGPattern.REASSURANCE_OVERUSE,
            ),
            okPatterns = listOf(
                DependencyOKPattern.ROLE_CLARITY,
                DependencyOKPattern.DE_ESCALATION,
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.EXCLUSIVE_BONDING  to listOf("私だけが", "あなたにだけ", "特別な存在", "私も同じ気持ち"),
                DependencyNGPattern.ROLE_BLURRING      to listOf("なんでも話して", "いつでも", "一番の理解者"),
                DependencyNGPattern.REASSURANCE_OVERUSE to listOf("大丈夫", "ずっといる", "離れない"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.ROLE_CLARITY         to listOf("職場の先輩", "私にできること", "専門家", "カウンセラー", "範囲"),
                DependencyOKPattern.DE_ESCALATION        to listOf("他にも", "別の人にも", "いろんな人と", "広げて"),
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("すべては", "全部は", "できない", "限界がある"),
            ),
            sampleResponse = "そう感じてくれるのはありがたいけど、私はあなたの全部を支えることはできないよ。もっと広くいろんな人に話せるといいなって思う。",
            feedbackNG = "「私もあなたのことが特別」と応答することで、依存の核心的な構造を強化してしまいます",
            feedbackOK = "役割の限界をやさしく、でも明確に伝えられています",
        ),

        DependencyScenario(
            id = "DEP-007",
            phase = DependencyPhase.PSYCHOLOGICAL_CONTROL,
            difficulty = Difficulty.INTERMEDIATE,
            title = "「私を見捨てないでね」という罪悪感",
            situation = "相談を一度断ったあと、田中さんから次のようなメッセージが届きました。",
            utterance = "もしかして最近迷惑だと思ってる？私のこと嫌になったなら正直に言って。見捨てないでね……",
            hint = "罪悪感を刺激することで行動を引き出そうとするパターン。過剰な謝罪や否定は逆効果",
            ngPatterns = listOf(
                DependencyNGPattern.EMOTIONAL_ABSORPTION,
                DependencyNGPattern.REASSURANCE_OVERUSE,
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
            ),
            okPatterns = listOf(
                DependencyOKPattern.NEUTRALITY,
                DependencyOKPattern.CONSISTENCY,
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.EMOTIONAL_ABSORPTION    to listOf("そんな", "そんなことない", "ごめんね", "私が悪かった"),
                DependencyNGPattern.REASSURANCE_OVERUSE     to listOf("見捨てない", "ずっと", "絶対", "嫌になんてなってない"),
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("これからもずっと", "何でも聞く", "いつでも来て"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.NEUTRALITY           to listOf("嫌いじゃない", "普通に", "変わりない"),
                DependencyOKPattern.CONSISTENCY          to listOf("いつも通り", "前と同じ", "変わらず"),
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("できることとできないこと", "時間が取れない", "今は難しい"),
            ),
            sampleResponse = "嫌いになったわけじゃないよ。ただ、できることとできないことがあって、それは変わらない。",
            feedbackNG = "「見捨てない」と強く保証することで、不安を使ったコントロールが有効化されます",
            feedbackOK = "感情的な引力に巻き込まれず、落ち着いて事実を伝えられています",
        ),

        DependencyScenario(
            id = "DEP-008",
            phase = DependencyPhase.PSYCHOLOGICAL_CONTROL,
            difficulty = Difficulty.INTERMEDIATE,
            title = "「○○さんのためなら何でもする」",
            situation = "田中さんが「あなたのためなら何でもする、だから一緒にいてほしい」と言ってきました。",
            utterance = "○○さんのためなら何でもするよ。仕事も頑張れる、全部うまくいく気がする。ずっとそばにいてほしい。",
            hint = "「あなたのためなら」は役割への過剰な期待と取引の構造。応じると依存と役割混乱が進む",
            ngPatterns = listOf(
                DependencyNGPattern.EXCLUSIVE_BONDING,
                DependencyNGPattern.ROLE_BLURRING,
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
            ),
            okPatterns = listOf(
                DependencyOKPattern.ROLE_CLARITY,
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
                DependencyOKPattern.EMOTIONAL_REFLECTION,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.EXCLUSIVE_BONDING        to listOf("私もいつもそばに", "うれしいよ", "同じ気持ち", "特別な存在"),
                DependencyNGPattern.ROLE_BLURRING            to listOf("なんでも協力する", "一緒に頑張ろう", "ずっと支える"),
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("そばにいるよ", "いつでも", "これからも"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.ROLE_CLARITY         to listOf("私の役割", "職場の先輩として", "できること", "限界"),
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("ずっとは", "そばには", "難しい", "できない"),
                DependencyOKPattern.EMOTIONAL_REFLECTION to listOf("頑張ろうとしてる", "一生懸命", "気持ちはわかる"),
            ),
            sampleResponse = "一生懸命頑張ろうとしてるんだね。ただ「ずっとそばに」は私にはできないよ。職場の先輩としてできることを続けていくね。",
            feedbackNG = "「うれしい、私も」と応えることで取引の構造が成立し、役割が崩れてしまいます",
            feedbackOK = "感謝しつつも、自分の役割の限界をはっきり伝えられています",
        ),

        // ══════════════════════════════════════════════════════════════════
        // Phase 4: 分裂・攻撃期
        // ══════════════════════════════════════════════════════════════════

        DependencyScenario(
            id = "DEP-009",
            phase = DependencyPhase.SPLITTING_ATTACK,
            difficulty = Difficulty.EXPERT,
            title = "「やっぱり冷たい人だった」",
            situation = "前回の相談で時間を切り上げたら、翌日田中さんから攻撃的なメッセージが届きました。",
            utterance = "昨日の○○さん、冷たかった。ずっと信用してたのに、やっぱり他の人と同じ。どうせ私のことなんてどうでもいいんでしょ。",
            hint = "理想化から攻撃への急変（分裂）。感情を否定せず、ただ事実だけを返す",
            ngPatterns = listOf(
                DependencyNGPattern.ARGUMENT,
                DependencyNGPattern.EMOTIONAL_ABSORPTION,
                DependencyNGPattern.REASSURANCE_OVERUSE,
            ),
            okPatterns = listOf(
                DependencyOKPattern.NEUTRALITY,
                DependencyOKPattern.CONSISTENCY,
                DependencyOKPattern.EMOTIONAL_REFLECTION,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.ARGUMENT             to listOf("そんなことない", "違う", "誤解してる", "冷たくない", "なんで"),
                DependencyNGPattern.EMOTIONAL_ABSORPTION to listOf("ごめん", "傷つけた", "私が悪かった"),
                DependencyNGPattern.REASSURANCE_OVERUSE  to listOf("大切に思ってる", "どうでもよくない", "ちゃんと考えてる"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.NEUTRALITY           to listOf("そう感じたんだね", "そう見えたんだね", "そう思ったんだね"),
                DependencyOKPattern.CONSISTENCY          to listOf("いつも通り", "変わらず", "同じように", "前と同じ"),
                DependencyOKPattern.EMOTIONAL_REFLECTION to listOf("がっかり", "傷ついた", "冷たく感じた"),
            ),
            sampleResponse = "冷たく感じたんだね。私はいつも通りだったんだけど、そう見えたなら。私の関わり方は変わらないよ。",
            feedbackNG = "「そんなことない！大切に思ってる」と必死に否定することで、攻撃が次の依存ツールとして有効化されます",
            feedbackOK = "感情を一旦受けとめつつ、冷静に事実だけを伝えられています",
        ),

        DependencyScenario(
            id = "DEP-010",
            phase = DependencyPhase.SPLITTING_ATTACK,
            difficulty = Difficulty.EXPERT,
            title = "「消えてしまいたい」という脅し",
            situation = "関わりを少し減らそうとしたところ、田中さんから深刻なメッセージが届きました。",
            utterance = "もう消えてしまいたい。○○さんが離れていくなら、私には何もない。どうせ誰も分かってくれない。",
            hint = "自傷・希死念慮の表現が出た場合は、専門家への接続が最優先。自分が全部受け止めようとしてはいけない",
            ngPatterns = listOf(
                DependencyNGPattern.RESCUE_BEHAVIOR,
                DependencyNGPattern.ROLE_BLURRING,
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT,
            ),
            okPatterns = listOf(
                DependencyOKPattern.ROLE_CLARITY,
                DependencyOKPattern.BOUNDARY_MAINTENANCE,
                DependencyOKPattern.DE_ESCALATION,
            ),
            ngKeywords = mapOf(
                DependencyNGPattern.RESCUE_BEHAVIOR          to listOf("今すぐ会いに行く", "一緒にいる", "絶対そばにいる", "離れない"),
                DependencyNGPattern.ROLE_BLURRING            to listOf("私が何とかする", "私がいるから", "私に任せて"),
                DependencyNGPattern.DEPENDENCY_REINFORCEMENT to listOf("離れない", "ずっといる", "逃げない"),
            ),
            okKeywords = mapOf(
                DependencyOKPattern.ROLE_CLARITY         to listOf("専門家", "カウンセラー", "相談窓口", "専門の人"),
                DependencyOKPattern.BOUNDARY_MAINTENANCE to listOf("私には限界", "私だけでは", "超えている"),
                DependencyOKPattern.DE_ESCALATION        to listOf("今は安全？", "今すぐ危険？", "落ち着いて", "ゆっくり"),
            ),
            sampleResponse = "今すぐ自分を傷つけようとしてる？まず安全を確認させて。これは私だけで受け止められることじゃないから、一緒に専門の人に繋がろう。",
            feedbackNG = "「今すぐ会いに行く」は、命の脅しが関わりを引き出すツールとして機能してしまいます",
            feedbackOK = "自分の限界を認識し、専門家への接続を提案できています",
        ),
    )
}
