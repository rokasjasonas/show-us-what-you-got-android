package com.rokas.showuswhatyougot.data

import com.rokas.showuswhatyougot.network.data.PokemonRepository

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
import com.rokas.showuswhatyougot.storage.db.PokemonDao
import com.rokas.showuswhatyougot.storage.db.PokemonDetailDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PokemonRepositoryTest {

    private lateinit var apiService: PokeApiService
    private lateinit var pokemonDao: PokemonDao
    private lateinit var pokemonDetailDao: PokemonDetailDao
    private lateinit var repository: PokemonRepository

    @Before
    fun setUp() {
        apiService = mockk()
        pokemonDao = mockk(relaxUnitFun = true)
        pokemonDetailDao = mockk(relaxUnitFun = true)
        repository = PokemonRepository(apiService, pokemonDao, pokemonDetailDao)
    }

    @Test
    fun `getPokemonPage maps response to Pokemon list`() = runTest {
        coEvery { apiService.getPokemonList(limit = 20, offset = 0) } returns PokemonListResponse(
            next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
            results = listOf(
                PokemonListEntry(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonListEntry(name = "charmander", url = "https://pokeapi.co/api/v2/pokemon/4/"),
            ),
        )

        val page = repository.getPokemonPage(limit = 20, offset = 0)

        assertEquals(2, page.pokemon.size)
        assertEquals(1, page.pokemon[0].id)
        assertEquals("Bulbasaur", page.pokemon[0].name)
        assertEquals(4, page.pokemon[1].id)
        assertEquals("Charmander", page.pokemon[1].name)
        assertEquals(20, page.nextOffset)
    }

    @Test
    fun `getPokemonPage returns null nextOffset when no next page`() = runTest {
        coEvery { apiService.getPokemonList(limit = 20, offset = 0) } returns PokemonListResponse(
            next = null,
            results = listOf(
                PokemonListEntry(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"),
            ),
        )

        val page = repository.getPokemonPage(limit = 20, offset = 0)

        assertNull(page.nextOffset)
    }

    @Test
    fun `getPokemonPage sorts pokemon by id`() = runTest {
        coEvery { apiService.getPokemonList(limit = 20, offset = 0) } returns PokemonListResponse(
            next = null,
            results = listOf(
                PokemonListEntry(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon/25/"),
                PokemonListEntry(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"),
            ),
        )

        val page = repository.getPokemonPage(limit = 20, offset = 0)

        assertEquals(1, page.pokemon[0].id)
        assertEquals(25, page.pokemon[1].id)
    }

    @Test
    fun `getPokemonPage capitalizes hyphenated names`() = runTest {
        coEvery { apiService.getPokemonList(limit = 20, offset = 0) } returns PokemonListResponse(
            next = null,
            results = listOf(
                PokemonListEntry(name = "mr-mime", url = "https://pokeapi.co/api/v2/pokemon/122/"),
            ),
        )

        val page = repository.getPokemonPage(limit = 20, offset = 0)

        assertEquals("Mr Mime", page.pokemon[0].name)
    }

    @Test
    fun `getPokemonPage builds correct image url`() = runTest {
        coEvery { apiService.getPokemonList(limit = 20, offset = 0) } returns PokemonListResponse(
            next = null,
            results = listOf(
                PokemonListEntry(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon/1/"),
            ),
        )

        val page = repository.getPokemonPage(limit = 20, offset = 0)

        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            page.pokemon[0].imageUrl,
        )
    }

    @Test
    fun `getPokemonDetail maps response correctly`() = runTest {
        coEvery { apiService.getPokemonDetail(25) } returns PokemonDetailResponse(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            types = listOf(
                PokemonTypeSlotResponse(slot = 1, type = NamedApiResourceResponse("electric")),
            ),
            abilities = listOf(
                PokemonAbilitySlotResponse(ability = NamedApiResourceResponse("static")),
                PokemonAbilitySlotResponse(ability = NamedApiResourceResponse("lightning-rod")),
            ),
            sprites = PokemonSpritesResponse(
                frontDefault = "https://sprites/25.png",
                other = PokemonOtherSpritesResponse(
                    officialArtwork = PokemonArtworkResponse(frontDefault = "https://artwork/25.png"),
                ),
            ),
        )

        val detail = repository.getPokemonDetail(25)

        assertEquals(25, detail.id)
        assertEquals("Pikachu", detail.name)
        assertEquals("https://artwork/25.png", detail.imageUrl)
        assertEquals(listOf("Electric"), detail.types)
        assertEquals(listOf("Lightning Rod", "Static"), detail.abilities)
        assertEquals(0.4, detail.heightMeters, 0.001)
        assertEquals(6.0, detail.weightKilograms, 0.001)
    }

    @Test
    fun `getPokemonDetail uses fallback image when artwork is null`() = runTest {
        coEvery { apiService.getPokemonDetail(1) } returns PokemonDetailResponse(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            types = listOf(
                PokemonTypeSlotResponse(slot = 1, type = NamedApiResourceResponse("grass")),
            ),
            abilities = listOf(
                PokemonAbilitySlotResponse(ability = NamedApiResourceResponse("overgrow")),
            ),
            sprites = PokemonSpritesResponse(
                frontDefault = "https://sprites/1.png",
                other = null,
            ),
        )

        val detail = repository.getPokemonDetail(1)

        assertEquals("https://sprites/1.png", detail.imageUrl)
    }

    @Test
    fun `getPokemonDetail uses constructed url when all sprite fields are null`() = runTest {
        coEvery { apiService.getPokemonDetail(1) } returns PokemonDetailResponse(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            types = listOf(
                PokemonTypeSlotResponse(slot = 1, type = NamedApiResourceResponse("grass")),
            ),
            abilities = listOf(
                PokemonAbilitySlotResponse(ability = NamedApiResourceResponse("overgrow")),
            ),
            sprites = PokemonSpritesResponse(
                frontDefault = null,
                other = null,
            ),
        )

        val detail = repository.getPokemonDetail(1)

        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            detail.imageUrl,
        )
    }
}

