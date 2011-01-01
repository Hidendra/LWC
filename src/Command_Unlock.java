import com.griefcraft.model.Entity;
import com.griefcraft.model.EntityTypes;

import static com.griefcraft.util.StringUtils.*;
import static com.griefcraft.util.StringUtils.*;

public class Command_Unlock implements Command {

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if (args.length < 1) {
			player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + "/lwc -u <Password>");
			return;
		}

		String password = join(args, 1);
		password = lwc.encrypt(password);

		if(!lwc.getMemoryDatabase().hasPendingUnlock(player.getName())) {
			player.sendMessage(Colors.Red + "Nothing selected. Open a locked Chest/Furnace first.");
			return;
		} 
		
		else {
			int chestID = lwc.getMemoryDatabase().getUnlockID(player.getName());

			if(chestID == -1) {
				player.sendMessage(Colors.Red + "[lwc] Internal error. [ulock]");
				return;
			}

			Entity entity = lwc.getPhysicalDatabase().loadProtectedEntity(chestID);

			if(entity.getType() != EntityTypes.PASSWORD) {
				player.sendMessage(Colors.Red + "That is not password protected!");
				return;
			}

			if(entity.getPassword().equals(password)) {
				player.sendMessage(Colors.Green + "Password accepted.");
				lwc.getMemoryDatabase().unregisterUnlock(player.getName());
				lwc.getMemoryDatabase().registerPlayer(player.getName(), chestID);

				for(ComplexBlock entity_ : lwc.getEntitySet(entity.getX(), entity.getY(), entity.getZ())) {
					if(entity_ != null) {
						entity_.update();
					}
				}
			} else {
				player.sendMessage(Colors.Red + "Invalid password.");
			}
		}
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "u") || hasFlag(args, "unlock");
	}

}
