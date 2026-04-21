package com.lerchenflo.taximeter.utilities

sealed interface Result<out D, out E: DataError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Failure<out E: DataError>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : DataError, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(map(data))
        is Result.Failure -> Result.Failure(error)
    }
}

fun <T, E : DataError> Result<T, E>.asEmptyResult(): EmptyResult<E> {
    return map { }
}

inline fun <T, E : DataError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return also { if (it is Result.Success) action(it.data) }
}

inline fun <T, E : DataError> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    return also { if (it is Result.Failure) action(it.error) }
}
