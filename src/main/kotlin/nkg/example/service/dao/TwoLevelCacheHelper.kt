package nkg.example.service.dao

suspend fun getFromTwoSources(
    keys: Collection<String>,
    cacheAccessor: (String) -> String?,
    dataSourceAccessor: suspend (Collection<String>) -> Map<String, String>
): Map<String, String> {
    val cached = HashMap<String, String>()
    val notFoundKeys = ArrayList<String>()
    keys.forEach { key ->
        val value = cacheAccessor(key)
        if (value != null) {
            cached[key] = value
        } else {
            notFoundKeys += key
        }
    }
    val fromDatasource = dataSourceAccessor(notFoundKeys)
    // cached value is latest
    return fromDatasource + cached
}