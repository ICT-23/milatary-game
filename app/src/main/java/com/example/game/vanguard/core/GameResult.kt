package com.example.game.vanguard.core

/**
 * Section 1.2 step 4 / Section 2 Core:
 * domain functions return an explicit `Result<T, GameError>` instead of throwing.
 *
 * Every failure carries a stable machine code so the UI can react without
 * parsing prose, and a message a player can read.
 */
enum class GameErrorCategory {
    NOT_READY,
    SAFETY,
    AMMUNITION,
    TIMING,
    AIM,
    GEOMETRY,
    INCOMPATIBLE,
    PROGRESSION,
    NARRATIVE,
    ECONOMY,
    SESSION_FLOW,
    INVALID_STATE
}

data class GameError(
    val category: GameErrorCategory,
    val code: String,
    val message: String,
    val detail: Map<String, String> = emptyMap()
) {
    companion object {
        fun of(category: GameErrorCategory, code: String, message: String): GameError =
            GameError(category, code, message)

        fun of(
            category: GameErrorCategory,
            code: String,
            message: String,
            key: String,
            value: String
        ): GameError = GameError(category, code, message, mapOf(key to value))
    }
}

/** The only result type domain logic is allowed to return. */
sealed interface GameResult<out T> {

    data class Ok<T>(val value: T) : GameResult<T>

    data class Err(val error: GameError) : GameResult<Nothing> {
        val code: String get() = error.code
    }

    companion object {
        fun <T> ok(value: T): GameResult<T> = Ok(value)

        fun <T> fail(
            category: GameErrorCategory,
            code: String,
            message: String
        ): GameResult<T> = Err(GameError.of(category, code, message))
    }
}

val GameResult<*>.isOk: Boolean get() = this is GameResult.Ok

val GameResult<*>.isErr: Boolean get() = this is GameResult.Err

fun <T> GameResult<T>.valueOrNull(): T? = (this as? GameResult.Ok)?.value

fun GameResult<*>.errorOrNull(): GameError? = (this as? GameResult.Err)?.error

fun GameResult<*>.errorCodeOrNull(): String? = errorOrNull()?.code

inline fun <T, R> GameResult<T>.map(transform: (T) -> R): GameResult<R> = when (this) {
    is GameResult.Ok -> GameResult.Ok(transform(value))
    is GameResult.Err -> this
}

inline fun <T> GameResult<T>.onOk(block: (T) -> Unit): GameResult<T> {
    if (this is GameResult.Ok) block(value)
    return this
}

inline fun <T> GameResult<T>.onErr(block: (GameError) -> Unit): GameResult<T> {
    if (this is GameResult.Err) block(error)
    return this
}

/**
 * Chains two domain steps without a `when` at every call site. A failure short
 * circuits and keeps its original code, which is what the contract audit asserts.
 */
inline fun <T, R> GameResult<T>.then(next: (T) -> GameResult<R>): GameResult<R> = when (this) {
    is GameResult.Ok -> next(value)
    is GameResult.Err -> this
}
