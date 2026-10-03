package mindustry.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.blocks.units.*;
import mindustry.world.draw.*;

import static mindustry.type.ItemStack.*;

/** Native Aurora Frontier equipment. Loaded after vanilla content to retain its numeric IDs. */
public class AuroraContent{
    public static final Color aurora = Color.valueOf("70f5ce");
    public static final Color violet = Color.valueOf("bc9bff");
    public static final Color frost = Color.valueOf("91dcff");

    public static PowerTurret prism;
    public static ItemTurret halo, rime;
    public static UnitType glimmer;
    public static UnitFactory droneFoundry;

    private static final Effect prismHit = new Effect(22f, e -> {
        Draw.color(aurora, Color.white, e.fout());
        Lines.stroke(1.8f * e.fout());
        Lines.poly(e.x, e.y, 4, 2f + e.fin() * 10f, 45f);
        Draw.reset();
    });

    private static final Effect haloHit = new Effect(28f, e -> {
        Draw.color(violet);
        Lines.stroke(2f * e.fout());
        Lines.circle(e.x, e.y, 2f + e.fin() * 15f);
        Angles.randLenVectors(e.id, 6, 3f + e.fin() * 19f, (x, y) ->
            Fill.circle(e.x + x, e.y + y, 1.6f * e.fout()));
        Draw.reset();
    });

    private static final Effect rimeHit = new Effect(38f, 90f, e -> {
        Draw.color(frost, Color.white, e.fout());
        Lines.stroke(1.7f * e.fout());
        Lines.poly(e.x, e.y, 6, 4f + e.fin() * 32f, 30f);
        Angles.randLenVectors(e.id, 8, 4f + e.fin() * 30f, (x, y) ->
            Fill.square(e.x + x, e.y + y, 2f * e.fout(), 45f));
        Draw.reset();
    });

    private static final Effect repairPulse = new Effect(42f, 160f, e -> {
        Draw.color(aurora);
        Lines.stroke(1.5f * e.fout());
        Lines.circle(e.x, e.y, e.fin() * 70f);
        Draw.reset();
    });

