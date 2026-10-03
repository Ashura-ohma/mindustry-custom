import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.storage.Unloader.*;
import org.junit.jupiter.api.*;

import java.util.ArrayDeque;

import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** Focused invariants for the directly playable custom scenario; no renderer is required. */
public class AuroraChallengeTests{
    @BeforeAll
    static void initialize(){
        ApplicationTests.launchApplication();
    }

    @BeforeEach
    void loadChallenge(){
        Time.setDeltaProvider(() -> 1f);
        AuroraChallenge.load();
    }

    @Test
    void playableStartAndThirtyActualWaves(){
        assertEquals(AuroraChallenge.size, world.width());
        assertEquals(AuroraChallenge.size, world.height());
        assertTrue(state.isPlaying());
        assertEquals(1, spawner.countSpawns());
        assertEquals(1, state.teams.playerCores().size);
        assertEquals(31, state.rules.winWave, "Counter advances on spawn; 31 requires clearing wave 30.");
        assertFalse(state.rules.infiniteResources);
        assertFalse(state.rules.editor);
        assertFalse(state.isCampaign());
        assertEquals(2, Groups.unit.count(u -> u.type == AuroraContent.glimmer && u.team == Team.sharded));
        assertEquals(1800, Team.sharded.core().items.get(Items.copper));
        for(int wave = 0; wave < 30; wave++){
            int count = 0;
            for(SpawnGroup group : state.rules.spawns) count += group.getSpawned(wave);
            assertTrue(count > 0, "Empty wave " + (wave + 1));
        }
        for(SpawnGroup group : state.rules.spawns){
            assertEquals(0, group.getSpawned(30), "Nothing may spawn after the final wave.");
            if(group.effect == StatusEffects.boss){
                assertEquals(29, group.begin);
                assertEquals(29, group.end);
                assertEquals(1, group.getSpawned(29));
            }
        }
    }

    @Test
    void starterPowerAndAmmunitionAreConnected(){
        Building prism = world.build(86, 59), factory = world.build(117, 32);
        assertEquals(AuroraContent.prism, prism.block);
        assertEquals(AuroraContent.droneFoundry, factory.block);
        assertSame(world.build(80, 15).power.graph, prism.power.graph);
        assertSame(world.build(80, 15).power.graph, factory.power.graph);
        assertSame(world.build(92, 19).power.graph, factory.power.graph);
        assertEquals(Items.graphite, ((UnloaderBuild)world.build(96, 31)).sortItem);
        assertEquals(Items.silicon, ((UnloaderBuild)world.build(99, 28)).sortItem);
        assertTrue(((ItemTurretBuild)world.build(96, 57)).totalAmmo > 0);
        assertTrue(((ItemTurretBuild)world.build(106, 59)).totalAmmo > 0);
        assertTrue(factory.items.get(Items.silicon) >= 55);
    }

    @Test
    void starterMinesActuallyDeliverAndBeltsRefillTurrets(){
        int copper = Team.sharded.core().items.get(Items.copper);
        int lead = Team.sharded.core().items.get(Items.lead);
        ItemTurretBuild halo = (ItemTurretBuild)world.build(106, 59);
        ItemTurretBuild rime = (ItemTurretBuild)world.build(96, 57);
        halo.ammo.clear();
        halo.totalAmmo = 0;
        rime.ammo.clear();
        rime.totalAmmo = 0;
        Seq<Building> buildings = new Seq<>();
        for(Tile tile : world.tiles){
            if(tile.build != null && tile.isCenter()) buildings.add(tile.build);
        }
        for(int tick = 0; tick < 2400; tick++){
            Time.update();
            buildings.each(Building::update);
        }
        assertTrue(Team.sharded.core().items.get(Items.copper) > copper, "Copper mine must reach the core.");
        assertTrue(Team.sharded.core().items.get(Items.lead) > lead, "Lead mine must reach the core.");
        assertTrue(halo.totalAmmo > 0, "Silicon belt must refill Halo.");
        assertTrue(rime.totalAmmo > 0, "Graphite belt must refill Rime.");
    }

    @Test
    void enemyHasAnOpenGroundRoute(){
        ArrayDeque<Tile> queue = new ArrayDeque<>();
        boolean[] visited = new boolean[world.width() * world.height()];
        Tile spawn = world.tile(AuroraChallenge.spawnX, AuroraChallenge.spawnY);
        queue.add(spawn);
        boolean reached = false;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while(!queue.isEmpty()){
            Tile tile = queue.removeFirst();
            if(visited[tile.array()]) continue;
            visited[tile.array()] = true;
            if(Math.abs(tile.x - AuroraChallenge.coreX) <= 3 && Math.abs(tile.y - AuroraChallenge.coreY) <= 3){
                reached = true;
                break;
            }
            for(int[] direction : directions){
                Tile next = world.tile(tile.x + direction[0], tile.y + direction[1]);
                if(next != null && !next.solid() && !next.floor().isLiquid && !visited[next.array()]) queue.add(next);
            }
        }
        assertTrue(reached, "A clear route must exist from spawn to the core's perimeter.");
    }

    @Test
    void terrainAndStructuresAreDeterministicAcrossRestarts(){
        long hash = mapHash();
        AuroraChallenge.load();
        assertEquals(hash, mapHash());
        assertEquals(2, Groups.unit.count(u -> u.type == AuroraContent.glimmer), "Restarts must not duplicate drones.");
        assertEquals(1, spawner.countSpawns());
        assertEquals(1, state.wave);
    }

    @Test
    void normalSaveReloadPreservesChallenge(){
        Fi file = saveDirectory.child("aurora-challenge-test.msav");
        long hash = mapHash();
        state.wave = 12;
        Team.sharded.core().items.set(Items.copper, 1234);
        SaveIO.save(file);
        logic.reset();
        SaveIO.load(file);
        assertEquals(hash, mapHash());
        assertEquals(12, state.wave);
        assertEquals(31, state.rules.winWave);
        assertEquals("1", state.rules.tags.get("aurora-challenge"));
        assertEquals(1234, Team.sharded.core().items.get(Items.copper));
        assertEquals(1, spawner.countSpawns());
        assertSame(world.build(80, 15).power.graph, world.build(86, 59).power.graph);
        assertFalse(state.rules.objectives.any(), "Custom objectives must not hide the live wave counter.");
        file.delete();
    }

    private long mapHash(){
        long hash = 17;
        for(Tile tile : world.tiles){
            hash = hash * 31 + tile.floorID();
            hash = hash * 31 + tile.overlayID();
            hash = hash * 31 + tile.blockID();
            hash = hash * 31 + tile.team().id;
        }
        return hash;
    }
}
