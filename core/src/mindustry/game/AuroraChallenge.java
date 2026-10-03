package mindustry.game;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;

/** A self-contained, save-compatible survival scenario using the normal game simulation. */
public final class AuroraChallenge{
    public static final int size = 192, waves = 30, seed = 160503;
    public static final int coreX = 96, coreY = 28, spawnX = 96, spawnY = 174;
    private static boolean launching;

    private AuroraChallenge(){}

    /** The mission brief is shown before starting, so Cancel never changes an existing world. */
    public static void show(){
        BaseDialog dialog = new BaseDialog("@aurora.challenge.title");
        dialog.cont.pane(table -> {
            table.margin(20f);
            float width = Math.min(600f, Core.graphics.getWidth() / arc.scene.ui.layout.Scl.scl(1f) - 60f);
            table.add("@aurora.challenge.description").width(width).wrap().left().row();
            table.add("@aurora.challenge.hint").width(width).wrap().left().padTop(24f);
        }).grow();
        dialog.addCloseButton();
        dialog.buttons.button("@aurora.challenge.start", Icon.play, () -> {
            if(launching) return;
            launching = true;
            dialog.hide();
            ui.loadAnd(() -> {
                try{
                    load();
                    if(Core.settings.getBool("savecreate")){
                        control.saves.addSave(Core.bundle.get("aurora.challenge.save") + " " + new java.text.SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date()));
                    }
                    Events.fire(Trigger.newGame);
                    ui.showInfoFade(Core.bundle.get("aurora.challenge.objective"), 8f);
                }catch(Throwable error){
                    Log.err(error);
                    logic.reset();
                    ui.showException(error);
                }finally{
                    launching = false;
                }
            });
        }).size(210f, 64f);
        dialog.show();
    }

    /** Also used by headless tests: no UI, graphics, network, or external files are required. */
    public static void load(){
        logic.reset();
        state.rules = rules();
        state.map = new Map(StringMap.of(
            "name", Core.bundle.get("aurora.challenge.title", "Aurora Frontier: Frozen Pass"),
            "author", "Aurora Frontier", "description", Core.bundle.get("aurora.challenge.description", "Defend the core through 30 waves."),
            "aurora-challenge", "1", "seed", String.valueOf(seed)
        ));
        state.map.width = state.map.height = size;
        state.map.spawns = 1;
        state.map.teams.add(Team.sharded.id);
        world.loadGenerator(size, size, AuroraChallenge::generate);
        connectPower();
        Events.fire(new RulesLoadEvent(state.rules));
        logic.play();
        AuroraContent.glimmer.spawn(Team.sharded, (coreX - 5) * tilesize, (coreY + 5) * tilesize);
        AuroraContent.glimmer.spawn(Team.sharded, (coreX + 5) * tilesize, (coreY + 5) * tilesize);
    }

    public static Rules rules(){
        Rules rules = new Rules();
        rules.waves = rules.waveTimer = rules.waveSending = true;
        rules.waitEnemies = true;
        rules.airUseSpawns = true;
        rules.hideSpawns = false;
        // runWave() increments state.wave as soon as enemies spawn. 31 means defeat wave 30.
        rules.winWave = waves + 1;
        rules.waveSpacing = 60f * 55f;
        rules.initialWaveSpacing = 60f * 150f;
        rules.dropZoneRadius = 12f * tilesize;
        rules.buildSpeedMultiplier = 1.35f;
        rules.unitCap = 24;
        rules.modeName = Core.bundle.get("aurora.challenge.menu", "Aurora Challenge");
        rules.tags.put("aurora-challenge", "1");
        rules.loadout = ItemStack.list(
            Items.copper, 1800, Items.lead, 1400, Items.graphite, 700,
            Items.silicon, 650, Items.titanium, 500, Items.metaglass, 200,
            Items.plastanium, 100, Items.thorium, 100
        );
        rules.spawns.add(group(UnitTypes.dagger, 0, 29, 1, 3, 2f, 17));
        rules.spawns.add(group(UnitTypes.crawler, 4, 28, 2, 2, 2f, 8));
        rules.spawns.add(group(UnitTypes.flare, 8, 29, 3, 2, 2f, 5));
        rules.spawns.add(group(UnitTypes.mace, 9, 29, 2, 1, 3f, 4));
        rules.spawns.add(group(UnitTypes.pulsar, 14, 29, 5, 1, 3f, 2));
        rules.spawns.add(group(UnitTypes.horizon, 17, 29, 4, 2, 3f, 3));
        rules.spawns.add(group(UnitTypes.fortress, 19, 28, 5, 1, 3f, 2));
        rules.spawns.add(group(UnitTypes.zenith, 24, 29, 5, 1, SpawnGroup.never, 1));
        SpawnGroup guardian = group(UnitTypes.scepter, 29, 29, 1, 1, SpawnGroup.never, 1);
        guardian.effect = StatusEffects.boss;
        rules.spawns.add(guardian);
        return rules;
    }

    private static SpawnGroup group(UnitType type, int begin, int end, int spacing, int amount, float scaling, int max){
        SpawnGroup group = new SpawnGroup(type);
        group.begin = begin;
        group.end = end;
        group.spacing = spacing;
        group.unitAmount = amount;
        group.unitScaling = scaling;
        group.max = max;
        return group;
    }

    /** A fixed-seed mountain pass with a guaranteed, unobstructed ground route to the base. */
    public static void generate(Tiles tiles){
        for(int y = 0; y < size; y++){
            for(int x = 0; x < size; x++){
                float detail = Simplex.noise2d(seed, 3, 0.55, 1.0 / 35.0, x, y);
                float center = 96f + Mathf.sin(y / 22f) * 11f;
                float halfWidth = y < 78 ? 49f : 20f + Mathf.sin(y / 17f) * 5f;
                boolean open = Math.abs(x - center) < halfWidth || within(x, y, coreX, coreY, 35f)
                    || within(x, y, spawnX, spawnY, 25f) || within(x, y, 52, 77, 19f) || within(x, y, 142, 89, 19f);
                boolean edge = x < 5 || y < 5 || x >= size - 5 || y >= size - 5;
                Block floor = detail > 0.57f ? Blocks.iceSnow : detail < 0.36f ? Blocks.ice : Blocks.snow;
                Block wall = !open || edge ? (detail > 0.51f ? Blocks.iceWall : Blocks.snowWall) : Blocks.air;
                if(!edge && open && y < 140 && (within(x, y, 43, 52, 10f) || within(x, y, 148, 46, 12f))){
                    floor = Blocks.water;
                    wall = Blocks.air;
                }
                tiles.set(x, y, new Tile(x, y, floor, Blocks.air, wall));
            }
        }

        // Resource shelves are connected to the central valley, without blocking the attack route.
        clear(tiles, 45, 67, 85, 83);
        clear(tiles, 114, 79, 147, 92);
        patch(tiles, 58, 47, 12, Blocks.sand, null);
        patch(tiles, 134, 56, 10, Blocks.sand, null);
        patch(tiles, 82, 28, 7, null, Blocks.oreCopper);
        patch(tiles, 96, 17, 6, null, Blocks.oreLead);
        patch(tiles, 68, 52, 8, null, Blocks.oreCoal);
        patch(tiles, 125, 39, 8, null, Blocks.oreLead);
        patch(tiles, 66, 73, 8, null, Blocks.oreTitanium);
        patch(tiles, 135, 85, 8, null, Blocks.oreTitanium);
        patch(tiles, 145, 94, 5, null, Blocks.oreThorium);
        patch(tiles, 76, 116, 6, null, Blocks.oreCopper);
        patch(tiles, 117, 129, 7, null, Blocks.oreCoal);
        // A straight central lane is always passable regardless of terrain noise.
        clear(tiles, 91, 63, 101, spawnY + 3);
        tiles.getn(spawnX, spawnY).setOverlay(Blocks.spawn);

        place(tiles, coreX, coreY, Blocks.coreNucleus);
        place(tiles, 82, 28, Blocks.mechanicalDrill);
        line(tiles, 84, 28, 93, 28, 0);
        place(tiles, 96, 17, Blocks.mechanicalDrill);
        line(tiles, 96, 19, 96, 25, 1);

        // Usable finite supplies: unload from the real core, with conventional belts to the turrets.
        place(tiles, 96, 31, Blocks.unloader).configureAny(Items.graphite);
        line(tiles, 96, 32, 96, 55, 1);
        place(tiles, 99, 28, Blocks.unloader).configureAny(Items.silicon);
        line(tiles, 100, 28, 106, 28, 0);
        line(tiles, 106, 28, 106, 58, 1);
        place(tiles, 86, 59, AuroraContent.prism);
        supply(place(tiles, 106, 59, AuroraContent.halo), Items.silicon, 30);
        supply(place(tiles, 96, 57, AuroraContent.rime), Items.graphite, 30);
        supply(place(tiles, 85, 50, Blocks.duo), Items.copper, 30);
        supply(place(tiles, 111, 50, Blocks.duo), Items.copper, 30);
        supply(place(tiles, 114, 57, Blocks.scatter), Items.lead, 30);
        place(tiles, 89, 55, Blocks.mender);
        place(tiles, 110, 55, Blocks.mender);
        for(int x = 77; x <= 89; x++) place(tiles, x, 63, Blocks.copperWall);
        for(int x = 104; x <= 118; x++) place(tiles, x, 63, Blocks.copperWall);

        // Eight solar panels provide 12.8 power/tick, leaving expansion headroom without fuel cheats.
        for(int x = 80; x <= 92; x += 4){
            place(tiles, x, 15, Blocks.largeSolarPanel);
            place(tiles, x, 19, Blocks.largeSolarPanel);
        }
        place(tiles, 82, 23, Blocks.batteryLarge);
        for(int[] position : powerNodes()) place(tiles, position[0], position[1], Blocks.powerNodeLarge);
        Building factory = place(tiles, 117, 32, AuroraContent.droneFoundry);
        factory.items.add(Items.silicon, 110);
        factory.items.add(Items.titanium, 70);
        factory.items.add(Items.lead, 50);
    }

    private static int[][] powerNodes(){
        return new int[][]{{86, 23}, {99, 23}, {112, 23}, {112, 36}, {112, 49}, {99, 49}, {86, 49}, {86, 36}};
    }

    private static void connectPower(){
        int[][] nodes = powerNodes();
        for(int i = 1; i < nodes.length; i++) link(nodes[i - 1][0], nodes[i - 1][1], nodes[i][0], nodes[i][1]);
        for(int x = 80; x <= 92; x += 4){
            link(86, 23, x, 15);
            link(86, 23, x, 19);
        }
        link(86, 23, 82, 23);
        link(86, 49, 86, 59);
        link(86, 49, 89, 55);
        link(112, 49, 110, 55);
        link(112, 36, 117, 32);
    }

    private static void link(int x, int y, int targetX, int targetY){
        Building node = world.build(x, y), target = world.build(targetX, targetY);
        if(node == null || target == null || !(node.block instanceof PowerNode)) throw new IllegalStateException("Invalid starter power link");
        if(!node.power.links.contains(target.pos())) node.configureAny(target.pos());
    }

    private static Building place(Tiles tiles, int x, int y, Block block){
        int offset = -(block.size - 1) / 2;
        clear(tiles, x + offset, y + offset, x + offset + block.size - 1, y + offset + block.size - 1);
        tiles.getn(x, y).setBlock(block, Team.sharded);
        return tiles.getn(x, y).build;
    }

    private static void line(Tiles tiles, int x1, int y1, int x2, int y2, int rotation){
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                place(tiles, x, y, Blocks.conveyor).rotation = rotation;
            }
        }
    }

    private static void supply(Building building, Item item, int amount){
        ItemTurretBuild turret = (ItemTurretBuild)building;
        turret.handleStack(item, turret.acceptStack(item, amount, null), null);
    }

    private static void clear(Tiles tiles, int x1, int y1, int x2, int y2){
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                Tile tile = tiles.getn(x, y);
                tile.setBlock(Blocks.air);
                if(tile.floor().isLiquid) tile.setFloor(Blocks.snow.asFloor());
            }
        }
    }

    private static void patch(Tiles tiles, int cx, int cy, int radius, Block floor, Block ore){
        for(int x = cx - radius; x <= cx + radius; x++){
            for(int y = cy - radius; y <= cy + radius; y++){
                if(!within(x, y, cx, cy, radius - Simplex.noise2d(seed + 1, 2, 0.5, 0.2, x, y) * 2f)) continue;
                Tile tile = tiles.getn(x, y);
                tile.setBlock(Blocks.air);
                tile.setFloor((floor == null ? Blocks.snow : floor).asFloor());
                if(ore != null) tile.setOverlay(ore);
            }
        }
    }

    private static boolean within(float x, float y, float cx, float cy, float radius){
        return Mathf.dst2(x, y, cx, cy) < radius * radius;
    }
}
