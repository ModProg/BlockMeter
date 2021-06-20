package de.modprog.blockmeter.measurebox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.modprog.blockmeter.util.JSONSource;
import de.modprog.blockmeter.util.parser.ParseBlockPos;
import de.modprog.blockmeter.util.parser.ParseDyeColor;
import de.modprog.blockmeter.util.parser.ParseIdentifier;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.ConfigManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ModConfig;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class ClientMeasureBoxTest {

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class, ParseBlockPos.class,
            ParseIdentifier.class, ParseDyeColor.class,
            Boolean.class }, jsons = {
                    "(1|2|0), (0|0|0), overworld, red, true",
                    "(-1020|30|10), (-1000|100|20), end, blue, false",
                    "(1|2|0), (0|256|0), overworld, red, true",
                    "(1400|2|-6000), (1200|32|-5000), nether, red, true"
            })
    void testPacketByteBuf(final BlockPos bp0, final BlockPos bp1,
            final Identifier dimension,
            final DyeColor color, final boolean finished) {
        final PacketByteBuf expectedBuf = new PacketByteBuf(Unpooled.buffer());
        expectedBuf.writeBlockPos(bp0);
        expectedBuf.writeBlockPos(bp1);
        expectedBuf.writeIdentifier(dimension);
        expectedBuf.writeInt(color.getId());
        expectedBuf.writeBoolean(finished);
        expectedBuf.writeInt(0);
        expectedBuf.writeInt(0);

        final ClientMeasureBox mb = ClientMeasureBox
                .fromPacketByteBuf(expectedBuf);
        final PacketByteBuf actualBuf = new PacketByteBuf(Unpooled.buffer());
        mb.writePacketBuf(actualBuf);

        expectedBuf.resetReaderIndex();
        assertThat(expectedBuf).isEqualTo(actualBuf);
    }

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class,
            ParseIdentifier.class, ParseDyeColor.class,
            Boolean.class }, jsons = {
                    "(1|2|3), overworld, blue, true",
                    "(-10|200|3123), nether, black, false",
                    "(-112312|100|33), end, red, true"
            })
    void testGetMeasureBox(final BlockPos bp, final Identifier id,
            final DyeColor color,
            final boolean incrementColor) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ModConfig config = BlockMeterClient.getConfigManager()
                    .getConfig();
            config.colorIndex = color.getId();
            config.incrementColor = incrementColor;

            final ClientMeasureBox box = ClientMeasureBox.getBox(bp, id);

            assertThat(box.getBlockStart()).isEqualTo(bp);
            assertThat(box.getBlockEnd()).isEqualTo(bp);
            assertThat(box.getDimension()).isEqualTo(id);
            assertThat(box.isFinished()).isFalse();
            assertThat(box.getColor()).isEqualTo(color);
            if (!incrementColor)
                assertThat(DyeColor.byId(config.colorIndex)).isEqualTo(color);
            else
                assertThat(DyeColor.byId(config.colorIndex))
                        .isNotEqualTo(color);
        }
    }

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class,
            ParseBlockPos.class }, jsons = {
                    "(0|0|0), (0|0|0)",
                    "(-10|0|10000), (-500|0|10200)"
            })
    void testSetBlockEnd(final BlockPos bp0, final BlockPos bp1) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {

            final ClientMeasureBox box = ClientMeasureBox.getBox(bp0,
                    DimensionType.OVERWORLD_ID);

            box.setBlockEnd(bp1);

            assertThat(box.getBlockStart()).isEqualTo(bp0);
            assertThat(box.getBlockEnd()).isEqualTo(bp1);
        }
    }

    @Test
    void testFinished() {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(new BlockPos(0, 0, 0), DimensionType.OVERWORLD_ID);

            assertThat(box.isFinished()).isFalse();

            box.setFinished();

            assertThat(box.isFinished()).isTrue();
        }
    }

    @ParameterizedTest
    @JSONSource(classes = { ParseDyeColor.class }, jsons = { "red", "black",
            "green", "blue" })
    void testSetColor(final DyeColor color) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(new BlockPos(0, 0, 0), DimensionType.OVERWORLD_ID);

            box.setColor(color);

            assertThat(box.getColor()).isEqualTo(color);
        }
    }

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class, ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class }, jsons = {
                    "(0|0|0), (10|10|10), (0|0|10), (0|10|0), (0|10|10), (10|0|0), (10|0|10), (10|10|0)",
                    "(-30|5|25), (-90|2|50), (-30|5|50), (-30|2|25), (-30|2|50), (-90|5|25), (-90|5|50), (-90|2|25)"
            })
    void testIsCorner(BlockPos bp0, BlockPos bp1, BlockPos bp2, BlockPos bp3,
            BlockPos bp4, BlockPos bp5, BlockPos bp6, BlockPos bp7) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(bp0, DimensionType.OVERWORLD_ID);

            box.setBlockEnd(bp1);

            assertThat(box.isCorner(bp0));
            assertThat(box.isCorner(bp1));
            assertThat(box.isCorner(bp2));
            assertThat(box.isCorner(bp3));
            assertThat(box.isCorner(bp4));
            assertThat(box.isCorner(bp5));
            assertThat(box.isCorner(bp6));
            assertThat(box.isCorner(bp7));
        }

    }

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class }, jsons = {
                    "(0|1|2), (3|4|5), (0|4|2)"
            })
    void testLoosenCorner(BlockPos bp0, BlockPos bp1, BlockPos newBp0) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(bp0, DimensionType.OVERWORLD_ID);

            box.setBlockEnd(bp1);
            box.setFinished();

            box.loosenCorner(newBp0);

            assertThat(box.isCorner(bp0)).isTrue();
            assertThat(box.isCorner(bp1)).isTrue();
            assertThat(box.getBlockEnd()).isEqualTo(newBp0);
            assertThat(box.isFinished()).isFalse();
        }
    }

    @ParameterizedTest
    @JSONSource(classes = { ParseDyeColor.class }, jsons = {
            "red", "blue", "white", "black"
    })
    void testSetColorIndex(DyeColor color) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            ClientMeasureBox.setColorIndex(color.getId());
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(new BlockPos(0, 0, 0), DimensionType.OVERWORLD_ID);

            assertThat(box.getColor()).isEqualTo(color);
        }
    }

    @SuppressWarnings("unchecked")
    private MockedStatic<BlockMeterClient> getBMC() {
        final MockedStatic<BlockMeterClient> client = Mockito
                .mockStatic(BlockMeterClient.class);
        final ConfigManager<ModConfig> configHolder = Mockito
                .mock(ConfigManager.class);
        when(configHolder.getConfig()).thenReturn(new ModConfig());

        client.when(BlockMeterClient::getConfigManager)
                .thenReturn(configHolder);
        return client;
    }
}