    public static void load(){
        // A lower-damage, longer-range alternative to Lancer. Cannot attack aircraft.
        prism = new AuroraPowerTurret("aurora-prism", "lancer", aurora){{
            requirements(Category.turret, with(Items.copper, 80, Items.lead, 80, Items.silicon, 75, Items.titanium, 40));
            size = 2;
            health = 980;
            range = 192f;
            reload = 75f;
            recoil = 2f;
            rotateSpeed = 4f;
            targetAir = false;
            shoot.firstShotDelay = 24f;
            moveWhileCharging = false;
            accurateDelay = false;
            shootSound = Sounds.shootLancer;
            chargeSound = Sounds.chargeLancer;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            heatColor = aurora;
            coolant = consumeCoolant(0.15f);
            consumePower(4.5f);

            shootType = new LaserBulletType(110f){{
                colors = new Color[]{aurora.cpy().a(0.35f), aurora, Color.white};
                length = 200f;
                width = 15f;
                lifetime = 18f;
                collidesAir = false;
                pierceCap = 3;
                buildingDamageMultiplier = 0.3f;
                hitEffect = prismHit;
                chargeEffect = Fx.lancerLaserCharge;
                ammoMultiplier = 1f;
            }};
        }};

        // Dedicated anti-air: three guided missiles per silicon, with a small splash radius.
        halo = new AuroraItemTurret("aurora-halo", "swarmer", violet){{
            requirements(Category.turret, with(Items.lead, 100, Items.graphite, 55, Items.silicon, 80, Items.titanium, 50));
            size = 2;
            health = 1050;
            range = 240f;
            reload = 48f;
            rotateSpeed = 7f;
            recoil = 1.5f;
            targetAir = true;
            targetGround = false;
            shoot = new ShootSpread(3, 8f);
            shoot.shotDelay = 4f;
            consumeAmmoOnce = true;
            maxAmmo = 24;
            shootSound = Sounds.shootMissile;
            heatColor = violet;
            coolant = consumeCoolant(0.1f);

            ammo(Items.silicon, new MissileBulletType(4.2f, 20f){{
                ammoMultiplier = 1f;
                width = 7f;
                height = 10f;
                lifetime = 68f;
                collidesGround = false;
                collidesTiles = false;
                homingPower = 0.11f;
                homingRange = 72f;
                splashDamage = 10f;
                splashDamageRadius = 14f;
                frontColor = Color.white;
                backColor = trailColor = hitColor = violet;
                trailLength = 9;
                trailWidth = 1.4f;
                hitEffect = despawnEffect = haloHit;
            }});
        }};

        // Area denial with a dead zone. Titanium trades ammunition cost for freeze synergy.
        rime = new AuroraItemTurret("aurora-rime", "ripple", frost){{
            requirements(Category.turret, with(Items.copper, 130, Items.lead, 110, Items.graphite, 100, Items.titanium, 90, Items.silicon, 60));
            size = 3;
            health = 1650;
            range = 280f;
            minRange = 52f;
            reload = 95f;
            recoil = 3f;
            rotateSpeed = 2.5f;
            inaccuracy = 2f;
            targetAir = false;
            maxAmmo = 30;
            shootSound = Sounds.shootArtillery;
            heatColor = frost;
            coolant = consumeCoolant(0.15f);

            ammo(
                Items.graphite, rimeShell(48f, 26f, StatusEffects.slow, 75f, 3f),
                Items.titanium, rimeShell(70f, 34f, StatusEffects.freezing, 120f, 2f)
            );
            limitRange();
        }};

        glimmer = new AuroraDrone("aurora-glimmer"){{
            constructor = UnitEntity::create;
            flying = true;
            lowAltitude = true;
            speed = 1.8f;
            accel = 0.09f;
            drag = 0.05f;
            rotateSpeed = 10f;
            hitSize = 10f;
            health = 420f;
            armor = 2f;
            engineSize = 2f;
            engineOffset = 6.5f;
            engineColor = aurora;
            range = 150f;
            buildSpeed = 0.5f;
            defaultCommand = UnitCommand.moveCommand;

            abilities.add(new RepairFieldAbility(18f, 240f, 70f){{
                maxTargets = 6;
                sameTypeHealMult = 0.5f;
                activeEffect = repairPulse;
            }});

            weapons.add(new Weapon("poly-weapon"){{
                x = 3.75f;
                y = -2.5f;
                top = false;
                reload = 24f;
                recoil = 1.2f;
                shootSound = Sounds.shootMissilePlasmaShort;

                bullet = new LaserBoltBulletType(4.5f, 14f){{
                    lifetime = 36f;
                    width = 3f;
                    height = 8f;
                    healPercent = 2f;
                    collidesTeam = true;
                    frontColor = Color.white;
                    backColor = trailColor = hitColor = aurora;
                    trailLength = 5;
                    trailWidth = 1f;
                    shootEffect = Fx.shootHeal;
                    hitEffect = despawnEffect = prismHit;
                }};
            }});
        }};

        droneFoundry = new AuroraFoundry("aurora-drone-foundry"){{
            requirements(Category.units, with(Items.copper, 100, Items.lead, 140, Items.silicon, 110, Items.titanium, 80));
            size = 3;
            health = 1450;
            consumePower(2f);
            plans = Seq.with(new UnitPlan(glimmer, 60f * 40f, with(Items.silicon, 55, Items.titanium, 35, Items.lead, 25)));
        }};
    }

    private static ArtilleryBulletType rimeShell(float splash, float radius, StatusEffect effect, float duration, float ammo){
        return new ArtilleryBulletType(3.2f, 0f){{
            width = height = 12f;
            splashDamage = splash;
            splashDamageRadius = radius;
            status = effect;
            statusDuration = duration;
            ammoMultiplier = ammo;
            backColor = trailColor = hitColor = frost;
            frontColor = Color.white;
            trailLength = 10;
            trailWidth = 2f;
            trailEffect = Fx.none;
            hitEffect = despawnEffect = rimeHit;
        }};
    }

    private static void fallbackIcons(UnlockableContent content, UnlockableContent original){
        if(!content.fullIcon.found()) content.fullIcon = original.fullIcon;
        if(!content.uiIcon.found()) content.uiIcon = original.uiIcon;
    }

    /** Reuses the shipped atlas; no runtime downloads or missing mod sprites are required. */
    public static class AuroraTurretDraw extends DrawTurret{
        private final String fallback;
        private final Color accent;

        public AuroraTurretDraw(String fallback, Color accent){
            this.fallback = fallback;
            this.accent = accent;
        }

