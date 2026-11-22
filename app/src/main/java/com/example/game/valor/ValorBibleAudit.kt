package com.example.game.valor

import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.ContractCase

/**
 * The Art, Audio & UI Bible contract gate.
 *
 * Each group mirrors one section of the bible, and every case runs the shipping
 * data model in-process so a drifted number fails on the next launch instead of
 * on a device.
 */
object ValorBibleAudit {

    const val GROUP_PILLARS = "VALOR_PILLARS"
    const val GROUP_CHARACTERS = "VALOR_CHARACTERS"
    const val GROUP_WEAPONS = "VALOR_WEAPONS"
    const val GROUP_ENVIRONMENT = "VALOR_ENVIRONMENT"
    const val GROUP_ANIMATION = "VALOR_ANIMATION"
    const val GROUP_VFX = "VALOR_VFX"
    const val GROUP_AUDIO = "VALOR_AUDIO"
    const val GROUP_UI = "VALOR_UI"
    const val GROUP_ACCESS = "VALOR_ACCESS"
    const val GROUP_PIPELINE = "VALOR_PIPELINE"

    val GROUP_ORDER: List<String> = listOf(
        GROUP_PILLARS,
        GROUP_CHARACTERS,
        GROUP_WEAPONS,
        GROUP_ENVIRONMENT,
        GROUP_ANIMATION,
        GROUP_VFX,
        GROUP_AUDIO,
        GROUP_UI,
        GROUP_ACCESS,
        GROUP_PIPELINE
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        ValorArtContractCases.run() +
            ValorMotionContractCases.run() +
            ValorSystemsContractCases.run()
}
