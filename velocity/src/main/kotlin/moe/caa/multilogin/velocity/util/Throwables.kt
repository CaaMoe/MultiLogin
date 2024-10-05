package moe.caa.multilogin.velocity.util

import java.sql.SQLIntegrityConstraintViolationException

fun Throwable.causeIsSQLIntegrityConstraintViolationException(): Boolean {
    return cause is SQLIntegrityConstraintViolationException
}
