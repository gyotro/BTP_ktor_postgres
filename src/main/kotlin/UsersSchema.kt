package com.postgres

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

@Serializable
data class ExposedUser(val id: String? = null, val firstname: String, val lastname: String, val email: String)

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

