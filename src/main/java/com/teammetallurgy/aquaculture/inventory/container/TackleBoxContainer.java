package com.teammetallurgy.aquaculture.inventory.container;

import com.teammetallurgy.aquaculture.api.AquacultureAPI;
import com.teammetallurgy.aquaculture.block.tileentity.TackleBoxTileEntity;
import com.teammetallurgy.aquaculture.init.AquaBlocks;
import com.teammetallurgy.aquaculture.init.AquaGuis;
import com.teammetallurgy.aquaculture.inventory.container.slot.SlotFishingRod;
import com.teammetallurgy.aquaculture.inventory.container.slot.SlotHidable;
import com.teammetallurgy.aquaculture.item.BaitItem;
import com.teammetallurgy.aquaculture.item.HookItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TackleBoxContainer extends Container {
    public TackleBoxTileEntity tackleBox;
    private int rows = 4;
    private int collumns = 4;
    public Slot slotHook;
    public Slot slotBait;
    public Slot slotLine;
    public Slot slotBobber;

    public TackleBoxContainer(int windowID, BlockPos pos, PlayerInventory playerInventory) {
        super(AquaGuis.TACKLE_BOX, windowID);
        this.tackleBox = (TackleBoxTileEntity) playerInventory.player.world.getTileEntity(pos);
        if (this.tackleBox != null) {
            this.tackleBox.openInventory(playerInventory.player);
            this.tackleBox.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                SlotFishingRod fishingRod = (SlotFishingRod) addSlot(new SlotFishingRod(handler, 0, 117, 21));
                this.slotHook = this.addSlot(new SlotHidable(fishingRod, 0, 106, 44));
                this.slotBait = this.addSlot(new SlotHidable(fishingRod, 1, 129, 44) {
                    @Override
                    public boolean canTakeStack(PlayerEntity player) {
                        return false;
                    }
                });
                this.slotLine = this.addSlot(new SlotHidable(fishingRod, 2, 106, 67));
                this.slotBobber = this.addSlot(new SlotHidable(fishingRod, 3, 129, 67));

                //Tackle Box
                for (int column = 0; column < collumns; ++column) {
                    for (int row = 0; row < rows; ++row) {
                        this.addSlot(new SlotItemHandler(handler, 1 + row + column * collumns, 8 + row * 18, 8 + column * 18) {
                            @Override
                            public boolean isItemValid(@Nonnull ItemStack stack) {
                                Item item = stack.getItem();
                                boolean isDyeable = stack.getItem() instanceof IDyeableArmorItem;
                                return item.isIn(AquacultureAPI.Tags.TACKLE_BOX) || item instanceof HookItem || item instanceof BaitItem ||
                                        item.isIn(AquacultureAPI.Tags.FISHING_LINE) && isDyeable || item.isIn(AquacultureAPI.Tags.BOBBER) && isDyeable;
                            }
                        });
                    }
                }
            });

            for (int column = 0; column < 3; ++column) {
                for (int row = 0; row < 9; ++row) {
                    this.addSlot(new Slot(playerInventory, row + column * 9 + 9, 8 + row * 18, 90 + column * 18));
                }
            }

            for (int row = 0; row < 9; ++row) {
                this.addSlot(new Slot(playerInventory, row, 8 + row * 18, 148));
            }
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return isWithinUsableDistance(IWorldPosCallable.of(Objects.requireNonNull(tackleBox.getWorld()), tackleBox.getPos()), player, AquaBlocks.TACKLE_BOX);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity player, int index) { //TODO Temporarily disabled
        /*ItemStack transferStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            System.out.println("INDEX: " + index + " SLOT: " + slot.slotNumber + " STACK: " + slotStack);
            transferStack = slotStack.copy();
            if (index < this.rows * this.collumns) {
                System.out.println("FIRST IF");
                if (!this.mergeItemStack(slotStack, this.rows * this.collumns, this.inventorySlots.size(), true)) {
                    System.out.println("IF NOT MERGE");
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotStack, 0, this.rows * this.collumns, false)) {
                System.out.println("ELSE IF NOT MERGE");
                return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty()) {
                System.out.println("EMPTY SLOT STACK");
                slot.putStack(ItemStack.EMPTY);
            } else {
                System.out.println("ELSE. CHANGE SLOT");
                slot.onSlotChanged();
            }
        }
        System.out.println("TRANSFERSTACK: " + transferStack);*/
        return ItemStack.EMPTY;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if (this.tackleBox != null) {
            this.tackleBox.closeInventory(player);
        }
    }

    @Override
    @Nonnull
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
        //Bait replacing
        if (slotId >= 0 && clickType == ClickType.PICKUP) {
            Slot slot = this.inventorySlots.get(slotId);
            if (slot == this.slotBait) {
                SlotItemHandler slotHandler = (SlotItemHandler) slot;
                if (slotHandler.isItemValid(player.inventory.getItemStack())) {
                    slotHandler.putStack(ItemStack.EMPTY); //Set to empty, to allow new bait to get put in
                }
            }
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }
}