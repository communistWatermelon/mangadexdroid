package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.models.auth.AuthToken

interface TokenProviderService {
    var token: AuthToken?
}

class InMemoryTokenProvider: TokenProviderService {
    override var token: AuthToken? = null
}