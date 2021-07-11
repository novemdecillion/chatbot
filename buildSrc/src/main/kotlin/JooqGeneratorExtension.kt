package io.github.novemdecillion

import org.jooq.meta.jaxb.ForcedType

open class JooqGeneratorExtension(
  var driver: String = "",
  var url: String = "",
  var user: String = "",
  var password: String = "",
  var directory: String = "",
  var packageName: String = "",
  var appendForcedTypes: Collection<ForcedType>? = null
)
