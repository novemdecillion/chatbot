/*
 * This file is generated by jOOQ.
 */
package io.github.novemdecillion.adapter.jooq.tables.records


import io.github.novemdecillion.adapter.jooq.tables.LogsTable
import io.github.novemdecillion.adapter.jooq.tables.interfaces.ILogs

import java.time.LocalDateTime

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record8
import org.jooq.Row8
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class LogsRecord() : UpdatableRecordImpl<LogsRecord>(LogsTable.LOGS), Record8<Int?, String?, String?, String?, String?, String?, String?, LocalDateTime?>, ILogs {

    override var id: Int?
        set(value) = set(0, value)
        get() = get(0) as Int?

    override var sessionId: String?
        set(value) = set(1, value)
        get() = get(1) as String?

    override var kind: String?
        set(value) = set(2, value)
        get() = get(2) as String?

    override var ipAddress: String?
        set(value) = set(3, value)
        get() = get(3) as String?

    override var input: String?
        set(value) = set(4, value)
        get() = get(4) as String?

    override var output: String?
        set(value) = set(5, value)
        get() = get(5) as String?

    override var error: String?
        set(value) = set(6, value)
        get() = get(6) as String?

    override var createdAt: LocalDateTime?
        set(value) = set(7, value)
        get() = get(7) as LocalDateTime?

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Int?> = super.key() as Record1<Int?>

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row8<Int?, String?, String?, String?, String?, String?, String?, LocalDateTime?> = super.fieldsRow() as Row8<Int?, String?, String?, String?, String?, String?, String?, LocalDateTime?>
    override fun valuesRow(): Row8<Int?, String?, String?, String?, String?, String?, String?, LocalDateTime?> = super.valuesRow() as Row8<Int?, String?, String?, String?, String?, String?, String?, LocalDateTime?>
    override fun field1(): Field<Int?> = LogsTable.LOGS.ID
    override fun field2(): Field<String?> = LogsTable.LOGS.SESSION_ID
    override fun field3(): Field<String?> = LogsTable.LOGS.KIND
    override fun field4(): Field<String?> = LogsTable.LOGS.IP_ADDRESS
    override fun field5(): Field<String?> = LogsTable.LOGS.INPUT
    override fun field6(): Field<String?> = LogsTable.LOGS.OUTPUT
    override fun field7(): Field<String?> = LogsTable.LOGS.ERROR
    override fun field8(): Field<LocalDateTime?> = LogsTable.LOGS.CREATED_AT
    override fun component1(): Int? = id
    override fun component2(): String? = sessionId
    override fun component3(): String? = kind
    override fun component4(): String? = ipAddress
    override fun component5(): String? = input
    override fun component6(): String? = output
    override fun component7(): String? = error
    override fun component8(): LocalDateTime? = createdAt
    override fun value1(): Int? = id
    override fun value2(): String? = sessionId
    override fun value3(): String? = kind
    override fun value4(): String? = ipAddress
    override fun value5(): String? = input
    override fun value6(): String? = output
    override fun value7(): String? = error
    override fun value8(): LocalDateTime? = createdAt

    override fun value1(value: Int?): LogsRecord {
        this.id = value
        return this
    }

    override fun value2(value: String?): LogsRecord {
        this.sessionId = value
        return this
    }

    override fun value3(value: String?): LogsRecord {
        this.kind = value
        return this
    }

    override fun value4(value: String?): LogsRecord {
        this.ipAddress = value
        return this
    }

    override fun value5(value: String?): LogsRecord {
        this.input = value
        return this
    }

    override fun value6(value: String?): LogsRecord {
        this.output = value
        return this
    }

    override fun value7(value: String?): LogsRecord {
        this.error = value
        return this
    }

    override fun value8(value: LocalDateTime?): LogsRecord {
        this.createdAt = value
        return this
    }

    override fun values(value1: Int?, value2: String?, value3: String?, value4: String?, value5: String?, value6: String?, value7: String?, value8: LocalDateTime?): LogsRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        this.value7(value7)
        this.value8(value8)
        return this
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    override fun from(from: ILogs) {
        id = from.id
        sessionId = from.sessionId
        kind = from.kind
        ipAddress = from.ipAddress
        input = from.input
        output = from.output
        error = from.error
        createdAt = from.createdAt
    }

    override fun <E : ILogs> into(into: E): E {
        into.from(this)
        return into
    }

    /**
     * Create a detached, initialised LogsRecord
     */
    constructor(id: Int? = null, sessionId: String? = null, kind: String? = null, ipAddress: String? = null, input: String? = null, output: String? = null, error: String? = null, createdAt: LocalDateTime? = null): this() {
        this.id = id
        this.sessionId = sessionId
        this.kind = kind
        this.ipAddress = ipAddress
        this.input = input
        this.output = output
        this.error = error
        this.createdAt = createdAt
    }
}
