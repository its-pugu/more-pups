package pugu.pups;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class DogBedBlockEntity extends BlockEntity {
    private @Nullable UUID ownerDogId;
    private DyeColor color = DyeColor.WHITE;

    public DogBedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DOG_BED, pos, state);
    }

    public @Nullable UUID getOwnerDogId() {
        return this.ownerDogId;
    }

    public boolean isClaimedBy(UUID dogId) {
        return dogId.equals(this.ownerDogId);
    }

    public boolean isUnclaimed() {
        return this.ownerDogId == null;
    }

    public void claim(UUID dogId) {
        this.ownerDogId = dogId;
        this.setChanged();
    }

    public void clearClaim() {
        this.ownerDogId = null;
        this.setChanged();
    }

    public DyeColor getColor() {
        return this.color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (this.ownerDogId != null) {
            output.putString("owner_dog", this.ownerDogId.toString());
        }

        output.store("color", DyeColor.CODEC, this.color);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.ownerDogId = input.getString("owner_dog").map(UUID::fromString).orElse(null);
        this.color = input.read("color", DyeColor.CODEC).orElse(DyeColor.WHITE);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}