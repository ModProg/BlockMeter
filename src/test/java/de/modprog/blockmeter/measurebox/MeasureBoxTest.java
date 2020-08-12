package de.modprog.blockmeter.measurebox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import win.baruna.blockmeter.measurebox.MeasureBox;

public class MeasureBoxTest {

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

        final MeasureBox mb = MeasureBox.fromPacketByteBuf(expectedBuf);
        final PacketByteBuf actualBuf = new PacketByteBuf(Unpooled.buffer());
        mb.writePacketBuf(actualBuf);

        expectedBuf.resetReaderIndex();
        assertThat(expectedBuf).isEqualTo(actualBuf);
    }

}

class MeasureBoxData implements ArgumentsProvider, Arguments {
    BlockPos bp1;
    BlockPos bp2;
    Identifier identifier;
    DyeColor color;
    boolean finished;
    int mode;
    int orientation;

    MeasureBoxData() {};

    private MeasureBoxData(BlockPos bp1, BlockPos bp2, Identifier identifier,
            DyeColor color, boolean finished, int mode, int orientation) {
        this.bp1 = bp1;
        this.bp2 = bp2;
        this.identifier = identifier;
        this.color = color;
        this.finished = finished;
        this.mode = mode;
        this.orientation = orientation;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(
            ExtensionContext context) throws Exception {
        return Stream.of(
                new MeasureBoxData(new BlockPos(0, 1, 2),
                        new BlockPos(10, 5, 2),
                        new Identifier("minecraft:overworld"), DyeColor.BLACK,
                        true, 0, 0),
                new MeasureBoxData(new BlockPos(-30,200, -1012),
                        new BlockPos(10,3, -100),
                        new Identifier("minecraft:overworld"), DyeColor.ORANGE,
                        false, 5, 3),
                new MeasureBoxData(new BlockPos(10300, 1, 2),
                        new BlockPos(10000, 5, 2),
                        new Identifier("minecraft:overworld"), DyeColor.WHITE,
                        true, 20, 4));
    }

    @Override
    public Object[] get() {

        return new Object[]{this};
    }

}
