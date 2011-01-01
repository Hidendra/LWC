
import static com.griefcraft.util.StringUtils.*;

public class Command_Modes implements Command {

	@Override
	public void execute(LWC lwc, Player player, String[] args) {
		if(args.length < 2) {
			lwc.sendSimpleUsage(player, "/lwc -p <persist|droptransfer>");
			return;
		}
		
		String mode = args[1].toLowerCase();
		
		if(mode.equals("persist")) {
			if(!lwc.isAdmin(player) && lwc.isModeBlacklisted(mode)) {
				player.sendMessage(Colors.Red + "That mode is currently disabled");
				return;
			}
			
			lwc.getMemoryDatabase().registerMode(player.getName(), mode);
			player.sendMessage(Colors.Green + "Persistance mode activated");
			player.sendMessage(Colors.Green + "Type " + Colors.Gold + "/lwc -r modes" + Colors.Green + " to undo (or logout)");
		}
		
		else if (mode.equals("droptransfer")) {
			mode = "dropTransfer";

			if (!lwc.isAdmin(player) && lwc.isModeBlacklisted(mode)) {
				player.sendMessage(Colors.Red + "That mode is currently disabled");
				return;
			}

			if (args.length < 3) {
				player.sendMessage(Colors.Green + "LWC Drop Transfer");
				player.sendMessage("");
				player.sendMessage(Colors.LightGreen + "/lwc -p droptransfer select - Select a chest to drop transfer to");
				player.sendMessage(Colors.LightGreen + "/lwc -p droptransfer on - Turn on drop transferring");
				player.sendMessage(Colors.LightGreen + "/lwc -p droptransfer off - Turn off drop transferring");
				player.sendMessage(Colors.LightGreen + "/lwc -p droptransfer status - Check the status of drop transferring");
				return;
			}

			String action = args[2].toLowerCase();
			String playerName = player.getName();

			if (action.equals("select")) {
				if (lwc.isPlayerDropTransferring(playerName)) {
					player.sendMessage(Colors.Red + "Please turn off drop transfer before reselecting a chest.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, mode);
				lwc.getMemoryDatabase().registerAction("dropTransferSelect", playerName, "");
				
				player.sendMessage(Colors.Green + "Please left-click a registered chest to set as your transfer target.");
			} else if (action.equals("on")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);
				
				if (target == -1) {
					player.sendMessage(Colors.Red + "Please register a chest before turning drop transfer on.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, "dropTransfer");
				lwc.getMemoryDatabase().registerMode(playerName, "dropTransfer", "t" + target);
				player.sendMessage(Colors.Green + "Drop transfer is now on.");
				player.sendMessage(Colors.Green + "Any items dropped will be transferred to your chest.");
			} else if (action.equals("off")) {
				int target = lwc.getPlayerDropTransferTarget(playerName);
				
				if (target == -1) {
					player.sendMessage(Colors.Red + "Please register a chest before turning drop transfer off.");
					return;
				}

				lwc.getMemoryDatabase().unregisterMode(playerName, "dropTransfer");
				lwc.getMemoryDatabase().registerMode(playerName, "dropTransfer", "f" + target);
				
				player.sendMessage(Colors.Green + "Drop transfer is now off.");
			} else if (action.equals("status")) {
				if (lwc.getPlayerDropTransferTarget(playerName) == -1) {
					player.sendMessage(Colors.Green + "You have not registered a drop transfer target.");
				} else {
					if (lwc.isPlayerDropTransferring(playerName)) {
						player.sendMessage(Colors.Green + "Drop transfer is currently active.");
					} else {
						player.sendMessage(Colors.Green + "Drop transfer is currently inactive."); 
					}
				}
			}
		}
		
	}

	@Override
	public boolean validate(LWC lwc, Player player, String[] args) {
		return hasFlag(args, "p");
	}

}
