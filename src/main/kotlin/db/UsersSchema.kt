
package com.postgres.db

import arrow.core.Either
import com.postgres.errors.UserDbError
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import java.util.UUID

@Serializable
data class ExposedUser(val id: String? = null, val firstname: String, val lastname: String, val email: String)

/*
class UserServiceDB(val database: Database) {
    object Users : Table("users") {
        val id = uuid("id").autoGenerate()
        val firstname = varchar("firstname", length = 50)
        val lastname = varchar("lastname", length = 50)
        val email = varchar("email", length = 255).uniqueIndex()

        override val primaryKey = PrimaryKey(id)
    }

    suspend fun readAll(): List<ExposedUser>{
        return dbQuery {
            Users.selectAll()
                .map { ExposedUser(it[Users.id].toString(), it[Users.firstname], it[Users.lastname], it[Users.email]) }
        }
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }

    suspend fun create(user: ExposedUser): UUID = dbQuery {
        Users.insert {
            it[firstname] = user.firstname
            it[lastname] = user.lastname
            it[email] = user.email
        }[Users.id]
    }

    suspend fun read(id: UUID): ExposedUser? {
        return dbQuery {
            Users.selectAll()
                .where { Users.id.eq(id) }
                .map { ExposedUser(it[Users.id].toString(), it[Users.firstname], it[Users.lastname], it[Users.email]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: UUID?, user: ExposedUser): Boolean {
        return try {
            dbQuery {
                if (id == null) {
                    return@dbQuery false
                }
                Users.update({ Users.id eq id }) {
                    it[firstname] = user.firstname
                    it[lastname] = user.lastname
                    it[email] = user.email
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun delete(id: UUID): Boolean {
        return try {
            dbQuery {
                Users.deleteWhere { Users.id eq id }
            }
            true
        } catch (e: Exception) {
            false

        }
    }
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, db = database) { block() }
}

*/

class UserServiceDB(private val database: Database) {

    object Users : Table("users") {
        val id = uuid("id").autoGenerate()
        val firstname = varchar("firstname", length = 50)
        val lastname = varchar("lastname", length = 50)
        val email = varchar("email", length = 255).uniqueIndex()

        override val primaryKey = PrimaryKey(id)
    }

    /** Call this explicitly at startup (instead of init {}) */
    fun ensureSchema(): Either<UserDbError, Unit> =
        Either.catch {
            transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(Users)
            }
        }.mapLeft { it.toUserDbError() }

    suspend fun readAll(): Either<UserDbError, List<ExposedUser>> =
        dbQuery {
            Users.selectAll()
                .map {
                    ExposedUser(
                        id = it[Users.id].toString(),
                        firstname = it[Users.firstname],
                        lastname = it[Users.lastname],
                        email = it[Users.email]
                    )
                }
        }

    suspend fun create(user: ExposedUser): Either<UserDbError, UUID> =
        dbQuery {
            Users.insert {
                it[firstname] = user.firstname
                it[lastname] = user.lastname
                it[email] = user.email
            }[Users.id]
        }

    suspend fun read(id: UUID): Either<UserDbError, ExposedUser?> =
        dbQuery {
            Users.selectAll()
                .where { Users.id eq id }
                .map {
                    ExposedUser(
                        id = it[Users.id].toString(),
                        firstname = it[Users.firstname],
                        lastname = it[Users.lastname],
                        email = it[Users.email]
                    )
                }
                .singleOrNull()
        }

    suspend fun update(id: UUID?, user: ExposedUser): Either<UserDbError, Int> =
        dbQuery {
            if (id == null) {
                return@dbQuery 0
            }
            Users.update({ Users.id eq id }) {
                it[firstname] = user.firstname
                it[lastname] = user.lastname
                it[email] = user.email
            }
            // returns number of affected rows
        }

    suspend fun delete(id: UUID): Either<UserDbError, Int> =
        dbQuery {
            Users.deleteWhere { Users.id eq id }
            // returns number of affected rows
        }

    private suspend fun <T> dbQuery(block: suspend () -> T): Either<UserDbError, T> =
        Either.catch {
            newSuspendedTransaction(Dispatchers.IO, db = database) { block() }
        }.mapLeft { it.toUserDbError() }

    private fun Throwable.toUserDbError(): UserDbError {
        // Minimal mapping now; improve later for Postgres SQLState
        val sqlEx = this.findSqlException()
        if (sqlEx != null) {
            // Postgres unique violation SQLState = 23505
            if (sqlEx.sqlState == "23505") return UserDbError.UniqueViolation(field = "email", cause = this)
        }
        return UserDbError.Unexpected(this)
    }

    private fun Throwable.findSqlException(): SQLException? {
        var t: Throwable? = this
        while (t != null) {
            if (t is SQLException) return t
            t = t.cause
        }
        return null
    }
}