        @Override
        public void load(Block block){
            block.region = Core.atlas.find(block.name, Core.atlas.find(fallback));
            super.load(block);
            heat = Core.atlas.find(block.name + "-heat", Core.atlas.find(fallback + "-heat"));
            top = Core.atlas.find(block.name + "-top", Core.atlas.find(fallback + "-top"));
            outline = Core.atlas.find(block.name + "-outline", Core.atlas.find(fallback + "-outline"));
        }

        @Override
        public void drawTurret(Turret block, TurretBuild build){
            Draw.color(Color.white, accent, 0.24f);
            super.drawTurret(block, build);
            Draw.reset();

            // An animated crystal identifies Aurora equipment even while it is idle.
            float x = build.x + build.recoilOffset.x, y = build.y + build.recoilOffset.y;
            Draw.color(accent);
            Lines.stroke(0.7f + build.heat * 0.6f);
            Lines.poly(x, y, 4, block.size * 1.75f, build.rotation + 45f);
            Fill.square(x, y, 1.15f + Mathf.absin(Time.time, 7f, 0.35f), 45f);
            Draw.reset();
        }
    }

    public static class AuroraPowerTurret extends PowerTurret{
        private final String fallback;

        public AuroraPowerTurret(String name, String fallback, Color accent){
            super(name);
            this.fallback = fallback;
            alwaysUnlocked = true;
            generateIcons = false;
            drawer = new AuroraTurretDraw(fallback, accent);
        }

        @Override
        public void loadIcon(){
            super.loadIcon();
            fallbackIcons(this, mindustry.Vars.content.block(fallback));
        }
    }

    public static class AuroraItemTurret extends ItemTurret{
        private final String fallback;

        public AuroraItemTurret(String name, String fallback, Color accent){
            super(name);
            this.fallback = fallback;
            alwaysUnlocked = true;
            generateIcons = false;
            drawer = new AuroraTurretDraw(fallback, accent);
        }

        @Override
        public void loadIcon(){
            super.loadIcon();
            fallbackIcons(this, mindustry.Vars.content.block(fallback));
        }
    }

    public static class AuroraDrone extends UnitType{
        public AuroraDrone(String name){
            super(name);
            alwaysUnlocked = true;
            generateIcons = false;
        }

        @Override
        public void loadIcon(){
            super.loadIcon();
            fallbackIcons(this, UnitTypes.poly);
        }

        @Override
        public void load(){
            super.load();
            region = Core.atlas.find(name, UnitTypes.poly.region);
            previewRegion = Core.atlas.find(name + "-preview", region);
            outlineRegion = Core.atlas.find(name + "-outline", UnitTypes.poly.outlineRegion);
            cellRegion = Core.atlas.find(name + "-cell", UnitTypes.poly.cellRegion);
            for(int i = 0; i < wreckRegions.length; i++){
                wreckRegions[i] = Core.atlas.find(name + "-wreck" + i, UnitTypes.poly.wreckRegions[i]);
            }
        }

        @Override
        public void drawBody(Unit unit){
            super.drawBody(unit);
            Draw.color(aurora);
            Lines.stroke(0.8f);
            Lines.poly(unit.x, unit.y, 4, 4.5f, unit.rotation);
            Draw.reset();
        }
    }

    public static class AuroraFoundry extends UnitFactory{
        public AuroraFoundry(String name){
            super(name);
            alwaysUnlocked = true;
            generateIcons = false;
            buildType = AuroraFoundryBuild::new;
        }

        @Override
        public void loadIcon(){
            super.loadIcon();
            fallbackIcons(this, Blocks.airFactory);
        }

        @Override
        public void load(){
            super.load();
            region = Core.atlas.find(name, Blocks.airFactory.region);
            topRegion = Core.atlas.find(name + "-top", ((UnitFactory)Blocks.airFactory).topRegion);
        }

        public class AuroraFoundryBuild extends UnitFactoryBuild{
            @Override
            public void draw(){
                super.draw();
                Draw.color(aurora);
                Lines.stroke(1f);
                Lines.square(x, y, 9.5f);
                for(int i = 0; i < 4; i++){
                    float angle = 45f + i * 90f;
                    Fill.square(x + Angles.trnsx(angle, 12f), y + Angles.trnsy(angle, 12f), 1f + speedScl * 0.5f, 45f);
                }
                Draw.reset();
            }
        }
    }
}
