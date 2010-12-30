
public class DefaultCommand implements Command {

	@Override
	public boolean validate(LWC lwc, Player player, String command, String[] args) {
		return command.equalsIgnoreCase("lwc") && args.length == 0;
	}

	@Override
	public void execute(LWC lwc, Player player, String command, String[] args) {
		player.sendMessage("DefaultCommand");
	}
	
}
