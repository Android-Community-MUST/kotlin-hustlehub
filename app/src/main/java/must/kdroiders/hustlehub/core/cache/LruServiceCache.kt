package must.kdroiders.hustlehub.core.cache

import must.kdroiders.hustlehub.ui.features.service.domain.model.Service

/**
 * Memory-bounded LRU cache for the home feed service list.
 * Backed by LinkedHashMap in access-order mode — O(1) insert and O(1) eviction.
 * Prevents unbounded memory growth during deep infinite scroll sessions.
 *
 * @param maxSize Maximum number of services to hold. Eldest entry is evicted when full.
 */
class LruServiceCache(private val maxSize: Int = 100) {
    private val cache = object : LinkedHashMap<String, Service>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, Service>) = size > maxSize
    }

    fun put(service: Service) {
        cache[service.id] = service
    }

    fun putAll(services: List<Service>) = services.forEach { put(it) }

    fun snapshot(): List<Service> = cache.values.toList()

    fun size(): Int = cache.size

    fun clear() = cache.clear()
}
