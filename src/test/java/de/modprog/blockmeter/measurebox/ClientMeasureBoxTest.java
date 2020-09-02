package de.modprog.blockmeter.measurebox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.modprog.blockmeter.measurebox.util.JSONSource;
import de.modprog.blockmeter.measurebox.util.parser.ParseBlockPos;
import de.modprog.blockmeter.measurebox.util.parser.ParseDyeColor;
import de.modprog.blockmeter.measurebox.util.parser.ParseIdentifier;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class ClientMeasureBoxTest {

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class, ParseBlockPos.class,
            ParseIdentifier.class, ParseDyeColor.class,
            Boolean.class }, jsons = {
                    "(1|2|0), (0|0|0), overworld, red, true",
                    "(-1020|30|10), (-1000|100|20), end, blue, false",
                    "(1|2|0), (0|256|0), overworld, red, true",
                    "(1400|2|-6000), (1200|32|-5000), overworld, red, true" })
    void testPacketByteBuf(BlockPos bp1, BlockPos bp2, Identifier dimension,
            DyeColor color, boolean finished) {
        final PacketByteBuf expectedBuf = new PacketByteBuf(Unpooled.buffer());
        expectedBuf.writeBlockPos(bp1);
        expectedBuf.writeBlockPos(bp2);
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
    @CsvSource({ "true" })
    void testGetMeasureBox(final boolean incrementColor,
            final boolean incrementColor2) {
        // try (MockedStatic<BlockMeterClient> client = Mockito
        // .mockStatic(BlockMeterClient.class)) {

        // final ConfigManager<ModConfig> configHolder = Mockito
        // .mock(ConfigManager.class);
        // when(configHolder.getConfig()).thenReturn(new ModConfig());

        // client.when(BlockMeterClient::getConfigManager)
        // .thenReturn(configHolder);

        // configHolder.getConfig().colorIndex = data.color.getId();
        // configHolder.getConfig().incrementColor = incrementColor;

        // final ClientMeasureBox box = ClientMeasureBox.getBox(data.bp1,
        // data.dimension);

        // assert(box.getBlockStart()).equals(data.bp1);
        // }

    }
}
