import arc.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.content.*;
import mindustry.core.GameState.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.units.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** Native-content regression checks; no rendering, audio, or network service is required. */
public class AuroraContentTests{
    @BeforeAll
    static void launch(){
        ApplicationTests.launchApplication(false);
    }

    @BeforeEach
    void resetBefore(){
        Time.setDeltaProvider(() -> 1f);
        Time.delta = 1f;
        logic.reset();
        state.set(State.menu);
    }

    @AfterEach
    void resetAfter(){
        logic.reset();
    }

    @Test
    void nativeContentIsRegisteredAndBuildable(){
        assertSame(AuroraContent.prism, content.block("aurora-prism"));
        assertSame(AuroraContent.halo, content.block("aurora-halo"));
        assertSame(AuroraContent.rime, content.block("aurora-rime"));
        assertSame(AuroraContent.droneFoundry, content.block("aurora-drone-foundry"));
        assertSame(AuroraContent.glimmer, content.unit("aurora-glimmer"));

        for(Block block : new Block[]{AuroraContent.prism, AuroraContent.halo, AuroraContent.rime, AuroraContent.droneFoundry}){
            assertTrue(block.alwaysUnlocked, block.name);
            assertTrue(block.isPlaceable(), block.name);
            assertTrue(block.requirements.length > 0, block.name);
            assertTrue(block.id > Blocks.airFactory.id, "Aurora IDs must follow original block IDs");
        }

        assertTrue(AuroraContent.prism.buildType.get() instanceof PowerTurret.PowerTurretBuild);
        assertTrue(AuroraContent.halo.buildType.get() instanceof ItemTurret.ItemTurretBuild);
        assertTrue(AuroraContent.rime.buildType.get() instanceof ItemTurret.ItemTurretBuild);
        assertTrue(AuroraContent.droneFoundry.buildType.get() instanceof UnitFactory.UnitFactoryBuild);
    }

    @Test
    void englishAndChineseDescriptionsArePresent(){
        var english = I18NBundle.createBundle(Core.files.internal("bundles/bundle"), Locale.ROOT);
        var chinese = I18NBundle.createBundle(Core.files.internal("bundles/bundle"), Locale.SIMPLIFIED_CHINESE);

        for(String key : new String[]{"block.aurora-prism", "block.aurora-halo", "block.aurora-rime", "block.aurora-drone-foundry", "unit.aurora-glimmer"}){
            assertTrue(english.get(key + ".name").startsWith("Aurora"), key);
            assertTrue(chinese.get(key + ".name").startsWith("极光"), key);
            assertFalse(english.get(key + ".description").isEmpty(), key);
            assertFalse(chinese.get(key + ".description").isEmpty(), key);
            assertNotEquals(english.get(key + ".description"), chinese.get(key + ".description"), key);
        }
        assertEquals("极光挑战", chinese.get("aurora.challenge.menu"));
        assertFalse(chinese.get("aurora.challenge.objective").isEmpty());
    }

    @Test
    void turretRolesAndAmmunitionStayDistinct(){
        assertFalse(AuroraContent.prism.targetAir);
        assertTrue(AuroraContent.prism.targetGround);
        var laser = (LaserBulletType)AuroraContent.prism.shootType;
        assertEquals(3, laser.pierceCap);
        assertFalse(laser.collidesAir);
        assertTrue(laser.length >= AuroraContent.prism.range);
        assertEquals(110f, laser.damage);

        assertTrue(AuroraContent.halo.targetAir);
        assertFalse(AuroraContent.halo.targetGround);
        assertEquals(3, AuroraContent.halo.shoot.shots);
        var missile = AuroraContent.halo.ammoTypes.get(Items.silicon);
        assertNotNull(missile);
        assertFalse(missile.collidesGround);
        assertFalse(missile.collidesTiles);
        assertEquals(1f, missile.ammoMultiplier);
        assertTrue(AuroraContent.halo.consumeAmmoOnce);

        assertFalse(AuroraContent.rime.targetAir);
        assertTrue(AuroraContent.rime.minRange > 0);
        var graphite = AuroraContent.rime.ammoTypes.get(Items.graphite);
        var titanium = AuroraContent.rime.ammoTypes.get(Items.titanium);
        assertEquals(48f, graphite.splashDamage);
        assertEquals(70f, titanium.splashDamage);
        assertSame(StatusEffects.slow, graphite.status);
        assertSame(StatusEffects.freezing, titanium.status);
        assertTrue(graphite.despawnHit);
        assertTrue(titanium.despawnHit);
    }

    @Test
    void droneSupportsCombatRepairAndConstruction(){
        assertTrue(AuroraContent.glimmer.create(Team.sharded) instanceof UnitEntity);
        assertTrue(AuroraContent.glimmer.alwaysUnlocked);
        assertTrue(AuroraContent.glimmer.canAttack);
        assertTrue(AuroraContent.glimmer.canHeal);
        assertEquals(2, AuroraContent.glimmer.weapons.size);
        assertEquals(1, AuroraContent.glimmer.abilities.size);
        assertTrue(AuroraContent.glimmer.commands.contains(UnitCommand.repairCommand));
        assertTrue(AuroraContent.glimmer.commands.contains(UnitCommand.rebuildCommand));
        assertTrue(AuroraContent.glimmer.commands.contains(UnitCommand.assistCommand));
    }

    @Test
    void foundryProducesOneDroneAndConsumesExactRecipe(){
        world.loadGenerator(80, 80, tiles -> {
            tiles.fill();
            for(Tile tile : tiles){
                tile.setFloor(Blocks.stone.asFloor());
            }
            tiles.getn(10, 10).setBlock(Blocks.coreShard, Team.sharded);
            tiles.getn(25, 25).setBlock(AuroraContent.droneFoundry, Team.sharded, 0);
        });

        var factory = (UnitFactory.UnitFactoryBuild)world.tile(25, 25).build;
        var plan = AuroraContent.droneFoundry.plans.first();
        assertSame(AuroraContent.glimmer, plan.unit);
        assertEquals(2400f, plan.time);

        factory.currentPlan = 0;
        factory.efficiency = 1f;
        factory.items.add(Items.silicon, 55);
        factory.items.add(Items.titanium, 35);
        factory.items.add(Items.lead, 25);

        // Isolate native production at full efficiency from asynchronous power/pathfinding updates.
        for(int tick = 0; tick < 2399; tick++){
            factory.updateTile();
        }
        assertNull(factory.payload, "Production must not complete early");
        assertEquals(55, factory.items.get(Items.silicon));
        factory.updateTile();

        assertNotNull(factory.payload);
        assertSame(AuroraContent.glimmer, factory.payload.unit.type);
        assertEquals(0, factory.items.get(Items.silicon));
        assertEquals(0, factory.items.get(Items.titanium));
        assertEquals(0, factory.items.get(Items.lead));
    }
}
