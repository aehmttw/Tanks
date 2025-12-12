package tanks.minigames.controlpoint;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.minigames.Minigame;
import tanks.network.event.EventAirdropTank;
import tanks.network.event.EventTankUpdateHealth;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.*;
import java.util.concurrent.*;

public class ControlPoint extends Minigame
{
    private final List<GamePoint> points = new CopyOnWriteArrayList<>();
    private int gameTimeSecond = 0;
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private Team teamRed = null;
    private Team teamBlue = null;

    private final Map<Player, Long> playerDeathTimes = new ConcurrentHashMap<>();
    private final Map<Player, Team> playerTeams = new ConcurrentHashMap<>();

    private Random random = new Random();

    private static final Map<Integer, String> COLOR_TO_POINT = new ConcurrentHashMap<>();

    static
    {
        COLOR_TO_POINT.put(0xFF0000FF, "A"); // Red
        COLOR_TO_POINT.put(0xCC3333FF, "A"); // Dark Red
        COLOR_TO_POINT.put(0x0000FFFF, "B"); // Blue
        COLOR_TO_POINT.put(0x007BFFFF, "B"); // Light Blue
        COLOR_TO_POINT.put(0xFFFF00FF, "C"); // Yellow
        COLOR_TO_POINT.put(0xFFA700FF, "C"); // Orange
        COLOR_TO_POINT.put(0x00FF00FF, "D"); // Green
        COLOR_TO_POINT.put(0x008000FF, "D"); // Dark Green
    }

