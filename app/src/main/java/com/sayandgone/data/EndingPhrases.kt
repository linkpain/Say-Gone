package com.sayandgone.data

object EndingPhrases {
    private val general = listOf(
        "这一步，到这里就好。",
        "已经说完了。",
        "不需要继续。",
        "情绪到此为止。"
    )

    private val dayRational = listOf(
        "可以回到该做的事了。",
        "这一页已经翻过去。",
        "结束得很干净。",
        "不必回头看。"
    )

    private val nightComforting = listOf(
        "没关系，已经结束了。",
        "现在可以安静一下了。",
        "你已经撑过来了。",
        "到这里就好。"
    )

    private val longText = listOf(
        "你已经说得很彻底了。",
        "这些话，不需要再留着。",
        "已经全部放下。",
        "情绪完成了一次循环。"
    )

    fun getPhrase(length: Int, isDay: Boolean): String {
        val pool = when {
            length > 150 -> longText
            isDay -> dayRational + general
            else -> nightComforting + general
        }
        return pool.random()
    }
}
