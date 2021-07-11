package io.github.novemdecillion.chat

import io.github.novemdecillion.adapter.jooq.tables.pojos.LogsEntity
import io.github.novemdecillion.adapter.jooq.tables.records.LogsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.net.InetSocketAddress

@Repository
@Transactional
class LogRepository(val dslContext: DSLContext) {
  fun insert(sessionId: String, kind: String, ipAddress: InetSocketAddress?, input: String?, output: String?, error: String? = null) {
    val record = LogsRecord()
    record.sessionId = sessionId
    record.kind = kind
    ipAddress?.hostString?.also { record.ipAddress = it }
    input?.also { record.input = it }
    output?.also { record.output = it }
    error?.also { record.error = it }
    dslContext.executeInsert(record)
  }
}