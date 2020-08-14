package de.modprog.blockmeter.measurebox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.modprog.blockmeter.measurebox.util.CsvToMBD;
import de.modprog.blockmeter.measurebox.util.MeasureBoxData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import win.baruna.blockmeter.measurebox.MeasureBox;

public class MeasureBoxTest {

    @ParameterizedTest
    @CsvSource({ "1|2|0, 0|0|0, overworld, red, finished" })
    public void testPacketByteBuf(@CsvToMBD MeasureBoxData data) {

        final PacketByteBuf expectedBuf = new PacketByteBuf(Unpooled.buffer());
        expectedBuf.writeBlockPos(data.bp1);
        expectedBuf.writeBlockPos(data.bp2);
        expectedBuf.writeIdentifier(data.dimension);
        expectedBuf.writeInt(data.color.getId());
        expectedBuf.writeBoolean(data.finished);
        expectedBuf.writeInt(data.mode);
        expectedBuf.writeInt(data.orientation);

        final MeasureBox mb = MeasureBox.fromPacketByteBuf(expectedBuf);
        final PacketByteBuf actualBuf = new PacketByteBuf(Unpooled.buffer());
        mb.writePacketBuf(actualBuf);

        expectedBuf.resetReaderIndex();
        assertThat(expectedBuf).isEqualTo(actualBuf);
    }

}
