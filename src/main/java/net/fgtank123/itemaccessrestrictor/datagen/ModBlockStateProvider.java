package net.fgtank123.itemaccessrestrictor.datagen;

import net.fgtank123.itemaccessrestrictor.ModMain;
import net.fgtank123.itemaccessrestrictor.core.ItemAccessRestrictorBlock;
import net.fgtank123.itemaccessrestrictor.definitions.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ModMain.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile.ExistingModelFile itemAccessRestrictorModelFile = models().getExistingFile(
            ModBlocks.ITEM_ACCESS_RESTRICTOR.getId()
        );
        ModelFile.ExistingModelFile itemAccessRestrictorBlockingModelFile = models().getExistingFile(
            ModBlocks.ITEM_ACCESS_RESTRICTOR.getId().withSuffix("_blocking")
        );
        registerBlockStates(
            ModBlocks.ITEM_ACCESS_RESTRICTOR.get(),
            PropertyDispatch.properties(ItemAccessRestrictorBlock.FACING, ItemAccessRestrictorBlock.BLOCKING)
                .generate((facing, blocking) -> {
                    int angleX;
                    int angleY;
                    switch (facing) {
                        case DOWN -> {
                            angleX = 0;
                            angleY = 0;
                        }
                        case UP -> {
                            angleX = 180;
                            angleY = 0;
                        }
                        case NORTH -> {
                            angleX = 270;
                            angleY = 0;
                        }
                        case SOUTH -> {
                            angleX = 90;
                            angleY = 0;
                        }
                        case WEST -> {
                            angleX = 90;
                            angleY = 90;
                        }
                        case EAST -> {
                            angleX = 270;
                            angleY = 90;
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + facing);
                    }
                    return applyRotation(
                        Variant.variant(),
                        angleX,
                        angleY
                    ).with(
                        VariantProperties.MODEL,
                        blocking ? itemAccessRestrictorBlockingModelFile.getLocation() : itemAccessRestrictorModelFile.getLocation()
                    );
                })
        );
    }

    private void registerBlockStates(Block block, PropertyDispatch propertyDispatch) {
        MultiVariantGenerator builder = MultiVariantGenerator.multiVariant(block).with(propertyDispatch);
        registeredBlocks.put(block, () -> builder.get().getAsJsonObject());
    }

    protected static Variant applyRotation(Variant variant, int angleX, int angleY) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
        if (angleX != 0) {
            variant = variant.with(VariantProperties.X_ROT, rotationByAngle(angleX));
        }
        if (angleY != 0) {
            variant = variant.with(VariantProperties.Y_ROT, rotationByAngle(angleY));
        }
        return variant;
    }

    private static VariantProperties.Rotation rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    private static int normalizeAngle(int angle) {
        return angle - (angle / 360) * 360;
    }

}
