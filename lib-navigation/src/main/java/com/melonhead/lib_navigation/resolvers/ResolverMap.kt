package com.melonhead.lib_navigation.resolvers

import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.keys.ScreenKey

interface ResolverMap {
    val activityResolvers: Map<Class<out ActivityKey>, ActivityResolver<*>>
    fun registerResolver(key: Class<out ActivityKey>, resolver: ActivityResolver<*>)

    val screenResolvers: Map<Class<out ScreenKey>, ScreenResolver<*>>
    fun registerResolver(key: Class<out ScreenKey>, resolver: ScreenResolver<*>)
}

internal class ResolverMapImpl: ResolverMap {
    private val mutableActivityResolvers = hashMapOf<Class<out ActivityKey>, ActivityResolver<*>>()
    override val activityResolvers: HashMap<Class<out ActivityKey>, ActivityResolver<*>> = mutableActivityResolvers

    private val mutableScreenResolvers = hashMapOf<Class<out ScreenKey>, ScreenResolver<*>>()
    override val screenResolvers: HashMap<Class<out ScreenKey>, ScreenResolver<*>> = mutableScreenResolvers

    override fun registerResolver(key: Class<out ActivityKey>, resolver: ActivityResolver<*>) {
        if (mutableActivityResolvers.containsKey(key)) {
            throw IllegalArgumentException("ActivityResolver for key $key already registered")
        }
        mutableActivityResolvers[key] = resolver
    }

    override fun registerResolver(key: Class<out ScreenKey>, resolver: ScreenResolver<*>) {
        if (mutableScreenResolvers.containsKey(key)) {
            throw IllegalArgumentException("ScreenResolver for key $key already registered")
        }
        mutableScreenResolvers[key] = resolver
    }
}
