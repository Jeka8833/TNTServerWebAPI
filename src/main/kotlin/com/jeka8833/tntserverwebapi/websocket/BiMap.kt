package com.jeka8833.tntserverwebapi.websocket

class BiMap<K, V> {
    private val keyToValueMap: MutableMap<K, V> = HashMap()
    private val valueToKeyMap: MutableMap<V, K> = HashMap()
    fun put(key: K, value: V) {
        keyToValueMap[key] = value
        valueToKeyMap[value] = key
    }

    fun getKey(value: V): K? {
        return valueToKeyMap[value]
    }

    operator fun get(key: K): V? {
        return keyToValueMap[key]
    }
}
