package de.modprog.blockmeter.measurebox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

public class ClientMeasureBoxTest {

    @ParameterizedTest
    @ArgumentsSource(MeasureBoxData.class)
    void testPacketByteBuf(MeasureBoxData data) {
        final PacketByteBuf expectedBuf = new PacketByteBuf(Unpooled.buffer());
        expectedBuf.writeBlockPos(data.bp1);
        expectedBuf.writeBlockPos(data.bp2);
        expectedBuf.writeIdentifier(data.identifier);
        expectedBuf.writeInt(data.color.getId());
        expectedBuf.writeBoolean(data.finished);
        expectedBuf.writeInt(data.mode);
        expectedBuf.writeInt(data.orientation);

        final ClientMeasureBox mb = ClientMeasureBox.fromPacketByteBuf(expectedBuf);
        final PacketByteBuf actualBuf = new PacketByteBuf(Unpooled.buffer());
        mb.writePacketBuf(actualBuf);
        
        expectedBuf.resetReaderIndex();
        assertThat(expectedBuf).usingRecursiveComparison().isEqualTo(actualBuf);
    }

}