    public ControlPoint()
    {
        super("{80,35,235.0,207.0,166.0,20.0,20.0,20.0,0,100,50|6...8-13-normal-3.0,6...8-18-normal-3.0,8-14...17-normal-3.0,13-25...28-normal-3.0,14...15-25-normal-3.0,22...24-25-normal-3.0,24-26...28-normal-3.0,30-15...18-normal-3.0,39-0...7-normal-3.0,39-26...34-normal-3.0,48-15...18-normal-3.0,54-0...2-normal-3.0,65-0...2-normal-3.0,72-13...18-normal-3.0,73...74-13-normal-3.0,73...74-18-normal-3.0,0...5-8-hard-3.0,0...5-23-hard-3.0,5-9...13-hard-3.0,5-18...22-hard-3.0,13-29...34-hard-3.0,16...21-25-hard-3.0,24-29...34-hard-3.0,30...48-8-hard-3.0,30-9...14-hard-3.0,30-19...25-hard-3.0,31...48-25-hard-3.0,48-9...14-hard-3.0,48-19...24-hard-3.0,54-3...8-hard-3.0,55...65-8-hard-3.0,65-3...7-hard-3.0,75-8...13-hard-3.0,75-18...23-hard-3.0,76...79-8-hard-3.0,76...79-23-hard-3.0,14...23-26-paint-cc3333,14...23-27-paint-cc3333,14...23-28-paint-cc3333,14...23-29-paint-cc3333,14...23-30-paint-cc3333,14...23-31-paint-cc3333,14...23-32-paint-cc3333,14...23-33-paint-cc3333,14...23-34-paint-cc3333,31...47-9-paint-ffa700,31...47-10-paint-ffa700,31...47-11-paint-ffa700,31...47-12-paint-ffa700,31...47-13-paint-ffa700,31...47-14-paint-ffa700,31...47-15-paint-ffa700,31...47-16-paint-ffa700,31...47-17-paint-ffa700,31...47-18-paint-ffa700,31...47-19-paint-ffa700,31...47-20-paint-ffa700,31...47-21-paint-ffa700,31...47-22-paint-ffa700,31...47-23-paint-ffa700,31...47-24-paint-ffa700,55...64-0-paint-7bff,55-1...7-paint-7bff,56...63-1-paint-78ff,56...63-2-paint-78ff,56...63-3-paint-78ff,56...63-4-paint-78ff,56...63-5-paint-78ff,56...63-6-paint-78ff,56...64-7-paint-7bff,64-1...6-paint-7bff|2-16-player-0-red,2-15-player-0-red,2-14-player-0-red,2-17-player-0-red,77-14-player-2-blue,77-15-player-2-blue,77-16-player-2-blue,77-17-player-2-blue|ally-true,enemy-true,red-false-255.0-0.0-0.0,blue-false-0.0-120.0-255.0}\n" +
            "coins\n" +
            "50\n" +
            "shop\n" +
            "{\"obj_type\":\"shop_item\",\"stack\":{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Rocket\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_fire\",\"colors\":[[127.0,127.0,127.0,100.0],[0.0,0.0,0.0,0.0],[0.0,0.0,0.0,0.0],[0.0,0.0,0.0,0.0],[255.0,0.0,0.0],[255.0,255.0,0.0],[255.0,0.0,0.0],[255.0,220.0,0.0]]},\"cooldown\":75.0,\"bullet\":{\"obj_type\":\"bullet\",\"override_color\":true,\"color2\":[0.0,0.0,0.0],\"max_live_bullets\":3,\"bullet_type\":\"bullet\",\"color\":[255.0,162.0,0.0],\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":true,\"glow_color\":[255.0,180.0,0.0,0.0],\"particle_lifespan\":0.25,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"front_color\":[127.0,127.0,127.0,100.0]},{\"obj_type\":\"trail\",\"back_color\":[255.0,0.0,0.0,0.0],\"length\":5.0,\"luminosity\":1.0,\"front_color\":[255.0,0.0,0.0],\"back_width\":5.0}],\"glow_size\":16.0,\"particle_color\":[255.0,180.0,64.0,0.0],\"particle_speed\":12.0,\"glow_glowy\":true,\"particles\":true,\"glow_intensity\":0.8},\"destroy_blocks\":true,\"bounces\":0,\"speed\":6.25}},\"amount\":3,\"max\":100},\"price\":8}\n" +
            "{\"obj_type\":\"shop_item\",\"stack\":{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Shield\",\"max_extra_health\":5.0,\"item_type\":\"shield\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"shield\",\"colors\":[[170.0,170.0,170.0],[230.0,230.0,230.0],[170.0,110.0,50.0]]},\"cooldown\":50.0,\"health_boost\":1.0},\"amount\":1,\"max\":10},\"price\":20}\n" +
            "{\"obj_type\":\"shop_item\",\"stack\":{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Mega Bullet\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_large\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0]]},\"cooldown\":100.0,\"bullet\":{\"obj_type\":\"bullet\",\"color2\":[0.0,0.0,0.0],\"sound_pitch\":0.4,\"bullet_type\":\"bullet\",\"size\":25.0,\"color\":[0.0,0.0,0.0],\"recoil\":4.0,\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"front_color\":[127.0,127.0,127.0,100.0]}],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0},\"bounces\":3,\"heavy\":true}},\"amount\":2,\"max\":20},\"price\":5}\n" +
            "{\"obj_type\":\"shop_item\",\"stack\":{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Mini Bullet\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_mini\",\"colors\":[[0.0,0.0,0.0],[0.0,0.0,0.0]]},\"cooldown\":5.0,\"bullet\":{\"obj_type\":\"bullet\",\"color2\":[0.0,0.0,0.0],\"damage\":0.125,\"max_live_bullets\":8,\"sound_pitch\":2.0,\"bullet_type\":\"bullet\",\"size\":5.0,\"color\":[0.0,0.0,0.0],\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"front_color\":[127.0,127.0,127.0,100.0]}],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0},\"bounces\":0,\"speed\":6.25}},\"amount\":20,\"max\":500},\"price\":3}\n" +
            "{\"obj_type\":\"shop_item\",\"stack\":{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Block\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_block\",\"colors\":[[127.0,127.0,127.0,100.0],[32.0,107.0,159.0],[0.0,150.0,255.0]]},\"cooldown\":100.0,\"bullet\":{\"obj_type\":\"bullet\",\"color2\":[0.0,0.0,0.0],\"max_live_bullets\":1,\"max_range\":300.0,\"bullet_type\":\"block\",\"size\":25.0,\"color\":[0.0,0.0,0.0],\"recoil\":0.0,\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"length\":22.5,\"front_color\":[127.0,127.0,127.0,100.0]}],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0},\"sound\":\"plop.ogg\",\"bounces\":0}},\"amount\":5,\"max\":20},\"price\":10}\n" +
            "items\n" +
            "{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Shield\",\"max_extra_health\":5.0,\"item_type\":\"shield\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"shield\",\"colors\":[[170.0,170.0,170.0],[230.0,230.0,230.0],[170.0,110.0,50.0]]},\"cooldown\":50.0,\"health_boost\":1.0},\"amount\":1,\"max\":10}\n" +
            "builds\n" +
            "{\"obj_type\":\"shop_build\",\"abilities\":[{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Basic bullet\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_normal\",\"colors\":[[127.0,127.0,127.0,100.0],[32.0,107.0,159.0],[0.0,150.0,255.0]]},\"cooldown\":20.0,\"bullet\":{\"obj_type\":\"bullet\",\"color2\":[0.0,0.0,0.0],\"bullet_type\":\"bullet\",\"color\":[0.0,0.0,0.0],\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"front_color\":[127.0,127.0,127.0,100.0]}],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0}}},\"amount\":0,\"max\":0},{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Basic mine\",\"mine\":{\"obj_type\":\"mine\",\"explosion\":{\"obj_type\":\"explosion\"},\"color2\":[255.0,0.0,0.0],\"color\":[255.0,255.0,0.0]},\"item_type\":\"mine\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"mine\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0],[255.0,220.0,0.0]]},\"cooldown\":50.0},\"amount\":0,\"max\":0}],\"color2\":[32.0,107.0,159.0],\"color\":[0.0,150.0,255.0],\"emblem_color\":[32.0,107.0,159.0],\"color3\":[16.0,128.5,207.0]}\n" +
            "{\"obj_type\":\"shop_build\",\"name\":\"cyan\",\"override_color3\":true,\"override_color2\":true,\"override_color1\":true,\"override_color_emblem\":true,\"color\":[128.0,255.0,255.0],\"description\":\"A support tank which shoots freezing bullets that deal low damage\",\"max_speed\":0.75,\"emblem_color\":[160.0,255.0,255.0],\"color3\":[112.0,207.25,207.25],\"abilities\":[{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Freezing bullet\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_freeze\",\"colors\":[[0.0,255.0,255.0],[255.0,255.0,255.0],[0.0,255.0,255.0]]},\"cooldown\":240.0,\"bullet\":{\"obj_type\":\"bullet\",\"override_color2\":true,\"color2\":[255.0,255.0,255.0],\"damage\":0.25,\"max_live_bullets\":1,\"bullet_type\":\"bullet\",\"color\":[0.0,0.0,0.0],\"freezing\":true,\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.125,\"trails\":[],\"glow_size\":4.0,\"particle_color\":[128.0,255.0,255.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":true,\"glow_intensity\":1.0},\"bounces\":0}},\"amount\":0,\"max\":0},{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Base Mine\",\"mine\":{\"obj_type\":\"mine\",\"explosion\":{\"obj_type\":\"explosion\"},\"color2\":[255.0,0.0,0.0],\"color\":[255.0,255.0,0.0]},\"item_type\":\"mine\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"mine\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0],[255.0,220.0,0.0]]},\"cooldown\":50.0},\"amount\":0,\"max\":0}],\"color2\":[96.0,159.5,159.5],\"emblem\":\"emblems/snowflake.png\",\"resist_freezing\":true}\n" +
            "{\"obj_type\":\"shop_build\",\"name\":\"medic\",\"override_color3\":true,\"override_color2\":true,\"override_color1\":true,\"override_color_emblem\":true,\"color\":[255.0,255.0,255.0],\"description\":\"A tank which adds extra health to its allies and becomes explosive as a last stand\",\"max_speed\":0.75,\"emblem_color\":[0.0,200.0,0.0],\"color3\":[207.25,207.25,207.25],\"abilities\":[{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Healing ray\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_healing\",\"colors\":[[0.0,255.0,0.0],[120.0,255.0,120.0],[160.0,255.0,160.0],[0.0,255.0,0.0]]},\"cooldown\":0.0,\"bullet\":{\"obj_type\":\"bullet\",\"override_color\":true,\"override_color2\":true,\"damage\":-0.01,\"bullet_type\":\"laser\",\"color\":[0.0,255.0,0.0],\"recoil\":0.0,\"collide_bullets\":false,\"color2\":[200.0,255.0,200.0],\"max_live_bullets\":1,\"sound_volume\":0.0,\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0},\"bounces\":0}},\"amount\":0,\"max\":0},{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"基础地雷\",\"mine\":{\"obj_type\":\"mine\",\"explosion\":{\"obj_type\":\"explosion\"},\"color2\":[255.0,0.0,0.0],\"color\":[255.0,255.0,0.0]},\"item_type\":\"mine\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"mine\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0],[255.0,220.0,0.0]]},\"cooldown\":50.0},\"amount\":0,\"max\":0}],\"color2\":[159.5,159.5,159.5],\"emblem\":\"emblems/medic.png\"}\n" +
            "{\"obj_type\":\"shop_build\",\"name\":\"fast_tank\",\"override_color2\":true,\"override_color1\":true,\"color\":[0.0,150.0,255.0],\"description\":\"Fast and small tank\",\"max_speed\":2.0,\"emblem_color\":[32.0,107.0,159.0],\"color3\":[16.0,128.5,207.0],\"abilities\":[{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Basic bullet\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_mini\",\"colors\":[[0.0,0.0,0.0],[0.0,0.0,0.0]]},\"cooldown\":20.0,\"bullet\":{\"obj_type\":\"bullet\",\"color2\":[0.0,0.0,0.0],\"bullet_type\":\"bullet\",\"color\":[0.0,0.0,0.0],\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"front_color\":[127.0,127.0,127.0,100.0]}],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0}}},\"amount\":0,\"max\":0},{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Basic mine\",\"mine\":{\"obj_type\":\"mine\",\"explosion\":{\"obj_type\":\"explosion\"},\"color2\":[255.0,0.0,0.0],\"color\":[255.0,255.0,0.0]},\"item_type\":\"mine\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"mine\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0],[255.0,220.0,0.0]]},\"cooldown\":50.0},\"amount\":0,\"max\":0}],\"color2\":[32.0,107.0,159.0],\"size\":30.0,\"price\":50}\n" +
            "{\"obj_type\":\"shop_build\",\"name\":\"mustard\",\"override_color3\":true,\"override_color2\":true,\"override_color1\":true,\"override_color_emblem\":true,\"color\":[180.0,160.0,0.0],\"color_skin\":\"tank_fixed\",\"description\":\"A stationary tank which lobs bullets over walls\",\"max_speed\":0.0,\"emblem_color\":[0.0,0.0,0.0],\"friction\":0.2,\"color3\":[151.0,136.0,16.0],\"abilities\":[{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Artillery shell\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_arc\",\"colors\":[[127.0,127.0,127.0,100.0],[32.0,107.0,159.0],[0.0,150.0,255.0]]},\"cooldown\":200.0,\"bullet\":{\"obj_type\":\"bullet\",\"color2\":[0.0,0.0,0.0],\"max_range\":1000.0,\"sound_pitch\":0.4,\"bullet_type\":\"artillery\",\"size\":25.0,\"color\":[0.0,0.0,0.0],\"recoil\":0.0,\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[{\"obj_type\":\"trail\",\"back_color\":[127.0,127.0,127.0,0.0],\"length\":22.5,\"front_color\":[127.0,127.0,127.0,100.0]}],\"glow_size\":4.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":1.0},\"sound\":\"arc.ogg\",\"bounces\":0}},\"amount\":0,\"max\":0},{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"基础地雷\",\"mine\":{\"obj_type\":\"mine\",\"explosion\":{\"obj_type\":\"explosion\"},\"color2\":[255.0,0.0,0.0],\"color\":[255.0,255.0,0.0]},\"item_type\":\"mine\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"mine\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0],[255.0,220.0,0.0]]},\"cooldown\":50.0},\"amount\":0,\"max\":0}],\"color2\":[122.0,112.0,32.0],\"turret_size\":14.0}\n" +
            "{\"obj_type\":\"shop_build\",\"name\":\"lightblue\",\"override_color3\":true,\"override_color2\":true,\"override_color1\":true,\"override_color_emblem\":true,\"enable_color3\":false,\"color\":[200.0,220.0,255.0],\"description\":\"A tank which blows strong gusts of air\",\"max_speed\":1.0,\"emblem_color\":[132.0,142.0,159.5],\"color3\":[166.0,181.0,207.25],\"abilities\":[{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"Air\",\"item_type\":\"bullet\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"bullet_air\",\"colors\":[[200.0,200.0,200.0],[200.0,200.0,255.0]]},\"cooldown\":0.0,\"bullet\":{\"obj_type\":\"bullet\",\"override_color\":true,\"override_color2\":true,\"damage\":0.0,\"bullet_type\":\"gas\",\"color\":[160.0,179.0,191.0],\"recoil\":0.0,\"lifespan\":200.0,\"accuracy_spread_angle\":20.0,\"sound\":\"wind.ogg\",\"collide_mines\":false,\"speed\":6.25,\"heavy\":true,\"color_noise\":[95.0,76.0,64.0],\"color2\":[100.0,131.0,151.0],\"knockback_tank\":0.1,\"max_live_bullets\":0,\"effect\":{\"obj_type\":\"bullet_effect\",\"luminance\":0.5,\"glow_color_override\":false,\"glow_color\":[0.0,0.0,0.0,0.0],\"particle_lifespan\":0.5,\"particle_glow\":0.5,\"trails\":[],\"glow_size\":0.0,\"particle_color\":[0.0,0.0,0.0,0.0],\"particle_speed\":4.0,\"glow_glowy\":true,\"particles\":false,\"glow_intensity\":0.0},\"bounces\":0,\"end_size\":400.0,\"opacity\":0.16666666666666666,\"knockback_bullet\":0.04,\"bush_lower\":false,\"sound_pitch_variation\":1.0}},\"amount\":0,\"max\":0},{\"obj_type\":\"item_stack\",\"item\":{\"obj_type\":\"item\",\"name\":\"基础地雷\",\"mine\":{\"obj_type\":\"mine\",\"explosion\":{\"obj_type\":\"explosion\"},\"color2\":[255.0,0.0,0.0],\"color\":[255.0,255.0,0.0]},\"item_type\":\"mine\",\"icon\":{\"obj_type\":\"item_icon\",\"id\":\"mine\",\"colors\":[[32.0,107.0,159.0],[0.0,150.0,255.0],[255.0,220.0,0.0]]},\"cooldown\":50.0},\"amount\":0,\"max\":0}],\"color2\":[132.0,142.0,159.5],\"emblem\":\"emblems/pinwheel.png\"}");

        this.disableFriendlyFire = true;
        this.showItems = true;
        this.hideSpeedrunTimer = true;
        this.enableTeams = true;
        this.customLevelEnd = true;
        this.enableKillMessages = false;
        this.noLose = true;

        if (Game.deterministicMode)
            this.random = new Random(0);
    }

    @Override
    public void setUp()
    {
        super.setUp();

        // teamRed and teamBlue is already assignment in customTeamAssignment

        List<GamePoint> detectedPoints = scanAndCreateGamePoints();
        points.addAll(detectedPoints);

        for (Movable movable : Game.movables)
        {
            if (movable instanceof IServerPlayerTank && movable instanceof Tank)
            {
                IServerPlayerTank playerTank = (IServerPlayerTank) movable;
                playerTeams.put(playerTank.getPlayer(), movable.team);
            }
        }

        scheduledExecutorService.scheduleAtFixedRate(() ->
        {
            gameTimeSecond++;
        }, 0L, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onKill(Tank attacker, Tank target)
    {
        if (target instanceof IServerPlayerTank)
        {
            IServerPlayerTank playerTank = (IServerPlayerTank) target;
            playerDeathTimes.put(playerTank.getPlayer(), System.currentTimeMillis());
        }
    }

    @Override
    public void update()
    {
        for (Movable movable : Game.movables)
        {
            if (movable instanceof Tank)
            {
                Tank tank = (Tank) movable;
                for (GamePoint gamePoint : points)
                {
                    if (gamePoint.checkTankInPointRange(tank))
                    {
                        gamePoint.addTank(tank);
                    } else
                    {
                        gamePoint.removeTank(tank);
                    }
                }
            }
        }

        List<Player> needRespawn = new ArrayList<>();

        for (Map.Entry<Player, Long> entry : playerDeathTimes.entrySet())
        {
            Player player = entry.getKey();
            long deathTime = entry.getValue();

            if (System.currentTimeMillis() - deathTime >= 10000)
            {
                boolean isAlive = false;
                for (Movable m : Game.movables)
                {
                    if (m instanceof TankPlayer)
                    {
                        TankPlayer tp = (TankPlayer) m;
                        if (tp.player.equals(player) && !tp.destroy)
                        {
                            isAlive = true;
                            break;
                        }
                    } else if (m instanceof TankPlayerBot)
                    {
                        TankPlayerBot tpb = (TankPlayerBot) m;
                        if (tpb.player.equals(player) && !tpb.destroy)
                        {
                            isAlive = true;
                            break;
                        }
                    }
                }

                if (!isAlive)
                {
                    needRespawn.add(player);
                }
            }
        }

        for (Player player : needRespawn)
        {
            Team team = playerTeams.get(player);
            if (team != null)
            {
                respawnPlayer(player, team);
            }
        }

        for (GamePoint gamePoint : points)
        {
            gamePoint.update();
        }

        if (((ScreenGame) Game.screen).playing)
        {
            ((ScreenGame) Game.screen).showRankings = false;
            ((ScreenGame) Game.screen).isVersus = false;
        }
    }

    public void respawnPlayer(Player player, Team team)
    {
        playerDeathTimes.remove(player);

        List<Integer> teamSpawnIndices = new ArrayList<>();
        for (int i = 0; i < this.playerSpawnsTeam.size(); i++)
        {
            if (this.playerSpawnsTeam.get(i).equals(team))
            {
                teamSpawnIndices.add(i);
            }
        }

        if (teamSpawnIndices.isEmpty())
        {
            for (int i = 0; i < this.playerSpawnsTeam.size(); i++)
            {
                teamSpawnIndices.add(i);
            }
        }

        int spawnIndex = teamSpawnIndices.get(random.nextInt(teamSpawnIndices.size()));
        double x = this.playerSpawnsX.get(spawnIndex);
        double y = this.playerSpawnsY.get(spawnIndex);
        double angle = this.playerSpawnsAngle.get(spawnIndex);

        TankPlayer t = new TankPlayer(x, y, angle);
        t.team = team;
        t.player = player;
        t.color.set(player.color);
        t.secondaryColor.set(player.color2);
        t.tertiaryColor.set(player.color3);

        double h = random.nextDouble() * 400 + 800;
        Game.movables.add(new Crate(t, h));
        Game.eventsOut.add(new EventAirdropTank(t, h));
    }

    @Override
    public void draw()
    {
        super.draw();

        if (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).paused && ((ScreenGame) Game.screen).screenshotMode)
            return;

        double barWidth = 260.0;
        double barHeight = 22.0;
        double markerWidth = 6.0;
        double markerHeight = barHeight + 8.0;

        for (GamePoint p : points)
        {
            double cx = p.getCenterX();
            double cy = p.getCenterY();

            Drawing.drawing.setColor(255, 255, 255, 255);
            double textX = Drawing.drawing.gameToInterfaceCoordsX(cx);
            double textY = Drawing.drawing.gameToInterfaceCoordsY(cy) - 30;
            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.drawInterfaceText(textX, textY, p.getId());

            Drawing.drawing.setColor(0, 0, 0, 200);
            Drawing.drawing.drawRect(cx, cy, barWidth + 4, barHeight + 4);

            Drawing.drawing.setColor(120, 120, 120, 160);
            Drawing.drawing.fillRect(cx, cy, barWidth, barHeight);

            double progress = p.getCaptureProgress(); // -1 .. 1
            double halfW = barWidth / 2.0;

            if (progress < 0.0)
            {
                double redSize = -progress * halfW;
                double redCenterX = cx - redSize / 2.0;
                Drawing.drawing.setColor(255, 60, 60, 220);
                Drawing.drawing.fillRect(redCenterX, cy, redSize, barHeight);
            }

            if (progress > 0.0)
            {
                double blueSize = progress * halfW;
                double blueCenterX = cx + blueSize / 2.0;
                Drawing.drawing.setColor(30, 140, 255, 220);
                Drawing.drawing.fillRect(blueCenterX, cy, blueSize, barHeight);
            }

            Drawing.drawing.setColor(0, 0, 0, 120);
            Drawing.drawing.fillRect(cx, cy, 2.0, barHeight + 2);

            double markerX = cx + progress * halfW;
            Drawing.drawing.setColor(250, 250, 250, 230);
            Drawing.drawing.fillRect(markerX, cy, markerWidth, markerHeight);
            Drawing.drawing.setColor(0, 0, 0, 140);
            Drawing.drawing.drawRect(markerX, cy, markerWidth, markerHeight);

            if (p.getControllingTeam() != null)
            {
                String msg;

                Drawing.drawing.setInterfaceFontSize(16);

                if (p.getControllingTeam().name.equals("red"))
                {
                    Drawing.drawing.setColor(255, 160, 160);
                    msg = "RED CONTROLLED";
                }
                else
                {
                    Drawing.drawing.setColor(160, 200, 255);
                    msg = "BLUE CONTROLLED";
                }

                double msgWidth = Drawing.drawing.getStringWidth(msg);

                double msgX = cx - msgWidth / 2.0;
                double msgY = cy + (barHeight / 2.0) + 10.0 / Drawing.drawing.scale;

                Drawing.drawing.drawText(msgX, msgY, msg);
            }
        }
    }

    @Override
    public boolean levelEnded()
    {
        if (!ScreenPartyHost.isServer) return false;

        boolean allRedControlled = true;
        boolean allBlueControlled = true;

        for (GamePoint gamePoint : points)
        {
            if (gamePoint.getControllingTeam() == null)
            {
                allRedControlled = false;
                allBlueControlled = false;
                break;
            }

            if (gamePoint.getControllingTeam().equals(teamRed))
            {
                allBlueControlled = false;
            } else
            {
                allRedControlled = false;
            }

            if (!allRedControlled && !allBlueControlled)
            {
                break;
            }
        }

        if (allRedControlled)
        {
            for (Movable movable : Game.movables)
            {
                if (movable instanceof Tank)
                {
                    Tank tank = (Tank) movable;
                    if (!tank.team.equals(teamRed))
                    {
                        tank.health = 0;
                        Game.eventsOut.add(new EventTankUpdateHealth(tank));
                    }
                }
            }
            return true;
        } else if (allBlueControlled)
        {
            for (Movable movable : Game.movables)
            {
                if (movable instanceof Tank)
                {
                    Tank tank = (Tank) movable;
                    if (!tank.team.equals(teamBlue))
                    {
                        tank.health = 0;
                        Game.eventsOut.add(new EventTankUpdateHealth(tank));
                    }
                }
            }
            return true;
        }

        int maxPlayerCount = 0;
        for (Player p : playerTeams.keySet())
        {
            if (Game.players.contains(p))
                maxPlayerCount++;
        }

        if (gameTimeSecond >= Math.min(1500, (300 * maxPlayerCount)))
        {
            int bluePointCount = 0;
            int redPointCount = 0;

            for (GamePoint gamePoint : points)
            {
                if (gamePoint.getControllingTeam() != null)
                {
                    if (gamePoint.getControllingTeam().equals(teamRed))
                        redPointCount++;
                    else if (gamePoint.getControllingTeam().equals(teamBlue))
                        bluePointCount++;
                }
            }

            if (bluePointCount == redPointCount)
            {
                return true;
            } else if (bluePointCount > redPointCount)
            {
                for (Movable movable : Game.movables)
                {
                    if (movable instanceof Tank && movable.team.equals(teamRed)) {
                        Tank tank = (Tank) movable;
                        tank.health = 0;
                        Game.eventsOut.add(new EventTankUpdateHealth(tank));
                    }
                }
                return true;
            } else
            {
                for (Movable movable : Game.movables)
                {
                    if (movable instanceof Tank && movable.team.equals(teamBlue)) {
                        Tank tank = (Tank) movable;
                        tank.health = 0;
                        Game.eventsOut.add(new EventTankUpdateHealth(tank));
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onLevelEnd(boolean levelWon)
    {
        super.onLevelEnd(levelWon);

        for (GamePoint gamePoint : points)
        {
            gamePoint.clear();
        }

        scheduledExecutorService.shutdownNow();
        points.clear();
        playerDeathTimes.clear();
        playerTeams.clear();
    }

    private static int gameCount = 0;

    @Override
    public Team customTeamAssignment(int playerIndex, int totalPlayers, Team defaultTeam) {
        if (teamRed == null || teamBlue == null) {
            teamRed = teamsMap.get("red");
            teamBlue = teamsMap.get("blue");
        }

        if (teamRed == null || teamBlue == null) {
            return defaultTeam;
        }

        if (playerIndex == 0) {
            gameCount++;
        }

        boolean useEvenRed = (gameCount % 2 == 0);

        boolean isEvenIndex = (playerIndex % 2 == 0);

        if (useEvenRed) {
            return isEvenIndex ? teamRed : teamBlue;
        } else {
            return isEvenIndex ? teamBlue : teamRed;
        }
    }

    private List<GamePoint> scanAndCreateGamePoints()
    {
        List<GamePoint> detectedPoints = new ArrayList<>();
        List<Obstacle> allObstacles = Game.obstacles;

        if (allObstacles.isEmpty())
            return detectedPoints;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Obstacle obstacle : allObstacles)
        {
            int gridX = (int) (obstacle.posX / Game.tile_size);
            int gridY = (int) (obstacle.posY / Game.tile_size);

            minX = Math.min(minX, gridX);
            minY = Math.min(minY, gridY);
            maxX = Math.max(maxX, gridX);
            maxY = Math.max(maxY, gridY);
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        Obstacle[][] obstacleGrid = new Obstacle[width][height];
        boolean[][] visited = new boolean[width][height];

        for (Obstacle obstacle : allObstacles)
        {
            int gridX = (int) (obstacle.posX / Game.tile_size) - minX;
            int gridY = (int) (obstacle.posY / Game.tile_size) - minY;

            if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height)
            {
                obstacleGrid[gridX][gridY] = obstacle;
            }
        }

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (!visited[x][y] && obstacleGrid[x][y] != null)
                {
                    Obstacle startObstacle = obstacleGrid[x][y];
                    int colorCode = getColorCode(startObstacle);

                    if (COLOR_TO_POINT.containsKey(colorCode))
                    {
                        FloodFillResult result = floodFill(obstacleGrid, visited, x, y, width, height, colorCode, minX, minY);

                        if (result != null && result.count >= 1)
                        {
                            String teamId = COLOR_TO_POINT.get(colorCode);
                            String pointId = teamId + "_" + (detectedPoints.size() + 1);

                            double widthInPixels = (result.maxX - result.minX + 1) * Game.tile_size;
                            double heightInPixels = (result.maxY - result.minY + 1) * Game.tile_size;
                            double centerXPixels = (result.minX + (result.maxX - result.minX) / 2.0) * Game.tile_size;
                            double centerYPixels = (result.minY + (result.maxY - result.minY) / 2.0) * Game.tile_size;

                            GamePoint gamePoint = new GamePoint(pointId, widthInPixels, heightInPixels, centerXPixels, centerYPixels, teamRed, teamBlue);
                            detectedPoints.add(gamePoint);
                        }
                    }
                }
            }
        }

        return detectedPoints;
    }

    private FloodFillResult floodFill(Obstacle[][] grid, boolean[][] visited, int startX, int startY,
                                      int width, int height, int targetColor, int offsetX, int offsetY)
    {
        List<int[]> stack = new ArrayList<>();
        stack.add(new int[]{startX, startY});

        int minGridX = startX;
        int minGridY = startY;
        int maxGridX = startX;
        int maxGridY = startY;
        int count = 0;

        while (!stack.isEmpty())
        {
            int[] pos = stack.remove(stack.size() - 1);
            int x = pos[0];
            int y = pos[1];

            if (x < 0 || x >= width || y < 0 || y >= height)
                continue;

            if (visited[x][y] || grid[x][y] == null)
                continue;

            if (getColorCode(grid[x][y]) != targetColor)
                continue;

            visited[x][y] = true;
            count++;

            minGridX = Math.min(minGridX, x);
            minGridY = Math.min(minGridY, y);
            maxGridX = Math.max(maxGridX, x);
            maxGridY = Math.max(maxGridY, y);

            stack.add(new int[]{x + 1, y});
            stack.add(new int[]{x - 1, y});
            stack.add(new int[]{x, y + 1});
            stack.add(new int[]{x, y - 1});
        }

        if (count == 0)
            return null;

        return new FloodFillResult(minGridX + offsetX, minGridY + offsetY, maxGridX + offsetX, maxGridY + offsetY, count);
    }

    private int getColorCode(Obstacle obstacle)
    {
        int r = (int) obstacle.colorR;
        int g = (int) obstacle.colorG;
        int b = (int) obstacle.colorB;
        int a = (int) obstacle.colorA;

        return ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
    }

    private static class FloodFillResult
    {
        final int minX;
        final int minY;
        final int maxX;
        final int maxY;
        final int count;

        FloodFillResult(int minX, int minY, int maxX, int maxY, int count)
        {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.count = count;
        }
    }

    public List<GamePoint> getPoints()
    {
        return points;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        ControlPoint that = (ControlPoint) o;
        return gameTimeSecond == that.gameTimeSecond && Objects.equals(points, that.points) && Objects.equals(scheduledExecutorService, that.scheduledExecutorService) && Objects.equals(teamRed, that.teamRed) && Objects.equals(teamBlue, that.teamBlue) && Objects.equals(playerDeathTimes, that.playerDeathTimes) && Objects.equals(playerTeams, that.playerTeams) && Objects.equals(random, that.random);
    }

    @Override
    public String toString()
    {
        return "ControlPoint{" +
            "points=" + points +
            ", gameTimeSecond=" + gameTimeSecond +
            ", scheduledExecutorService=" + scheduledExecutorService +
            ", teamRed=" + teamRed +
            ", teamBlue=" + teamBlue +
            ", playerDeathTimes=" + playerDeathTimes +
            ", playerTeams=" + playerTeams +
            ", random=" + random +
            '}';
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(points, gameTimeSecond, scheduledExecutorService, teamRed, teamBlue, playerDeathTimes, playerTeams, random);
    }
}
