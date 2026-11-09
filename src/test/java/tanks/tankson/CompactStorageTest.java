package tanks.tankson;

import basewindow.BaseFile;
import basewindow.ComputerFileManager;
import tanks.Game;
import tanks.Team;
import tanks.network.NetworkUtils;
import tanks.network.event.EventSetItem;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class CompactStorageTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setup()
    {
        Game.game.fileManager = new TanksFileManager(() -> Game.reducedDiskSpace);
    }

    @After
    public void reset()
    {
        Game.reducedDiskSpace = false;
    }

    public static String objectText()
    {
        return "/*TANKSON v1.1*/{\"obj_type\":\"team\",\"name\":\"猫 🐢\",\"friendly_fire\":false," +
            "\"enable_color\":true,\"team_color\":[12,34,56,78]}";
    }

    @Test
    public void mixedDocumentPreservesSectionsAndObjectValues()
    {
        String document = "level\n{28,18||}\ntanks\n[0,1]" + objectText() + "\nshop\n" + objectText() + "\n";
        byte[] bytes = TanksONDocument.encode(document);
        String decoded = TanksONDocument.decode(bytes);
        assertTrue(decoded.startsWith("level\n{28,18||}\ntanks\n[0,1]/*TANKSON v1.1*/"));
        assertTrue(decoded.contains("\nshop\n/*TANKSON v1.1*/"));
        assertTrue(decoded.endsWith("\n"));
        assertArrayEquals(bytes, TanksONDocument.encode(decoded));
        String object = decoded.substring(decoded.indexOf("/*TANKSON"));
        assertEquals(TanksON.parseObject(objectText()), TanksON.parseObject(object));
        assertNull(TanksONDocument.encode("{28,18||}"));
        assertNull(TanksONDocument.encode(objectText().replace("v1.1", "v9.9")));
        String legacy = objectText().replace("/*TANKSON v1.1*/", "");
        String restored = TanksONDocument.decode(TanksONDocument.encode(legacy));
        assertFalse(restored.contains("TANKSON"));
        assertEquals(TanksON.parseObject(legacy), TanksON.parseObject(restored));
    }

    @Test
    public void directObjectBufferRoundTripRestoresTypedColor()
    {
        Team original = new Team("猫", false, 12, 34, 56);
        original.teamColor.alpha = 78;
        Team copy = (Team) Serializer.fromObjBuf(Serializer.toObjBuf(original));
        assertEquals(original.name, copy.name);
        assertEquals(original.friendlyFire, copy.friendlyFire);
        assertEquals(original.teamColor.red, copy.teamColor.red, 0);
        assertEquals(original.teamColor.alpha, copy.teamColor.alpha, 0);
    }

    @Test
    public void annotatedNetworkPayloadUsesObjectBuffersAndLiteralStringsStayLiteral()
    {
        EventSetItem original = new EventSetItem();
        original.itemStackString = objectText();
        original.playerID = UUID.randomUUID();
        original.slot = 2;
        ByteBuf buffer = Unpooled.buffer();
        try
        {
            NetworkUtils.writeTanksON(buffer, objectText());
            assertEquals(-2, buffer.getInt(0));
            assertEquals(TanksON.parseObject(objectText()), TanksON.parseObject(NetworkUtils.readTanksON(buffer)));
            original.write(buffer);
            EventSetItem copy = new EventSetItem();
            copy.read(buffer);
            assertEquals(original.playerID, copy.playerID);
            assertEquals(original.slot, copy.slot);
            assertEquals(TanksON.parseObject(original.itemStackString), TanksON.parseObject(copy.itemStackString));
            assertFalse(buffer.isReadable());

            NetworkUtils.writeString(buffer, objectText());
            assertEquals(objectText(), NetworkUtils.readString(buffer));
        }
        finally
        {
            buffer.release();
        }
    }

    @Test
    public void compactSavesAreDetectedWithTheOptionOffAndCanBeSavedAsTextAgain() throws Exception
    {
        File path = folder.newFile("team.tanks");
        BaseFile save = Game.game.fileManager.getFile(path.toString());
        Game.reducedDiskSpace = true;
        save.startWriting();
        save.println(objectText());
        save.stopWriting();
        byte[] bytes = Files.readAllBytes(path.toPath());
        assertEquals("TKOB", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
        assertEquals(0, bytes[5]); // no disk compression
        assertTrue(bytes.length < objectText().getBytes(StandardCharsets.UTF_8).length);

        Game.reducedDiskSpace = false;
        save = Game.game.fileManager.getFile(path.toString());
        save.startReading();
        assertTrue(save.hasNextLine());
        String restored = save.nextLine();
        assertEquals(TanksON.parseObject(objectText()), TanksON.parseObject(restored));
        assertFalse(save.hasNextLine());
        save.stopReading();
        assertEquals(restored + "\n", save.read());
        save.startWriting();
        save.println(restored);
        save.stopWriting();
        assertTrue(new String(Files.readAllBytes(path.toPath()), StandardCharsets.UTF_8).startsWith("/*TANKSON"));
        assertEquals(restored + "\n", save.read());
    }

    @Test
    public void incompressiblePlainSavesAndOtherFileTypesStayText() throws Exception
    {
        Game.reducedDiskSpace = true;
        for (String name: Arrays.asList("plain.tanks", "options.txt"))
        {
            File path = folder.newFile(name);
            BaseFile save = Game.game.fileManager.getFile(path.toString());
            save.startWriting();
            save.println("hello 猫");
            save.stopWriting();
            assertEquals("hello 猫\n", save.read());
            assertTrue(new String(Files.readAllBytes(path.toPath()), StandardCharsets.UTF_8).startsWith("hello"));
        }
    }

    @Test
    public void rejectsUnsupportedDiskCompression() throws Exception
    {
        File path = folder.newFile("future.tanks");
        Files.write(path.toPath(), new byte[]{'T', 'K', 'O', 'B', 1, 1});
        BaseFile save = Game.game.fileManager.getFile(path.toString());
        assertThrows(RuntimeException.class, save::read);
    }

    @Test
    public void savePolicyCanChangeWithoutGlobalSettings() throws Exception
    {
        AtomicBoolean compact = new AtomicBoolean(true);
        TanksFileManager manager = new TanksFileManager(compact::get);
        File path = folder.newFile("independent.tanks");
        BaseFile save = manager.getFile(path.toString());
        assertFalse(new ComputerFileManager().getFile(path.toString()) instanceof TanksFile);
        save.startWriting();
        save.println(objectText());
        save.stopWriting();
        assertEquals('T', Files.readAllBytes(path.toPath())[0]);

        compact.set(false);
        save.startWriting();
        save.println(objectText());
        save.stopWriting();
        assertEquals('/', Files.readAllBytes(path.toPath())[0]);
    }

    @Test
    public void containerCodecReadsEitherFormatFromStreams() throws Exception
    {
        for (boolean compact: new boolean[]{false, true})
        {
            byte[] encoded = TanksSaveCodec.encode(objectText(), compact);
            try (BufferedReader reader = TanksSaveCodec.openReader(new ByteArrayInputStream(encoded)))
            {
                assertEquals(TanksON.parseObject(objectText()), TanksON.parseObject(reader.readLine()));
                assertNull(reader.readLine());
            }
        }
        assertThrows(IOException.class, () -> TanksSaveCodec.openReader(new ByteArrayInputStream(new byte[]{'T', 'K', 'O', 'B'})));
    }

    @Test
    public void rejectsOverflowingVarintsAndPreservesLongExtremes()
    {
        for (long value: new long[]{Long.MIN_VALUE, Long.MAX_VALUE, -1, 0, 1})
            assertEquals(value, ObjectBuffer.parse(ObjectBuffer.toBytes(value)));

        byte[] overflow = new byte[10];
        Arrays.fill(overflow, (byte) 0x80);
        overflow[9] = 2;
        assertThrows(RuntimeException.class, () -> new ObjectBuffer.Reader(overflow).readVarLong());
        overflow[9] = 1;
        assertThrows(RuntimeException.class, () -> new ObjectBuffer.Reader(overflow).readVarInt());

        byte[] previousFormat = ObjectBuffer.toBytes("previous save");
        previousFormat[0] = 1;
        assertEquals("previous save", ObjectBuffer.parse(previousFormat));
        previousFormat[0] = 2;
        assertEquals("previous save", ObjectBuffer.parse(previousFormat));
        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) ObjectBuffer.parse(ObjectBuffer.toBytes(new byte[]{1, 2, 3})));
        assertArrayEquals(new float[]{1.5f, -2.5f}, (float[]) ObjectBuffer.parse(ObjectBuffer.toBytes(new float[]{1.5f, -2.5f})), 0);
    }

    @Test
    public void networkFormatKeepsExtensionPropertyIds()
    {
        Map<String, Object> object = new HashMap<>();
        object.put("obj_type", "team");
        object.put("extension_test_property_123", "value");
        byte[] encoded = ObjectBuffer.toNetworkBytes(object);
        assertEquals(ObjectBuffer.VERSION, encoded[0]); // full header carries the unknown property's name
        assertEquals(object, ObjectBuffer.parseNetworkBytes(encoded));

        byte[] compact = ObjectBuffer.toNetworkBytes("known value");
        assertEquals(0, compact[0]);
        assertEquals("known value", ObjectBuffer.parseNetworkBytes(compact));
    }
}
