package tanks.network;

import tanks.Game;
import tanks.gui.screen.ScreenArcadeBonuses;
import tanks.network.event.*;
import tanks.tankson.ObjectBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class EventObjectBufferTest
{
    @BeforeClass
    public static void registerEvents()
    {
        Game.debug = false;
        Game.registerEvents();
    }

    @Test
    public void everyRegisteredEventHasAnObjectBufferRoundTrip() throws Exception
    {
        for (Class<? extends INetworkEvent> type: NetworkEventMap.map1.values())
        {
            INetworkEvent event = type.getConstructor().newInstance();
            byte[] bytes = EventObjectBuffer.encode(event);
            List<?> values = (List<?>) ObjectBuffer.parseNetworkBytes(bytes);
            assertEquals(NetworkEventMap.get(type), values.get(0));
            ByteBuf body = Unpooled.wrappedBuffer(bytes);
            try
            {
                assertEquals(type, EventObjectBuffer.decode(body).getClass());
                assertFalse(body.isReadable());
            }
            finally
            {
                body.release();
            }
        }
    }

    @Test
    public void customEventValuesRoundTripAsTypedFields()
    {
        EventArcadeBonuses bonuses = new EventArcadeBonuses(
            new ScreenArcadeBonuses.Bonus("first", 1, 1.5, 2.5, 3.5),
            new ScreenArcadeBonuses.Bonus("second", 2, 4.5, 5.5, 6.5),
            new ScreenArcadeBonuses.Bonus("third", 3, 7.5, 8.5, 9.5));
        EventArcadeBonuses copy = roundTrip(bonuses);
        assertEquals("first", copy.bonus1.name);
        assertEquals(2, copy.bonus2.value);
        assertEquals(9.5, copy.bonus3.blue, 0);

        EventTankTransformCustom transform = new EventTankTransformCustom();
        transform.tankMusic = new HashSet<>(Arrays.asList("track1", "track2"));
        transform.color.red = 37;
        EventTankTransformCustom restored = roundTrip(transform);
        assertEquals(transform.tankMusic, restored.tankMusic);
        assertEquals(37, restored.color.red, 0);
    }

    @Test
    public void tankUpdatesPackTheirFloatFields()
    {
        EventTankUpdate update = new EventTankUpdate();
        update.angle = 1.125;
        update.pitch = 2.25;
        update.posX = 3.5;
        update.posY = 4.75;
        update.vX = 5.875;
        update.vY = 6.125;
        update.tank = 42;
        update.time = 123456789L;
        byte[] encoded = EventObjectBuffer.encode(update);
        assertEquals(0, encoded[0]); // network protocol supplies the shared version and property table
        List<?> values = (List<?>) ObjectBuffer.parseNetworkBytes(encoded);
        assertTrue(values.get(1) instanceof float[]);
        assertArrayEquals(new float[]{1.125f, 2.25f, 3.5f, 4.75f, 5.875f, 6.125f},
            (float[]) values.get(1), 0);
        assertEquals(42, values.get(2));
        assertEquals(123456789L, values.get(3));
        EventTankUpdate restored = roundTrip(update);
        assertEquals(update.angle, restored.angle, 0);
        assertEquals(update.pitch, restored.pitch, 0);
        assertEquals(update.posX, restored.posX, 0);
        assertEquals(update.posY, restored.posY, 0);
        assertEquals(update.vX, restored.vX, 0);
        assertEquals(update.vY, restored.vY, 0);
        assertEquals(update.tank, restored.tank);
        assertEquals(update.time, restored.time);
    }

    @SuppressWarnings("unchecked")
    private static <T extends INetworkEvent> T roundTrip(T event)
    {
        ByteBuf body = Unpooled.wrappedBuffer(EventObjectBuffer.encode(event));
        try
        {
            return (T) EventObjectBuffer.decode(body);
        }
        finally
        {
            body.release();
        }
    }
}
