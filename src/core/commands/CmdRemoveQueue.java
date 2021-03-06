package core.commands;

import core.Constants;
import core.entities.QueueManager;
import core.entities.Server;
import core.exceptions.BadArgumentsException;
import core.exceptions.DoesNotExistException;
import core.util.Utils;
import net.dv8tion.jda.core.entities.Member;

public class CmdRemoveQueue extends Command {

	public CmdRemoveQueue() {
		this.helpMsg = Constants.REMOVEQUEUE_HELP;
		this.description = Constants.REMOVEQUEUE_DESC;
		this.name = Constants.REMOVEQUEUE_NAME;
		this.adminRequired = true;
	}

	@Override
	public void execCommand(Server server, Member member, String[] args) {
		QueueManager qm = server.getQueueManager();
		try {
			if (args.length > 0) {
				for (String a : args) {
					try {
						qm.removeQueue(Integer.valueOf(a));
					} catch (NumberFormatException ex) {
						qm.removeQueue(a);
					}
				}
			}else{
				throw new BadArgumentsException();
			}
			qm.updateTopic();
			this.response = Utils.createMessage(String.format("Queue %s removed", args[0]), qm.getHeader(), true);
			System.out.println(success());
		} catch (DoesNotExistException | BadArgumentsException ex) {
			this.response = Utils.createMessage("Error!", ex.getMessage(), false);
		}

	}

}
