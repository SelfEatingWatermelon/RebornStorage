package RebornStorage.tiles;

import RebornStorage.blocks.BlockMultiCrafter;
import RebornStorage.multiblocks.MultiBlockCrafter;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPattern;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternContainer;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingPatternProvider;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.tile.data.TileDataManager;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import reborncore.common.multiblock.MultiblockControllerBase;
import reborncore.common.multiblock.MultiblockValidationException;
import reborncore.common.multiblock.rectangular.RectangularMultiblockTileEntityBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mark on 03/01/2017.
 */
public class TileMultiCrafter extends RectangularMultiblockTileEntityBase implements ICraftingPatternContainer, INetworkNode {

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		if(!getVarient().equals("frame")){
			throw new MultiblockValidationException(getVarient() + " is not valid for the frame of the block");
		}
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		if(!getVarient().equals("heat")){
			throw new MultiblockValidationException(getVarient() + " is not valid for the sides of the block");
		}
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		if(!getVarient().equals("heat")){
			throw new MultiblockValidationException(getVarient() + " is not valid for the sides of the block");
		}
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		if(!getVarient().equals("heat")){
			throw new MultiblockValidationException(getVarient() + " is not valid for the sides of the block");
		}
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		if(!getVarient().equals("cpu") && !getVarient().equals("storage")){
			throw new MultiblockValidationException(getVarient() + " is not valid for the inside of the block");
		}
	}

	@Override
	public void onMachineActivated() {
		if(getMultiBlock() != null){
			MultiBlockCrafter multiBlockCrafter = getMultiBlock();
			multiBlockCrafter.rebuildPatterns();
		}
		if(network != null){
			network.rebuildPatterns();
		}
	}

	@Override
	public void onMachineDeactivated() {
		if(getMultiBlock() != null){
			MultiBlockCrafter multiBlockCrafter = getMultiBlock();
			multiBlockCrafter.rebuildPatterns();
		}
		if(network != null){
			network.rebuildPatterns();
		}
	}

	String getVarient(){
		return world.getBlockState(pos).getValue(BlockMultiCrafter.VARIANTS);
	}

	@Override
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiBlockCrafter(getWorld());
	}

	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() {
		return MultiBlockCrafter.class;
	}

	public MultiBlockCrafter getMultiBlock() {
		return (MultiBlockCrafter) getMultiblockController();
	}

	//RS stuff

	protected TileDataManager dataManager;
	protected int ticks;
	protected INetworkMaster network;

	public TileMultiCrafter() {
		this.dataManager = new TileDataManager(this);
	}

	public int getEnergyUsage() {
		if (getMultiBlock() == null) {
			return 0;
		}
		return getMultiBlock().powerUsage;
	}

	public void update() {

		if(getMultiBlock() != null){
			MultiBlockCrafter multiBlockCrafter = getMultiBlock();
			if(multiBlockCrafter.inv.hasChanged){
				multiBlockCrafter.rebuildPatterns();
				multiBlockCrafter.inv.hasChanged = false;
			}
			if (!this.getWorld().isRemote && this.ticks == 0) {
				multiBlockCrafter.rebuildPatterns();
			}
		}
		if (!this.getWorld().isRemote) {
			++this.ticks;
			this.dataManager.detectAndSendChanges();
		}
	}



	public int getSpeedUpdateCount() {
		if(getMultiBlock() == null){
			return 0;
		}
		return getMultiBlock().speed;
	}

	public IItemHandler getFacingInventory() {
		return null;
	}

	@Override
	public TileEntity getFacingTile() {
		return null;
	}

	public List<ICraftingPattern> getPatterns() {
		if(getMultiBlock() == null){
			return new ArrayList<>();
		}
		return getMultiBlock().actualPatterns;
	}

	@Override
	public BlockPos getPosition() {
		return getPos();
	}

	@Override
	public void onConnected(INetworkMaster iNetworkMaster) {
		network = iNetworkMaster;
		if(getMultiBlock() != null){
			getMultiBlock().onConnectionChange(network, true, pos);
		}

	}

	@Override
	public void onDisconnected(INetworkMaster iNetworkMaster) {
		if(getMultiBlock() != null){
			getMultiBlock().onConnectionChange(network, false, pos);
		}
		network = null;
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public boolean canConduct(EnumFacing enumFacing) {
		return false;
	}

	@Override
	public INetworkMaster getNetwork() {
		return network;
	}

	@Override
	public World getNodeWorld() {
		return world;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (getMultiblockController() != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (getMultiblockController() != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) new InvWrapper(getMultiBlock().inv);
		}
		return super.getCapability(capability, facing);
	}

}
