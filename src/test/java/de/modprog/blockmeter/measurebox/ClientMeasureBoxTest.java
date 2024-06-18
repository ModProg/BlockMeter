package de.modprog.blockmeter.measurebox;

import de.modprog.blockmeter.util.JSONSource;
import de.modprog.blockmeter.util.parser.ParseBlockPos;
import de.modprog.blockmeter.util.parser.ParseDyeColor;
import me.shedaniel.autoconfig.ConfigManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import win.baruna.blockmeter.BlockMeterClient;
import win.baruna.blockmeter.ModConfig;
import win.baruna.blockmeter.measurebox.ClientMeasureBox;

import static net.minecraft.world.dimension.DimensionTypes.OVERWORLD_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ClientMeasureBoxTest {

    @ParameterizedTest
    @JSONSource(classes = {ParseDyeColor.class}, jsons = {"red", "black",
            "green", "blue"})
    void testSetColor(final DyeColor color) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox.getBox(new BlockPos(0, 0, 0),
                    OVERWORLD_ID);

            box.setColor(color);

            assertThat(box.getColor()).isEqualTo(color);
        }
    }

    @ParameterizedTest
    @JSONSource(classes = {ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class, ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class}, jsons = {
            "(0|0|0), (10|10|10), (0|0|10), (0|10|0), (0|10|10), (10|0|0), (10|0|10), (10|10|0)",
            "(-30|5|25), (-90|2|50), (-30|5|50), (-30|2|25), (-30|2|50), (-90|5|25), (-90|5|50), (-90|2|25)"
    })
    void testIsCorner(BlockPos bp0, BlockPos bp1, BlockPos bp2, BlockPos bp3,
                      BlockPos bp4, BlockPos bp5, BlockPos bp6, BlockPos bp7) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(bp0, OVERWORLD_ID);

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
    @JSONSource(classes = {ParseBlockPos.class, ParseBlockPos.class,
            ParseBlockPos.class}, jsons = {
            "(0|1|2), (3|4|5), (0|4|2)"
    })
    void testLoosenCorner(BlockPos bp0, BlockPos bp1, BlockPos newBp0) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(bp0, OVERWORLD_ID);

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
    @JSONSource(classes = {ParseDyeColor.class}, jsons = {
            "red", "blue", "white", "black"
    })
    void testSetColorIndex(DyeColor color) {
        try (MockedStatic<BlockMeterClient> client = getBMC()) {
            ClientMeasureBox.setColorIndex(color.getId());
            final ClientMeasureBox box = ClientMeasureBox
                    .getBox(new BlockPos(0, 0, 0), OVERWORLD_ID);

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
