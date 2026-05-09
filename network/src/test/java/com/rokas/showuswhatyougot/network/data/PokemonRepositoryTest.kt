package com.rokas.showuswhatyougot.network.data

import com.rokas.showuswhatyougot.model.Pokemon
import com.rokas.showuswhatyougot.model.PokemonDetail
import com.rokas.showuswhatyougot.network.NamedApiResourceResponse
import com.rokas.showuswhatyougot.network.PokeApiService
import com.rokas.showuswhatyougot.network.PokemonAbilitySlotResponse
import com.rokas.showuswhatyougot.network.PokemonArtworkResponse
import com.rokas.showuswhatyougot.network.PokemonDetailResponse
import com.rokas.showuswhatyougot.network.PokemonListEntry
import com.rokas.showuswhatyougot.network.PokemonListResponse
import com.rokas.showuswhatyougot.network.PokemonOtherSpritesResponse
import com.rokas.showuswhatyougot.network.PokemonSpritesResponse
import com.rokas.showuswhatyougot.network.PokemonTypeSlotResponse
import com.rokas.showuswhatyougot.storage.db.FavoritePokemonDao
import com.rokas.showuswhatyougot.storage.db.PokemonDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailEntity
import com.rokas.showuswhatyougot.storage.db.PokemonEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PokemonRepositoryTest {

    private val api = mockk<PokeApiService>()
    private val pokemonDao = mockk<PokemonDao>(relaxUnitFun = true)
    private val pokemonDetailDao = mockk<PokemonDetailDao>(relaxUnitFun = true)
    private val favoritePokemonDao = mockk<FavoritePokemonDao>(relaxUnitFun = true)

    private val repository = PokemonRepository(api, pokemonDao, pokemonDetailDao, favoritePokemonDao)

    @Test
    fun `getPokemonPage caches results in dao`() = runTest {
        coEvery { api.getPokemonList(limit = 30, offset = 0) } returns PokemonListResponse(
            next = "url?offset=30&limit=30",
            results = listOf(PokemonListEntry("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"))
        )

        val page = repository.getPokemonPage(30, 0)

        assertEquals(1, page.pokemon.size)
        assertEquals("Bulbasaur", page.pokemon[0].name)
        assertEquals(30, page.nextOffset)

        coVerify { pokemonDao.insertAll(match { it.size == 1 && it[0].id == 1 }) }
    }

    @Test
    fun `getPokemonDetail caches result in dao`() = runTest {
        coEvery { api.getPokemonDetail(25) } returns PokemonDetailResponse(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            abilities = listOf(PokemonAbilitySlotResponse(NamedApiResourceResponse("static"))),
            types = listOf(PokemonTypeSlotResponse(1, NamedApiResourceResponse("electric"))),
            sprites = PokemonSpritesResponse(
                frontDefault = "sprite.png",
                other = PokemonOtherSpritesResponse(PokemonArtworkResponse("artwork.png"))
            ),
        )

        val detail = repository.getPokemonDetail(25)

        assertEquals("Pikachu", detail.name)
        assertEquals("artwork.png", detail.imageUrl)
        assertEquals(listOf("Electric"), detail.types)
        assertEquals(listOf("Static"), detail.abilities)
        assertEquals(0.4, detail.heightMeters, 0.001)
        assertEquals(6.0, detail.weightKilograms, 0.001)

        coVerify { pokemonDetailDao.insert(match { it.id == 25 }) }
    }

    @Test
    fun `getCachedPokemonList returns mapped entities`() = runTest {
        coEvery { pokemonDao.getAll() } returns listOf(
            PokemonEntity(1, "Bulbasaur", "url1"),
            PokemonEntity(2, "Ivysaur", "url2"),
        )

        val result = repository.getCachedPokemonList()

        assertEquals(2, result.size)
        assertEquals(Pokemon(1, "Bulbasaur", "url1"), result[0])
    }

    @Test
    fun `getCachedPokemonDetail returns null when not cached`() = runTest {
        coEvery { pokemonDetailDao.getById(99) } returns null

        val result = repository.getCachedPokemonDetail(99)

        assertNull(result)
    }

    @Test
    fun `getCachedPokemonDetail returns mapped entity`() = runTest {
        coEvery { pokemonDetailDao.getById(25) } returns PokemonDetailEntity(
            id = 25,
            name = "Pikachu",
            imageUrl = "url",
            types = "Electric",
            abilities = "Static,Lightning Rod",
            heightMeters = 0.4,
            weightKilograms = 6.0,
        )

        val result = repository.getCachedPokemonDetail(25)

        assertEquals(PokemonDetail(
            id = 25,
            name = "Pikachu",
            imageUrl = "url",
            types = listOf("Electric"),
            abilities = listOf("Static", "Lightning Rod"),
            heightMeters = 0.4,
            weightKilograms = 6.0,
        ), result)
    }
}

