package must.kdroiders.hustlehub.core.search

/**
 * Prefix Trie for O(L) autocomplete where L = prefix length.
 * Populated lazily from the backend suggestions endpoint or loaded service catalog.
 */
class SearchTrie {
    private val root = TrieNode()
    private val maxSuggestionsPerPrefix = 5

    private inner class TrieNode {
        val children = HashMap<Char, TrieNode>(4)
        val suggestions = ArrayDeque<String>(maxSuggestionsPerPrefix)
    }

    fun insert(term: String) {
        val normalized = term.trim().lowercase()
        if (normalized.isBlank()) return
        var node = root
        for (ch in normalized) {
            node = node.children.getOrPut(ch) { TrieNode() }
            if (!node.suggestions.contains(term) && node.suggestions.size < maxSuggestionsPerPrefix) {
                node.suggestions.addLast(term)
            }
        }
    }

    fun suggest(prefix: String): List<String> {
        var node = root
        for (ch in prefix.trim().lowercase()) {
            node = node.children[ch] ?: return emptyList()
        }
        return node.suggestions.toList()
    }

    fun clear() {
        root.children.clear()
    }
}
