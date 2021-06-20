package de.modprog.blockmeter.measurebox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;

import de.modprog.blockmeter.util.JSONSource;
import de.modprog.blockmeter.util.parser.ParseBlockPos;
import de.modprog.blockmeter.util.parser.ParseDyeColor;
import de.modprog.blockmeter.util.parser.ParseIdentifier;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.measurebox.MeasureBox;

public class MeasureBoxTest {

    @ParameterizedTest
    @JSONSource(classes = { ParseBlockPos.class, ParseBlockPos.class,
            ParseIdentifier.class, ParseDyeColor.class,
            Boolean.class }, jsons = {
                    "(11|2|-50), (0|0|0), overworld, red, true",
                    "(10020|45|130), (10000|99|203), end, blue, false",
                    "(17|0|40), (40|256|70), overworld, red, true",
                    "(-1455|2|-6000), (-1455|32|-5000), overworld, red, true" })
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

        final MeasureBox mb = MeasureBox
                .fromPacketByteBuf(expectedBuf);
        final PacketByteBuf actualBuf = new PacketByteBuf(Unpooled.buffer());
        mb.writePacketBuf(actualBuf);

        expectedBuf.resetReaderIndex();
        assertThat(expectedBuf).isEqualTo(actualBuf);
    }

}
