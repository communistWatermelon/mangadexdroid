package com.melonhead.lib_navigation.resolvers

import com.melonhead.lib_navigation.keys.ActivityKey

interface ResolverMap {
    val activityResolvers: Map<Class<out ActivityKey>, ActivityResolver<*>>
    fun registerResolver(key: Class<out ActivityKey>, resolver: ActivityResolver<*>)
}

internal class ResolverMapImpl: ResolverMap {
    private val mutableActivityResolvers = hashMapOf<Class<out ActivityKey>, ActivityResolver<*>>()
    override val activityResolvers = mutableActivityResolvers.toMap()

    override fun registerResolver(key: Class<out ActivityKey>, resolver: ActivityResolver<*>) {
        if (mutableActivityResolvers.containsKey(key)) {
            throw IllegalArgumentException("Resolver for key $key already registered")
        }
        mutableActivityResolvers[key] = resolver
    }
}
